package com.examia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGradeRequest {

    @NotBlank(message = "El ID de la pregunta es obligatorio")
    private String questionId;

    @NotNull(message = "El puntaje es obligatorio")
    @PositiveOrZero(message = "El puntaje no puede ser negativo")
    private Double score;

    private String feedback;
}
