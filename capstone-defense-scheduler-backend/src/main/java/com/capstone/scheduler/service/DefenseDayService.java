package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateDefenseDayRequest;
import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.repository.DefenseDayRepository;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefenseDayService {

    private final DefenseDayRepository defenseDayRepository;
    private final DefenseRoundRepository defenseRoundRepository;

    @Transactional(readOnly = true)
    public List<DefenseDayResponse> getAllDefenseDays(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        List<DefenseDay> defenseDays = defenseDayRepository.findByDefenseRound_RoundIdOrderByDefenseDateAsc(roundId);

        return defenseDays.stream()
                .map(day -> DefenseDayResponse.builder()
                        .dayId(day.getDayId())
                        .defenseDate(day.getDefenseDate())
                        .roundId(round.getRoundId())
                        .roundName(round.getRoundName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DefenseDayResponse> createDefenseDays(Integer roundId, CreateDefenseDayRequest request) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));
        Semester semester = round.getSemester();

        List<DefenseDay> daysToSave = new ArrayList<>();
        Set<LocalDate> uniqueInputDates = new HashSet<>();

        // Duyệt qua từng ngày để Validate
        for (LocalDate date : request.getDefenseDates()) {

            if (!uniqueInputDates.add(date)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate date in request list: " + date);
            }

            // Không được chọn Chủ nhật
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid Date: " + date + " is a Sunday.");
            }

            // Range Semester
            if (date.isBefore(semester.getStartDate()) || date.isAfter(semester.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid Date: " + date + " is outside Semester range ("
                                + semester.getStartDate() + " - " + semester.getEndDate() + ")");
            }

            // Check trùng trong Database
            if (defenseDayRepository.existsByDefenseRound_RoundIdAndDefenseDate(roundId, date)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Date " + date + " already exists in this Round.");
            }

            daysToSave.add(DefenseDay.builder()
                    .defenseDate(date)
                    .defenseRound(round)
                    .build());
        }

        List<DefenseDay> savedDays = defenseDayRepository.saveAll(daysToSave);

        return savedDays.stream()
                .map(day -> DefenseDayResponse.builder()
                        .dayId(day.getDayId())
                        .defenseDate(day.getDefenseDate())
                        .roundId(round.getRoundId())
                        .roundName(round.getRoundName())
                        .build())
                .collect(Collectors.toList());
    }
}