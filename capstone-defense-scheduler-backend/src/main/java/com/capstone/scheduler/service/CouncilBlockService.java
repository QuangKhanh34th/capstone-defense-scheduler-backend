package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CouncilBlockRequest;
import com.capstone.scheduler.entity.CouncilBlock;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.repository.CouncilBlockRepository;
import com.capstone.scheduler.repository.DefenseDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouncilBlockService {

    private final CouncilBlockRepository councilBlockRepository;
    private final DefenseDayRepository defenseDayRepository;

    // Hằng số cấu hình từ BR
    private static final long BLOCK_DURATION_MINUTES = 90; // BR-62
    private static final long BREAK_DURATION_MINUTES = 10; // BR-63
    private static final int MAX_BLOCKS_PER_DAY = 7;       // BR-61

    @Transactional
    public CouncilBlock createBlock(Integer dayId, CouncilBlockRequest request) {

        // 1. Kiểm tra Ngày bảo vệ tồn tại
        DefenseDay defenseDay = defenseDayRepository.findById(dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Day not found"));

        LocalTime newStart = request.getStartTime();
        LocalTime newEnd = request.getEndTime();

        // 2. Validate BR-50: Start < End
        if (!newStart.isBefore(newEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before End time.");
        }

        // 3. Validate BR-62: Thời lượng phải đúng 90 phút (Fixed)
        long duration = Duration.between(newStart, newEnd).toMinutes();
        if (duration != BLOCK_DURATION_MINUTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid duration! Per BR-62, a slot must be exactly " + BLOCK_DURATION_MINUTES + " minutes.");
        }

        // 4. Lấy danh sách các block đã có trong ngày (Đã sort theo thời gian)
        List<CouncilBlock> existingBlocks = councilBlockRepository.findByDefenseDay_DayIdOrderByStartTimeAsc(dayId);

        // 5. Validate BR-61: Không quá 7 slot/ngày
        if (existingBlocks.size() >= MAX_BLOCKS_PER_DAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quota exceeded! Per BR-61, a Defense Day can have maximum " + MAX_BLOCKS_PER_DAY + " slots.");
        }

        // 6. Validate BR-12 & BR-63: Check trùng giờ VÀ khoảng nghỉ 10 phút
        validateTimeConflictAndBreak(existingBlocks, newStart, newEnd);

        // 7. Lưu Block nếu tất cả hợp lệ
        CouncilBlock block = new CouncilBlock();
        block.setDefenseDay(defenseDay);
        block.setBlockName(request.getBlockName());
        block.setStartTime(newStart);
        block.setEndTime(newEnd);

        // Nếu user không nhập expected count, mặc định là 6 (BR-13)
        block.setExpectedProjectCount(request.getExpectedProjectCount() != null ? request.getExpectedProjectCount() : 6);

        return councilBlockRepository.save(block);
    }

    /**
     * Hàm check overlap và khoảng nghỉ (Logic phức tạp tách riêng)
     */
    private void validateTimeConflictAndBreak(List<CouncilBlock> existingBlocks, LocalTime newStart, LocalTime newEnd) {
        for (CouncilBlock existing : existingBlocks) {
            LocalTime existStart = existing.getStartTime();
            LocalTime existEnd = existing.getEndTime();

            // A. Check Trùng lặp thời gian (Overlap) cơ bản
            // (StartA < EndB) và (EndA > StartB)
            if (newStart.isBefore(existEnd) && newEnd.isAfter(existStart)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Time Conflict! The new block overlaps with existing block: " + existing.getBlockName());
            }

            // B. Check Khoảng nghỉ BR-63 (10 phút)
            // Nếu block mới nằm ngay SAU block cũ: NewStart phải >= ExistEnd + 10p
            if (newStart.isAfter(existStart)) {
                long gapAfter = Duration.between(existEnd, newStart).toMinutes();
                // Chú ý: gapAfter < 0 nghĩa là overlap (đã check ở trên), gapAfter >= 0 là không overlap
                if (gapAfter >= 0 && gapAfter < BREAK_DURATION_MINUTES) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Violation of BR-63! Must have at least " + BREAK_DURATION_MINUTES +
                                    " mins break after existing block " + existing.getBlockName() +
                                    " (ends at " + existEnd + ")");
                }
            }

            // Nếu block mới nằm ngay TRƯỚC block cũ: NewEnd <= ExistStart - 10p
            if (newEnd.isBefore(existEnd)) {
                long gapBefore = Duration.between(newEnd, existStart).toMinutes();
                if (gapBefore >= 0 && gapBefore < BREAK_DURATION_MINUTES) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Violation of BR-63! Must have at least " + BREAK_DURATION_MINUTES +
                                    " mins break before existing block " + existing.getBlockName() +
                                    " (starts at " + existStart + ")");
                }
            }
        }
    }
}