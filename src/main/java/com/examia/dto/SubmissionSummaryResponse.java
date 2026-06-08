package com.examia.dto;

import com.examia.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionSummaryResponse {

    private String id;
    private String examId;
    private String examTitle;
    private String studentId;
    private String studentName;
    private String studentLegajo;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private Double totalScore;
    private Double totalPoints;
    private int violationCount;
    private Integer timeTakenSeconds;
}
