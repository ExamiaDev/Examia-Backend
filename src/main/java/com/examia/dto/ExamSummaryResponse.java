package com.examia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta resumida de un examen (sin preguntas).
 * Útil para listar exámenes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryResponse {

    private String id;
    private String title;
    private String description;
    private String professorId;
    private String subjectId;
    private String subjectName;
    private int questionCount;
    private Integer durationMinutes;
    private Double totalPoints;
    private Double passingScore;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String shift;
    private boolean published;
    private long pendingCorrectionsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

