package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.entity.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectImportService {

    private final ProjectRepository projectRepository;
    private final RoundProjectRepository roundProjectRepository;
    private final ProjectSupervisorRepository projectSupervisorRepository;
    private final DefenseRoundRepository roundRepository;
    private final LecturerRepository lecturerRepository;

    private final TransactionTemplate transactionTemplate;

    //  API TẢI FILE MẪU
    public InputStream getExcelTemplate() throws IOException {
        Resource resource = new ClassPathResource("templates/Project_Import_Template.xlsx");

        if (!resource.exists()) {
            throw new IOException("The template file does not exist on the server!");
        }
        return resource.getInputStream();
    }

    //  API IMPORT PROJECT
    public ImportResultResponse importProjects(MultipartFile file, Integer roundId) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Defense Round ID " + roundId + " not found"));

        Semester semester = round.getSemester();

        Map<String, Lecturer> lecturerMap = lecturerRepository.findByIsActiveTrue().stream()
                .collect(Collectors.toMap(
                        l -> l.getLecturerCode().toUpperCase().trim(),
                        l -> l
                ));

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Bắt đầu đọc từ dòng 2 (Index 1) vì dòng 1 là Header
            // Lưu ý: getLastRowNum() trả về index dòng cuối cùng (0-based)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;

                try {

                    transactionTemplate.executeWithoutResult(status -> {
                        try {
                            processSingleRow(row, round, semester, lecturerMap);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });

                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    String errorMsg = "Row " + rowNum + ": " + e.getMessage();
                    log.error(errorMsg); // Log để debug
                    errors.add(errorMsg);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }

        return ImportResultResponse.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .errorDetails(errors)
                .build();
    }

    // LOGIC XỬ LÝ 1 DÒNG
    private void processSingleRow(Row row, DefenseRound round, Semester semester,
                                  Map<String, Lecturer> lecturerMap) throws Exception {

        // Giả định Format Excel:
        // Col A (0): STT (Bỏ qua)
        // Col B (1): Title (Required)
        // Col C (2): Major (Optional)
        // Col D (3): Supervisor Code (Required)

        String title = getCellValue(row, 1, true);
        String major = getCellValue(row, 2, false);
        String supCode = getCellValue(row, 3, true);

        Lecturer supervisor = lecturerMap.get(supCode.toUpperCase());
        if (supervisor == null) {
            throw new Exception("Supervisor with code '" + supCode + "' not found or inactive.");
        }
        Project project = Project.builder()
                .title(title)
                .major(major)
                .semester(semester)
                .status("ACTIVE")
                .build();

        project = projectRepository.save(project);

        ProjectSupervisor ps = ProjectSupervisor.builder()
                .project(project)
                .lecturer(supervisor)
                .roleType("MAIN")
                .build();

        projectSupervisorRepository.save(ps);

        RoundProject rp = RoundProject.builder()
                .project(project)
                .defenseRound(round)
                .resultStatus("IN_PROGRESS")
                .roundBlock(null)
                .build();

        roundProjectRepository.save(rp);
    }

    private String getCellValue(Row row, int index, boolean required) throws Exception {
        Cell cell = row.getCell(index);
        String val = "";

        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    val = cell.getStringCellValue().trim();
                    break;
                case NUMERIC:
                    val = String.valueOf((int) cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    val = String.valueOf(cell.getBooleanCellValue());
                    break;
                default:
                    val = "";
            }
        }

        if (required && val.isEmpty()) {
            throw new Exception("Missing required value at column " + (index + 1));
        }
        return val;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}