package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.CommonStatus; // IMPORT ENUM
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LecturerImportService {

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final CouncilRoleRepository councilRoleRepository;
    private final LecturerCompetencyRepository competencyRepository;
    private final LecturerQuotaRepository quotaRepository;
    private final DefenseRoundRepository roundRepository;
    private final TransactionTemplate transactionTemplate;

    public InputStream getExcelTemplate() throws IOException {
        Resource resource = new ClassPathResource("templates/Lecturer_Import_Template.xlsx");
        if (!resource.exists()) throw new IOException("Template not found!");
        return resource.getInputStream();
    }

    public ImportResultResponse importLecturers(MultipartFile file, Integer roundId) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round ID " + roundId + " not found"));

        Map<String, Department> deptMap = new HashMap<>();
        departmentRepository.findAll().forEach(d -> deptMap.put(d.getName().toUpperCase(), d));

        Map<String, CouncilRole> roleMap = new HashMap<>();
        councilRoleRepository.findAll().forEach(r -> roleMap.put(r.getRoleCode(), r));

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                int rowNum = i + 1;
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        try {
                            processSingleRow(row, round, deptMap, roleMap);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel: " + e.getMessage());
        }

        return ImportResultResponse.builder().successCount(successCount).failureCount(failureCount).errorDetails(errors).build();
    }

    private void processSingleRow(Row row, DefenseRound round, Map<String, Department> deptMap, Map<String, CouncilRole> roleMap) throws Exception {
        String fullName = getCellValue(row, 1, true);
        String email = getCellValue(row, 2, true);
        String code = getCellValue(row, 3, true);
        String phone = getCellValue(row, 4, false);
        String deptName = getCellValue(row, 5, true);

        if (!deptMap.containsKey(deptName.toUpperCase())) {
            throw new Exception("Department '" + deptName + "' not found.");
        }

        User user = userRepository.findByUsername(email).orElse(new User());
        if (user.getUserId() == null) {
            user.setUsername(email);
            user.setPasswordHash("123456");
            user.setRole("LECTURER");
            user.setStatus(CommonStatus.ACTIVE); // FIXED
            user = userRepository.save(user);
        }

        Lecturer lecturer = lecturerRepository.findByLecturerCode(code).orElse(new Lecturer());
        lecturer.setUser(user);
        lecturer.setDepartment(deptMap.get(deptName.toUpperCase()));
        lecturer.setLecturerCode(code);
        lecturer.setFullName(fullName);
        lecturer.setEmail(email);
        lecturer.setPhone(phone);
        lecturer.setStatus(CommonStatus.ACTIVE); // FIXED: Thay isActive(true)
        lecturer = lecturerRepository.save(lecturer);

        saveCompetency(lecturer, roleMap.get("PRESIDENT"), getNumericValue(row, 6));
        saveCompetency(lecturer, roleMap.get("SECRETARY"), getNumericValue(row, 7));
        saveCompetency(lecturer, roleMap.get("BUSINESS"), getNumericValue(row, 8));
        saveCompetency(lecturer, roleMap.get("TECH"), getNumericValue(row, 9));
        saveCompetency(lecturer, roleMap.get("AI"), getNumericValue(row, 10));

        int minQuota = (int) getNumericValue(row, 11);
        int maxQuota = (int) getNumericValue(row, 12);
        if (minQuota > maxQuota) throw new Exception("Min Quota > Max Quota");

        LecturerQuota quota = quotaRepository.findByLecturerIdAndRoundId(lecturer.getLecturerId(), round.getRoundId())
                .orElse(LecturerQuota.builder().lecturer(lecturer).defenseRound(round).build());
        quota.setMinCouncil(minQuota);
        quota.setMaxCouncil(maxQuota);
        quotaRepository.save(quota);
    }

    private void saveCompetency(Lecturer lecturer, CouncilRole role, double score) {
        if (role == null) return;
        LecturerCompetency comp = competencyRepository.findByLecturer_LecturerIdAndCouncilRole_RoleId(lecturer.getLecturerId(), role.getRoleId())
                .orElse(LecturerCompetency.builder().lecturer(lecturer).councilRole(role).build());
        comp.setWeight(score);
        competencyRepository.save(comp);
    }

    private String getCellValue(Row row, int index, boolean required) throws Exception {
        Cell cell = row.getCell(index);
        String val = (cell == null) ? "" : cell.toString().trim();
        if (required && val.isEmpty()) throw new Exception("Missing required value at col " + (index + 1));
        return val;
    }

    private double getNumericValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0.0;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (Exception ex) {
                return 0.0;
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) return false;
        }
        return true;
    }
}