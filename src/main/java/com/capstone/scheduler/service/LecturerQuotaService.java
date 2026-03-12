package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.SetQuotaRequest;
import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.LecturerQuota;
import com.capstone.scheduler.enums.CommonStatus;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.LecturerQuotaRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerQuotaService {

    private final LecturerQuotaRepository quotaRepository;
    private final DefenseRoundRepository roundRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional
    public List<String> setLecturerQuotas(Integer roundId, List<SetQuotaRequest> requests) {

        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        List<String> results = new ArrayList<>();
        List<LecturerQuota> toSave = new ArrayList<>();

        for (SetQuotaRequest req : requests) {

            int min = (req.getMinCouncil() != null) ? req.getMinCouncil() : 0;
            int max = (req.getMaxCouncil() != null) ? req.getMaxCouncil() : 7;

            if (min > max) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lecturer ID " + req.getLecturerId() + ": Min (" + min + ") cannot be greater than Max (" + max + ")");
            }

            LecturerQuota quota = quotaRepository.findByLecturer_LecturerIdAndDefenseRound_RoundId(req.getLecturerId(), roundId)
                    .orElse(null);

            if (quota == null) {

                Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Lecturer not found with ID: " + req.getLecturerId()));

                quota = LecturerQuota.builder()
                        .defenseRound(round)
                        .lecturer(lecturer)
                        .minCouncil(min)
                        .maxCouncil(max)
                        .build();

                results.add("Created quota for Lecturer ID " + req.getLecturerId());
            } else {

                quota.setMinCouncil(min);
                quota.setMaxCouncil(max);

                results.add("Updated quota for Lecturer ID " + req.getLecturerId());
            }

            toSave.add(quota);
        }

        quotaRepository.saveAll(toSave);

        return results;
    }
    // =========================================================================
    // 1. XUẤT FILE EXCEL TEMPLATE ĐỘNG (Đã điền sẵn danh sách GV Active)
    // =========================================================================
    @Transactional(readOnly = true)
    public byte[] generateDynamicQuotaTemplate() throws IOException {
        // Lấy tất cả GV đang đi làm
        List<Lecturer> activeLecturers = lecturerRepository.findByStatus(CommonStatus.ACTIVE);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Quotas");

            // Tạo Header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Lecturer ID (DO NOT EDIT)");
            headerRow.createCell(1).setCellValue("Lecturer Code");
            headerRow.createCell(2).setCellValue("Full Name");
            headerRow.createCell(3).setCellValue("Min Council");
            headerRow.createCell(4).setCellValue("Max Council");

            // Style Header (bôi vàng cho nổi bật)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i <= 4; i++) headerRow.getCell(i).setCellStyle(headerStyle);

            // Điền Data
            int rowIdx = 1;
            for (Lecturer l : activeLecturers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getLecturerId());
                row.createCell(1).setCellValue(l.getLecturerCode());
                row.createCell(2).setCellValue(l.getFullName());
                row.createCell(3).setCellValue(0); // Default Min
                row.createCell(4).setCellValue(7); // Default Max
            }

            // Auto-size các cột cho đẹp
            for (int i = 0; i <= 4; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // 2. NHẬP FILE EXCEL QUOTA VÀO DATABASE
    // =========================================================================
    @Transactional
    public ImportResultResponse importQuotasFromExcel(MultipartFile file, Integer roundId) {
        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Round not found"));

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        List<LecturerQuota> toSave = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Duyệt từ dòng 1 (bỏ qua dòng Header 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Cột 0: Lecturer ID (Đảm bảo chính xác 100% mapping)
                    Cell idCell = row.getCell(0);
                    if (idCell == null || idCell.getCellType() == CellType.BLANK) continue;
                    Integer lecturerId = (int) idCell.getNumericCellValue();

                    // Cột 3: Min, Cột 4: Max
                    int min = (int) row.getCell(3).getNumericCellValue();
                    int max = (int) row.getCell(4).getNumericCellValue();

                    if (min > max) throw new Exception("Min > Max");

                    // Bắt buộc Giảng viên phải tồn tại
                    Lecturer lecturer = lecturerRepository.findById(lecturerId)
                            .orElseThrow(() -> new Exception("Lecturer ID " + lecturerId + " not found in DB"));

                    // Tìm xem đã có Quota chưa (Upsert)
                    LecturerQuota quota = quotaRepository.findByLecturer_LecturerIdAndDefenseRound_RoundId(lecturerId, roundId)
                            .orElse(LecturerQuota.builder().lecturer(lecturer).defenseRound(round).build());

                    quota.setMinCouncil(min);
                    quota.setMaxCouncil(max);
                    toSave.add(quota);

                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

            quotaRepository.saveAll(toSave);

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel: " + e.getMessage());
        }

        return ImportResultResponse.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .errorDetails(errors)
                .build();
    }
}