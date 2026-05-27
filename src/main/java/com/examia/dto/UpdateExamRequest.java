package com.examia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para actualizar un examen existente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {

    /**
     * Título del examen
     */
    @NotBlank(message = "El título del examen es obligatorio")
    private String title;

    /**
     * Descripción o instrucciones del examen
     */
    private String description;

    /**
     * ID de la materia/clase a la que pertenece el examen
     */
    private String subjectId;

    /**
     * Nombre de la materia
     */
    private String subjectName;

    /**
     * Lista de preguntas del examen (reemplaza todas las existentes)
     */
    @Valid
    private List<QuestionRequest> questions;

    /**
     * Duración del examen en minutos
     */
    @Positive(message = "La duración debe ser positiva")
    private Integer durationMinutes;

    /**
     * Puntaje mínimo para aprobar
     */
    @Positive(message = "El puntaje para aprobar debe ser positivo")
    private Double passingScore;

    /**
     * Fecha y hora de inicio programada
     */
    private LocalDateTime scheduledStartTime;

    /**
     * Fecha y hora de fin programada
     */
    private LocalDateTime scheduledEndTime;

    /**
     * Indica si el examen está publicado
     */
    private Boolean published;

    /**
     * Indica si las preguntas deben mostrarse en orden aleatorio
     */
    private Boolean shuffleQuestions;

    /**
     * Indica si las opciones deben mostrarse en orden aleatorio
     */
    private Boolean shuffleOptions;

    /**
     * Indica si se muestran los resultados al finalizar
     */
    private Boolean showResultsOnCompletion;

    /**
     * Número máximo de intentos permitidos
     */
    @Positive(message = "El número de intentos debe ser positivo")
    private Integer maxAttempts;

    /**
     * Turno del examen (1 = Mañana, 2 = Tarde, 3 = Noche)
     */
    private String shift;
}

