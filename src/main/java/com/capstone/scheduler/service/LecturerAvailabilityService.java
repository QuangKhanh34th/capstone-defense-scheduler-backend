package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AvailabilityRequest;
import com.capstone.scheduler.dto.response.AvailabilityResponse;
import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.LecturerAvailability;
import com.capstone.scheduler.repository.DefenseDayRepository;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.LecturerAvailabilityRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerAvailabilityService {

    private final LecturerAvailabilityRepository availabilityRepository;
    private final LecturerRepository lecturerRepository;
    private final DefenseRoundRepository roundRepository;
    private final DefenseDayRepository defenseDayRepository;

    /**
     * Lấy danh sách các đợt bảo vệ
     */
    @Transactional(readOnly = true)
    public List<DefenseRoundResponse> getAllRounds() {
        return roundRepository.findAll().stream()
                .map(this::toRoundResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách ngày bảo vệ của 1 đợt
     */
    @Transactional(readOnly = true)
    public List<DefenseDayResponse> getDaysByRound(Integer roundId) {
        return defenseDayRepository.findByDefenseRound_RoundIdOrderByDefenseDateAsc(roundId).stream()
                .map(this::toDayResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch rảnh của giảng viên theo đợt
     */
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilityByLecturerAndRound(Integer lecturerId, Integer roundId) {
        return availabilityRepository.findByLecturerIdAndRoundId(lecturerId, roundId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Đăng ký lịch rảnh
     */
    @Transactional
    public AvailabilityResponse registerAvailability(AvailabilityRequest request) {
        // Kiểm tra lecturer tồn tại
        Lecturer lecturer;
        if (request.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found with ID: " + request.getLecturerId()));
        } else if (request.getUserId() != null) {
            lecturer = lecturerRepository.findByUser_UserId(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found for User ID: " + request.getUserId()));
        } else if (request.getUsername() != null) {
            lecturer = lecturerRepository.findByUser_Username(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found for Username: " + request.getUsername()));
        } else {
            throw new RuntimeException("Lecturer ID, User ID, or Username must be provided");
        }

        // Kiểm tra round tồn tại
        DefenseRound round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new RuntimeException("Defense Round not found with ID: " + request.getRoundId()));

        // Kiểm tra đã đăng ký chưa
        if (availabilityRepository.existsByLecturer_LecturerIdAndAvailableDate(
                request.getLecturerId(), request.getAvailableDate())) {
            throw new RuntimeException("Lecturer already registered for this date");
        }

        // Tạo mới availability
        LecturerAvailability availability = LecturerAvailability.builder()
                .lecturer(lecturer)
                .defenseRound(round)
                .availableDate(request.getAvailableDate())
                .build();

        LecturerAvailability saved = availabilityRepository.save(availability);
        return toResponse(saved);
    }

    /**
     * Hủy đăng ký lịch rảnh
     */
    @Transactional
    public void deleteAvailability(Integer availabilityId) {
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new RuntimeException("Availability not found with ID: " + availabilityId);
        }
        availabilityRepository.deleteById(availabilityId);
    }

    /**
     * Hủy đăng ký theo lecturer, round, date
     */
    @Transactional
    public void deleteByLecturerAndDate(Integer lecturerId, Integer roundId, LocalDate date) {
        List<LecturerAvailability> availabilities = availabilityRepository.findByLecturerIdAndRoundId(lecturerId, roundId);
        
        availabilities.stream()
                .filter(a -> a.getAvailableDate().equals(date))
                .findFirst()
                .ifPresent(a -> availabilityRepository.deleteById(a.getAvailabilityId()));
    }

    // === Mapper methods ===

    private AvailabilityResponse toResponse(LecturerAvailability entity) {
        return AvailabilityResponse.builder()
                .availabilityId(entity.getAvailabilityId())
                .lecturerId(entity.getLecturer().getLecturerId())
                .lecturerName(entity.getLecturer().getFullName())
                .roundId(entity.getDefenseRound().getRoundId())
                .roundName(entity.getDefenseRound().getRoundName())
                .availableDate(entity.getAvailableDate())
                .build();
    }

    private DefenseRoundResponse toRoundResponse(DefenseRound entity) {
        return DefenseRoundResponse.builder()
                .roundId(entity.getRoundId())
                .roundName(entity.getRoundName())
                .description(entity.getDescription())
                .semesterId(entity.getSemester().getSemesterId())
                .semesterName(entity.getSemester().getName())
                .status(entity.getStatus())
                .build();
    }

    private DefenseDayResponse toDayResponse(DefenseDay entity) {
        return DefenseDayResponse.builder()
                .dayId(entity.getDayId())
                .defenseDate(entity.getDefenseDate())
                .roundId(entity.getDefenseRound().getRoundId())
                .roundName(entity.getDefenseRound().getRoundName())
                .build();
    }
}
