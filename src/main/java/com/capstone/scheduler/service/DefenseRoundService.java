package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.DefenseRoundRequest;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.RoundProject;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundProjectStatus;
import com.capstone.scheduler.enums.RoundStatus; // IMPORT ENUM
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.RoundProjectRepository;
import com.capstone.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefenseRoundService {

    private final DefenseRoundRepository defenseRoundRepository;
    private final SemesterRepository semesterRepository;
    private final NotificationTriggerService notificationTriggerService;
    private final RoundProjectRepository roundProjectRepository;

    @Transactional
    public DefenseRoundResponse createRound(Integer semesterId, DefenseRoundRequest request) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Semester not found with ID: " + semesterId));

        DefenseRound defenseRound = DefenseRound.builder()
                .roundName(request.getRoundName())
                .description(request.getDescription())
                .semester(semester)
                .status(RoundStatus.PLANNING)
                .build();

        DefenseRound savedRound = defenseRoundRepository.save(defenseRound);

        return mapToResponse(savedRound);
    }

    @Transactional(readOnly = true)
    public Page<DefenseRoundResponse> getDefenseRounds(Integer semesterId, Pageable pageable) {
        Specification<DefenseRound> spec = (root, query, cb) -> {
            if (semesterId != null) {
                return cb.equal(root.get("semester").get("semesterId"), semesterId);
            }
            return cb.conjunction();
        };

        Page<DefenseRound> pageResult = defenseRoundRepository.findAll(spec, pageable);
        return pageResult.map(this::mapToResponse);
    }

    private DefenseRoundResponse mapToResponse(DefenseRound round) {
        return DefenseRoundResponse.builder()
                .roundId(round.getRoundId())
                .roundName(round.getRoundName())
                .description(round.getDescription())
                .status(round.getStatus())
                .semesterId(round.getSemester().getSemesterId())
                .semesterName(round.getSemester().getName())
                .build();
    }

    @Transactional
    public void cancelDefenseRound(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        // 1. CHẶN BẢO VỆ: Chỉ cho phép hủy nếu đang ở trạng thái PLANNING
        if (round.getStatus() != RoundStatus.PLANNING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel this round. Only rounds in PLANNING status can be cancelled. Current status is: "
                            + round.getStatus());
        }

        // 2. CẬP NHẬT TRẠNG THÁI: Hủy đợt
        round.setStatus(RoundStatus.CANCELLED);
        defenseRoundRepository.save(round);

        // 3. GIẢI PHÓNG ĐỀ TÀI: Xóa toàn bộ liên kết (RoundProject) của đợt này
        // Các Project gốc không bị đụng chạm gì tới (vẫn giữ nguyên PENDING),
        // nhưng vì phiếu đăng ký đã mất nên chúng sẽ tự động trở thành "Unassigned"
        // (Chưa được gán đợt)
        roundProjectRepository.deleteByDefenseRound_RoundId(roundId);

        log.info("Successfully cancelled Defense Round ID {} and released all its projects back to PENDING pool.",
                roundId);
    }

    // ==========================================
    // API 1: EXPORT GRADING TEMPLATE
    // ==========================================
    @Transactional(readOnly = true)
    public byte[] exportResultTemplate(Integer roundId) throws IOException {
        List<RoundProject> projectsToGrade = roundProjectRepository
                .findByDefenseRound_RoundIdAndResultStatusAndProject_Status(
                        roundId, RoundProjectStatus.IN_PROGRESS, ProjectStatus.PENDING);

        if (projectsToGrade.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No IN_PROGRESS projects found to grade in this round.");
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Grading_Results");

            // Create Header
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID (Hidden)", "No.", "Project Name", "PASSED (Mark X)", "FAILED (Mark X)" };

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Data
            int rowIdx = 1;
            for (RoundProject rp : projectsToGrade) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rp.getRoundProjectId());
                row.createCell(1).setCellValue(rowIdx - 1);
                row.createCell(2).setCellValue(rp.getProject().getTitle());
            }

            // Format Columns
            sheet.setColumnHidden(0, true); // Hide ID column
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(2, 15000);
            sheet.setColumnWidth(3, 6000);
            sheet.setColumnWidth(4, 6000);

            // ========================================================
            // MUTUAL EXCLUSION VALIDATION (PASSED vs FAILED)
            // ========================================================
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();

            // Constraint for PASSED column (Index 3) -> Only allow input if FAILED column
            // (E) is blank
            CellRangeAddressList passedRange = new CellRangeAddressList(1, 1000, 3, 3);
            DataValidationConstraint passedConstraint = validationHelper.createCustomConstraint("=ISBLANK(E2)");
            DataValidation passedValidation = validationHelper.createValidation(passedConstraint, passedRange);
            passedValidation.setShowErrorBox(true);
            passedValidation.createErrorBox("Validation Error",
                    "Cannot mark PASSED because FAILED is already marked. Please clear the FAILED column first.");
            sheet.addValidationData(passedValidation);

            // Constraint for FAILED column (Index 4) -> Only allow input if PASSED column
            // (D) is blank
            CellRangeAddressList failedRange = new CellRangeAddressList(1, 1000, 4, 4);
            DataValidationConstraint failedConstraint = validationHelper.createCustomConstraint("=ISBLANK(D2)");
            DataValidation failedValidation = validationHelper.createValidation(failedConstraint, failedRange);
            failedValidation.setShowErrorBox(true);
            failedValidation.createErrorBox("Validation Error",
                    "Cannot mark FAILED because PASSED is already marked. Please clear the PASSED column first.");
            sheet.addValidationData(failedValidation);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==========================================
    // API 2: IMPORT RESULTS FROM EXCEL
    // ==========================================
    @Transactional
    public String importResults(Integer roundId, MultipartFile file) {
        int passedCount = 0;
        int failedCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                Cell idCell = row.getCell(0);
                if (idCell == null || idCell.getCellType() != CellType.NUMERIC)
                    continue;

                Integer roundProjectId = (int) idCell.getNumericCellValue();
                RoundProject rp = roundProjectRepository.findById(roundProjectId).orElse(null);

                if (rp == null || rp.getResultStatus() != RoundProjectStatus.IN_PROGRESS)
                    continue;

                // Read result columns
                Cell passedCell = row.getCell(3);
                Cell failedCell = row.getCell(4);

                boolean isPassed = isMarked(passedCell);
                boolean isFailed = isMarked(failedCell);

                if (isPassed && !isFailed) {
                    rp.setResultStatus(RoundProjectStatus.PASSED);
                    rp.getProject().setStatus(ProjectStatus.COMPLETED);
                    passedCount++;
                } else if (!isPassed && isFailed) {
                    rp.setResultStatus(RoundProjectStatus.FAILED);
                    // Project remains PENDING
                    failedCount++;
                }
            }

            return String.format("Import completed successfully: %d projects PASSED, %d projects FAILED.", passedCount,
                    failedCount);

        } catch (IOException e) {
            log.error("Failed to parse grading excel file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to parse the uploaded Excel file.");
        }
    }

    private boolean isMarked(Cell cell) {
        if (cell == null)
            return false;
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            return !val.isEmpty();
        }
        return false;
    }

    @Transactional
    public void checkAndCompleteRounds() {
        List<DefenseRound> expiredRounds = defenseRoundRepository.findRoundsReadyToComplete();

        for (DefenseRound round : expiredRounds) {
            // Kiểm tra xem còn đề tài nào chưa có kết quả (IN_PROGRESS) không
            boolean hasPendingResults = roundProjectRepository.existsByDefenseRound_RoundIdAndResultStatus(
                    round.getRoundId(), RoundProjectStatus.IN_PROGRESS);

            if (!hasPendingResults) {
                round.setStatus(RoundStatus.COMPLETED);
                defenseRoundRepository.save(round);
                log.info("Defense Round ID {} has been marked as COMPLETED.", round.getRoundId());
            } else {
                log.warn("Defense Round ID {} has passed its end date but still has ungraded projects.",
                        round.getRoundId());
            }
        }
    }
}