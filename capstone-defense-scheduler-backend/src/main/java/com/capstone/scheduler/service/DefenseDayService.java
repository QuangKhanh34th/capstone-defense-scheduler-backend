package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.DefenseDayRequest;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefenseDayService {

    private final DefenseDayRepository defenseDayRepository;
    private final DefenseRoundRepository defenseRoundRepository;

    @Transactional
    public List<DefenseDay> createDays(Integer roundId, DefenseDayRequest request) {

        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Round not found"));

        Semester semester = round.getSemester();

        List<DefenseDay> savedDays = new ArrayList<>();

        for (LocalDate date : request.getDefenseDates()) {

            if (date.isBefore(semester.getStartDate()) || date.isAfter(semester.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ngày không hợp lệ: " + date + ". Ngày bảo vệ phải nằm trong thời gian của học kỳ "
                                + semester.getName() + " (" + semester.getStartDate() + " đến " + semester.getEndDate() + ")");
            }

            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ngày không hợp lệ: " + date + " là Chủ Nhật. Hội đồng không làm việc vào Chủ Nhật.");
            }

            // 3. Check trùng và Lưu
            if (!defenseDayRepository.existsByDefenseRound_RoundIdAndDefenseDate(roundId, date)) {
                DefenseDay day = new DefenseDay();
                day.setDefenseRound(round);
                day.setDefenseDate(date);

                savedDays.add(defenseDayRepository.save(day));
            }
        }

        return savedDays;
    }
}