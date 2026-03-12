package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.util.PasswordGenerator;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.CommonStatus; // IMPORT ENUM
import com.capstone.scheduler.enums.UserRole;
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public InputStream getExcelTemplate() throws IOException {
        Resource resource = new ClassPathResource("templates/Lecturer_Import_Template.xlsx");
        if (!resource.exists()) throw new IOException("Template not found!");
        return resource.getInputStream();
    }

    public ImportResultResponse importLecturers(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");

        Map<String, Department> deptMap = new HashMap<>();
        departmentRepository.findAll().forEach(d -> deptMap.put(d.getName().toUpperCase(), d));

        Map<String, CouncilRole> roleMap = new HashMap<>();
        councilRoleRepository.findAll().forEach(r -> roleMap.put(r.getRoleCode(), r));

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                int rowNum = i + 1;
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        try {
                            processSingleRow(row, deptMap, roleMap);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });
                    successCount++;
                } catch (Exception e) {
                    if (e.getMessage().contains("SKIPPED")) {
                        skippedCount++;
                        errors.add("Row " + rowNum + " (Skipped): " + e.getMessage().replace("java.lang.RuntimeException: ", ""));
                    } else {
                        errors.add("Row " + rowNum + " (Failed): " + e.getMessage().replace("java.lang.RuntimeException: ", ""));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel: " + e.getMessage());
        }

        return ImportResultResponse.builder()
                .successCount(successCount)
                .failureCount(skippedCount + errors.size() - skippedCount)
                .errorDetails(errors)
                .build();
    }

    private void processSingleRow(Row row, Map<String, Department> deptMap, Map<String, CouncilRole> roleMap) throws Exception {
        String code = getCellValue(row, 3, true);

        if (lecturerRepository.existsByLecturerCode(code)) {
            throw new Exception("SKIPPED - Lecturer Code '" + code + "' already exists.");
        }

        String fullName = getCellValue(row, 1, true);
        String email = getCellValue(row, 2, true);
        String phone = getCellValue(row, 4, false);
        String deptName = getCellValue(row, 5, true);

        if (!deptMap.containsKey(deptName.toUpperCase())) {
            throw new Exception("Department '" + deptName + "' not found.");
        }

        User user = userRepository.findByUsername(email).orElse(new User());
        if (user.getUserId() == null) {
            String rawPassword = PasswordGenerator.generate(10);
            log.info("Import: Generated password for {}: {}", email, rawPassword);

            user.setUsername(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setRole(UserRole.LECTURER);
            user.setStatus(CommonStatus.ACTIVE);
            user = userRepository.save(user);
        }

        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setDepartment(deptMap.get(deptName.toUpperCase()));
        lecturer.setLecturerCode(code);
        lecturer.setFullName(fullName);
        lecturer.setEmail(email);
        lecturer.setPhone(phone);
        lecturer.setStatus(CommonStatus.ACTIVE);
        lecturer = lecturerRepository.save(lecturer);

        saveCompetency(lecturer, roleMap.get("PRESIDENT"), getNumericValue(row, 6));
        saveCompetency(lecturer, roleMap.get("SECRETARY"), getNumericValue(row, 7));
        saveCompetency(lecturer, roleMap.get("BUSINESS"), getNumericValue(row, 8));
        saveCompetency(lecturer, roleMap.get("TECH"), getNumericValue(row, 9));
        saveCompetency(lecturer, roleMap.get("AI"), getNumericValue(row, 10));
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