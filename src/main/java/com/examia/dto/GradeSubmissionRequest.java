package com.examia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmissionRequest {

    @NotEmpty(message = "Debe incluir calificaciones para todas las preguntas")
    @Valid
    private List<QuestionGradeRequest> questionGrades;

    @NotNull(message = "El puntaje total es obligatorio")
    @PositiveOrZero(message = "El puntaje total no puede ser negativo")
    private Double totalScore;

    private String teacherFeedback;
}
