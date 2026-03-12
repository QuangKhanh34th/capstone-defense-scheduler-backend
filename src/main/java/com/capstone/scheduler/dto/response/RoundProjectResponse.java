package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoundProjectResponse {
    private Integer roundProjectId;
    private Integer projectId;
    private String projectTitle;

    // Gồm cả 2 status để FE tiện hiển thị màu sắc
    private String projectStatus;       // Trạng thái gốc (PENDING, COMPLETED)
    private String roundProjectStatus;  // Kết quả đợt (IN_PROGRESS, PASSED, FAILED)

    private String major;
    private String supervisorName;      // Tên giảng viên hướng dẫn
}