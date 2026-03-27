package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AssignProjectsToBlockRequest;
import com.capstone.scheduler.dto.response.BlockProjectResponse;
import com.capstone.scheduler.dto.response.CouncilBlockDetailResponse;
import com.capstone.scheduler.dto.response.CouncilBlockResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundProjectStatus;
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouncilBlockService {

    private final DefenseDayRepository defenseDayRepository;
    private final RoundProjectRepository roundProjectRepository;
    private final CouncilBlockRepository councilBlockRepository;
    private final RoundBlockRepository roundBlockRepository;
    private final CouncilBlockAssignmentRepository  councilBlockAssignmentRepository;


    private static final int MAX_PROJECTS = 7;
    private static final int MINUTES_PER_PROJECT = 90;
    private static final int MINUTES_BREAK = 10;
    private static final LocalTime START_TIME_DEFAULT = LocalTime.of(7, 30);

    // AUTO CREATE BLOCKS
    @Transactional
    public List<CouncilBlockResponse> autoCreateBlocksForDay(Integer dayId, Integer numberOfBlocks) {

        // 1. Validate đầu vào
        if (numberOfBlocks == null || numberOfBlocks < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of blocks (rooms) must be at least 1.");
        }

        DefenseDay day = defenseDayRepository.findById(dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Day not found with ID: " + dayId));

        Integer roundId = day.getDefenseRound().getRoundId();

        List<RoundProject> unassignedProjects = roundProjectRepository.findUnassignedPendingProjects(
                roundId,
                List.of(ProjectStatus.PENDING),
                RoundProjectStatus.IN_PROGRESS
        );

        if (unassignedProjects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No PENDING projects found in this Round to schedule.");
        }

        // 2. Tính toán sức chứa của ngày hôm nay
        int maxCapacityForDay = numberOfBlocks * MAX_PROJECTS;

        // Cắt danh sách dự án nếu vượt quá sức chứa của số phòng FE gửi lên
        List<RoundProject> projectsToProcess = unassignedProjects;
        if (unassignedProjects.size() > maxCapacityForDay) {
            projectsToProcess = unassignedProjects.subList(0, maxCapacityForDay);
        }

        int totalProjects = projectsToProcess.size();

        // 3. Thuật toán chia đều (Tránh tình trạng phòng chấm 7 nhóm, phòng chấm 1 nhóm)
        int baseSize = totalProjects / numberOfBlocks;
        int remainder = totalProjects % numberOfBlocks;

        List<CouncilBlockResponse> responses = new ArrayList<>();
        int currentCount = councilBlockRepository.countByDefenseDay_DayId(dayId);
        int startIndex = 0;

        // 4. Lặp ĐÚNG bằng số lượng phòng FE yêu cầu
        for (int i = 0; i < numberOfBlocks; i++) {

            // Tính số lượng dự án cho phòng này (cộng thêm 1 cho những phòng đầu tiên nếu có dư)
            int batchSize = baseSize + (i < remainder ? 1 : 0);

            // Nếu số lượng dự án ít hơn số phòng (VD: 2 dự án nhưng FE đòi mở 4 phòng), ta bỏ qua việc tạo phòng trống.
            if (batchSize == 0) continue;

            List<RoundProject> batch = projectsToProcess.subList(startIndex, startIndex + batchSize);
            startIndex += batchSize;
            currentCount++;

            LocalTime startTime = START_TIME_DEFAULT;
            LocalTime endTime = calculateEndTime(startTime, batchSize);

            // Tạo Phòng (CouncilBlock)
            CouncilBlock councilBlock = CouncilBlock.builder()
                    .defenseDay(day)
                    .blockName("Council " + currentCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .expectedProjectCount(batchSize)
                    .build();
            councilBlock = councilBlockRepository.save(councilBlock);

            // Tạo Kíp/Slot (RoundBlock) và nhét Đề tài vào
            List<RoundBlock> newSlots = new ArrayList<>();
            for (RoundProject rp : batch) {

                RoundBlock slot = RoundBlock.builder()
                        .councilBlock(councilBlock)
                        .roundProject(rp)
                        .build();
                newSlots.add(slot);
            }
            roundBlockRepository.saveAll(newSlots);

            responses.add(CouncilBlockResponse.builder()
                    .blockId(councilBlock.getBlockId())
                    .blockName(councilBlock.getBlockName())
                    .projectCount(batchSize)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());
        }

        return responses;
    }

    //  MANUAL ASSIGN
    @Transactional
    public List<BlockProjectResponse> assignProjectsToBlock(Integer blockId, AssignProjectsToBlockRequest request) {

        CouncilBlock councilBlock = councilBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Council Block not found"));

        List<RoundProject> projectsToAssign = roundProjectRepository.findAllById(request.getProjectIds());

        if (projectsToAssign.size() != request.getProjectIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some Project IDs are invalid");
        }

        Integer roundIdOfBlock = councilBlock.getDefenseDay().getDefenseRound().getRoundId();

        int currentCount = councilBlock.getRoundBlocks() != null ? councilBlock.getRoundBlocks().size() : 0;
        int newCount = currentCount;

        for (RoundProject rp : projectsToAssign) {
            if (!rp.getDefenseRound().getRoundId().equals(roundIdOfBlock)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project ID " + rp.getProject().getProjectId() + " belongs to a different Round.");
            }

            if (rp.getProject().getStatus() != ProjectStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project '" + rp.getProject().getTitle() + "' is " + rp.getProject().getStatus() + ". Only PENDING projects can be assigned.");
            }

            boolean alreadyInBlock = councilBlock.getRoundBlocks() != null && councilBlock.getRoundBlocks().stream()
                    .anyMatch(rb -> rb.getRoundProject() != null && rb.getRoundProject().getRoundProjectId().equals(rp.getRoundProjectId()));
            if (!alreadyInBlock) {
                newCount++;
            }
        }

        if (newCount > MAX_PROJECTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Block capacity exceeded! Max is " + MAX_PROJECTS + ". Current: " + currentCount + ", Adding: " + (newCount - currentCount));
        }

        List<RoundBlock> newSlots = new ArrayList<>();
        for (RoundProject rp : projectsToAssign) {
            rp.setResultStatus(RoundProjectStatus.IN_PROGRESS);

            if (rp.getRoundBlocks() != null && !rp.getRoundBlocks().isEmpty()) {
                roundBlockRepository.deleteAll(rp.getRoundBlocks());
            }

            RoundBlock slot = RoundBlock.builder()
                    .councilBlock(councilBlock)
                    .roundProject(rp)
                    .build();
            newSlots.add(slot);
        }
        roundProjectRepository.saveAll(projectsToAssign);
        roundBlockRepository.saveAll(newSlots);

        LocalTime newEndTime = calculateEndTime(councilBlock.getStartTime(), newCount);
        councilBlock.setEndTime(newEndTime);
        councilBlock.setExpectedProjectCount(newCount);
        councilBlockRepository.save(councilBlock);

        return projectsToAssign.stream()
                .map(rp -> mapProjectToDto(rp.getProject()))
                .toList();
    }

    // GET BLOCKS
    @Transactional(readOnly = true)
    public List<CouncilBlockDetailResponse> getBlocksByDayId(Integer dayId) {
        if (!defenseDayRepository.existsById(dayId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Day not found with ID: " + dayId);
        }
        List<CouncilBlock> blocks = councilBlockRepository.findByDefenseDay_DayIdOrderByBlockIdAsc(dayId);
        return blocks.stream().map(this::mapToDetailResponse).toList();
    }

    // GET PROJECTS IN BLOCK
    @Transactional(readOnly = true)
    public List<BlockProjectResponse> getProjectsInBlock(Integer blockId) {
        CouncilBlock councilBlock = councilBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Council Block not found"));

        if (councilBlock.getRoundBlocks() == null) {
            return new ArrayList<>();
        }

        return councilBlock.getRoundBlocks().stream()
                .filter(rb -> rb.getRoundProject() != null)
                .map(rb -> mapProjectToDto(rb.getRoundProject().getProject()))
                .collect(Collectors.toList());
    }

    private LocalTime calculateEndTime(LocalTime start, int projectCount) {
        if (projectCount <= 0) return start;
        int totalDefenseMinutes = projectCount * MINUTES_PER_PROJECT;
        int totalBreakMinutes = (projectCount - 1) * MINUTES_BREAK;
        if (totalBreakMinutes < 0) totalBreakMinutes = 0;
        return start.plusMinutes(totalDefenseMinutes + totalBreakMinutes);
    }

    private CouncilBlockDetailResponse mapToDetailResponse(CouncilBlock block) {
        List<BlockProjectResponse> projectDtos = new ArrayList<>();
        // MỚI: Map 1-1 từ Slot sang Project
        if (block.getRoundBlocks() != null) {
            for (RoundBlock rb : block.getRoundBlocks()) {
                if (rb.getRoundProject() != null) {
                    projectDtos.add(mapProjectToDto(rb.getRoundProject().getProject()));
                }
            }
        }
        return CouncilBlockDetailResponse.builder()
                .blockId(block.getBlockId())
                .blockName(block.getBlockName())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .currentProjectCount(projectDtos.size())
                .projects(projectDtos)
                .build();
    }

    private BlockProjectResponse mapProjectToDto(Project project) {
        String supervisorName = "N/A";
        if (project.getProjectSupervisors() != null) {
            supervisorName = project.getProjectSupervisors().stream()
                    .filter(ps -> "MAIN".equals(ps.getRoleType()))
                    .map(ps -> ps.getLecturer().getFullName())
                    .findFirst()
                    .orElse("N/A");
        }
        return BlockProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .major(project.getMajor())
                .supervisorName(supervisorName)
                .build();
    }

    // Nhớ inject thêm councilBlockAssignmentRepository và roundBlockRepository nếu chưa có nhé

    @Transactional
    public void deleteBlocksByDayId(Integer dayId) {
        // 1. Kiểm tra Ngày bảo vệ
        DefenseDay day = defenseDayRepository.findById(dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Day not found with ID: " + dayId));

        // Lấy danh sách các Block của ngày hôm đó
        List<CouncilBlock> blocks = councilBlockRepository.findByDefenseDay_DayIdOrderByStartTime(dayId);

        if (blocks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No blocks found for this day to delete.");
        }

        // Lấy ra danh sách các ID của Block để query cho lẹ
        List<Integer> blockIds = blocks.stream()
                .map(CouncilBlock::getBlockId)
                .toList();

        // 2. DỌN DẸP GIẢNG VIÊN: Xóa các phân công (Assignments) để tránh lỗi Khóa ngoại (Foreign Key)
        councilBlockAssignmentRepository.deleteByCouncilBlock_BlockIdIn(blockIds);

        // 3. GIẢI PHÓNG ĐỀ TÀI: Xóa liên kết RoundBlock
        // Đề tài (RoundProject) vẫn giữ nguyên, nhưng nó sẽ mất liên kết với phòng và trở về list Unassigned
        roundBlockRepository.deleteByCouncilBlock_BlockIdIn(blockIds);

        // 4. XÓA BLOCK: Cuối cùng mới xóa các phòng này
        councilBlockRepository.deleteAll(blocks);

        log.info("Successfully deleted {} blocks and released all associated projects/lecturers for Day ID: {}", blocks.size(), dayId);
    }
}