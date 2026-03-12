package com.capstone.scheduler.service;

import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Project;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundStatus;
import com.capstone.scheduler.enums.SemesterStatus;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.ProjectRepository;
import com.capstone.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class SystemStatusSchedulerService {

    private final SemesterRepository semesterRepository;
    private final DefenseRoundRepository defenseRoundRepository;
    private final ProjectRepository projectRepository;
    private final DefenseRoundService defenseRoundService;

    /**
     * Tác vụ tổng, chạy vào lúc 00:01 sáng mỗi ngày.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void dailySystemStatusUpdateJob() {
        log.info("========== BẮT ĐẦU JOB CẬP NHẬT TRẠNG THÁI HỆ THỐNG HẰNG NGÀY ==========");
        LocalDate today = LocalDate.now();

        // 1. Cập nhật trạng thái của các Đợt bảo vệ (Defense Round)
        updateDefenseRoundStatuses(today);

        // 2. Cập nhật trạng thái của Học kỳ (Semester) và Đề tài (Project)
        updateSemesterAndProjectStatuses(today);

        log.info("========== KẾT THÚC JOB CẬP NHẬT TRẠNG THÁI ==========");
    }

    private void updateDefenseRoundStatuses(LocalDate today) {
        log.info("--- Đang quét cập nhật Defense Round ---");

        // A. PLANNING -> ON_GOING (Khi tới ngày thi đầu tiên)
        List<DefenseRound> planningRounds = defenseRoundRepository.findByStatus(RoundStatus.PLANNING);
        for (DefenseRound round : planningRounds) {
            if (round.getDefenseDays() != null && !round.getDefenseDays().isEmpty()) {
                // Tìm ngày thì đầu tiên trong danh sách
                LocalDate firstDay = round.getDefenseDays().stream()
                        .map(DefenseDay::getDefenseDate)
                        .min(Comparator.naturalOrder())
                        .orElse(null);

                // Nếu hôm nay đúng bằng ngày thi đầu tiên (hoặc đã lố qua)
                if (firstDay != null && !today.isBefore(firstDay)) {
                    round.setStatus(RoundStatus.ON_GOING);
                    log.info("Round '{}' (ID: {}) chuyển sang ON_GOING.", round.getRoundName(), round.getRoundId());
                }
            }
        }
        defenseRoundRepository.saveAll(planningRounds);

        // B. ON_GOING -> COMPLETED (Khi đã qua ngày thi cuối cùng)
        List<DefenseRound> ongoingRounds = defenseRoundRepository.findByStatus(RoundStatus.ON_GOING);
        for (DefenseRound round : ongoingRounds) {
            if (round.getDefenseDays() != null && !round.getDefenseDays().isEmpty()) {
                // Tìm ngày thi cuối cùng
                LocalDate lastDay = round.getDefenseDays().stream()
                        .map(DefenseDay::getDefenseDate)
                        .max(Comparator.naturalOrder())
                        .orElse(null);

                // Chỉ chuyển sang COMPLETED khi ngày hôm nay ĐÃ VƯỢT QUA ngày thi cuối cùng
                // (Nghĩa là quét vào 00:01 sáng của ngày hôm sau)
                if (lastDay != null && today.isAfter(lastDay)) {
                    round.setStatus(RoundStatus.COMPLETED);
                    log.info("Round '{}' (ID: {}) chuyển sang COMPLETED.", round.getRoundName(), round.getRoundId());
                }
            }
        }
        defenseRoundRepository.saveAll(ongoingRounds);
    }

    private void updateSemesterAndProjectStatuses(LocalDate today) {
        log.info("--- Đang quét cập nhật Semester & Project ---");

        List<Semester> ongoingSemesters = semesterRepository.findByStatus(SemesterStatus.ON_GOING);

        for (Semester semester : ongoingSemesters) {
            // Luật: Semester FINISHED sau khi ngày hiện tại sau end_date
            if (semester.getEndDate() != null && today.isAfter(semester.getEndDate())) {
                semester.setStatus(SemesterStatus.FINISHED);
                log.info("Semester '{}' (ID: {}) chuyển sang FINISHED.", semester.getName(), semester.getSemesterId());

                // Luật dây chuyền: ProjectStatus chuyển sang COMPLETED khi SemesterStatus là FINISHED
                // Tìm tất cả Project PENDING của Học kỳ này và cho "Tốt nghiệp"
                List<Project> pendingProjects = projectRepository.findBySemester_SemesterIdAndStatus(
                        semester.getSemesterId(), ProjectStatus.PENDING);

                for (Project p : pendingProjects) {
                    p.setStatus(ProjectStatus.COMPLETED);
                }

                if (!pendingProjects.isEmpty()) {
                    projectRepository.saveAll(pendingProjects);
                    log.info("Đã chuyển {} Project PENDING sang COMPLETED cho Semester {}.",
                            pendingProjects.size(), semester.getSemesterId());
                }
            }
        }
        semesterRepository.saveAll(ongoingSemesters);
    }

    @Scheduled(cron = "0 5 0 * * ?") // Chạy vào 00:05 mỗi sáng (sau job chính 4 phút)
    @Transactional
    public void autoCompleteRoundsJob() {
        log.info("--- START AUTO COMPLETE ROUNDS JOB ---");
        defenseRoundService.checkAndCompleteRounds();
        log.info("--- END AUTO COMPLETE ROUNDS JOB ---");
    }

}
