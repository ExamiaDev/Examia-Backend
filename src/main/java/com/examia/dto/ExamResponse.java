package com.examia.dto;

import com.examia.model.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta de un examen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private String id;
    private String title;
    private String description;
    private String professorId;
    private String subjectId;
    private String subjectName;
    private List<Question> questions;
    private Integer durationMinutes;
    private Double totalPoints;
    private Double passingScore;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String shift;
    private boolean published;
    private boolean shuffleQuestions;
    private boolean shuffleOptions;
    private boolean showResultsOnCompletion;
    private Integer maxAttempts;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}

