package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AvailabilityRegisterRequest;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.LecturerAvailability;
import com.capstone.scheduler.repository.DefenseDayRepository;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.LecturerAvailabilityRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final LecturerAvailabilityRepository availabilityRepository;
    private final DefenseRoundRepository defenseRoundRepository;
    private final LecturerRepository lecturerRepository;
    private final DefenseDayRepository defenseDayRepository;

    @Transactional
    public List<LecturerAvailability> registerAvailability(AvailabilityRegisterRequest request) {

        // 1. Tìm Giảng Viên
        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found"));

        // 2. Tìm Đợt Bảo Vệ
        DefenseRound round = defenseRoundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Round not found"));

        // 3. Lấy danh sách các ngày bảo vệ HỢP LỆ của Round này (Từ bảng DefenseDay)
        List<DefenseDay> validDefenseDays = defenseDayRepository.findByDefenseRound_RoundId(request.getRoundId());

        // Chuyển sang Set<LocalDate> để so sánh cho nhanh
        Set<LocalDate> validDatesSet = validDefenseDays.stream()
                .map(DefenseDay::getDefenseDate)
                .collect(Collectors.toSet());

        if (validDatesSet.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This Defense Round has no Defense Days configured yet.");
        }

        List<LecturerAvailability> savedList = new ArrayList<>();

        // 4. Duyệt qua từng ngày giảng viên gửi lên
        for (LocalDate date : request.getAvailableDates()) {

            // VALIDATION: Ngày chọn phải nằm trong danh sách DefenseDay
            if (!validDatesSet.contains(date)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid date: " + date + ". This date is NOT defined in the Defense Round days.");
            }

            // CHECK TRÙNG: Nếu đã đăng ký rồi thì bỏ qua (Idempotent)
            boolean exists = availabilityRepository
                    .existsByLecturer_LecturerIdAndDefenseRound_RoundIdAndAvailableDate(
                            request.getLecturerId(), request.getRoundId(), date);

            if (!exists) {
                LecturerAvailability availability = new LecturerAvailability();
                availability.setLecturer(lecturer);
                availability.setDefenseRound(round);
                availability.setAvailableDate(date);

                savedList.add(availabilityRepository.save(availability));
            }
        }

        return savedList;
    }
}