package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un examen en el sistema Examia.
 * Un examen pertenece a un profesor y a una materia/clase.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "exams")
public class Exam {

    @Id
    private String id;

    /**
     * Título del examen
     */
    private String title;

    /**
     * Descripción o instrucciones del examen
     */
    private String description;

    /**
     * ID del profesor que creó el examen
     */
    @Indexed
    private String professorId;

    /**
     * ID de la materia/clase a la que pertenece el examen
     */
    @Indexed
    private String subjectId;

    /**
     * Nombre de la materia (desnormalizado para consultas rápidas)
     */
    private String subjectName;

    /**
     * Lista de preguntas del examen
     */
    private List<Question> questions;

    /**
     * Duración del examen en minutos (opcional)
     */
    private Integer durationMinutes;

    /**
     * Puntaje total del examen (suma de puntos de todas las preguntas)
     */
    private Double totalPoints;

    /**
     * Puntaje mínimo para aprobar (opcional)
     */
    private Double passingScore;

    /**
     * Fecha y hora de inicio programada (opcional)
     */
    private LocalDateTime scheduledStartTime;

    /**
     * Fecha y hora de fin programada (opcional)
     */
    private LocalDateTime scheduledEndTime;

    /**
     * Turno del examen (1 = Mañana, 2 = Tarde, 3 = Noche)
     */
    private String shift;

    /**
     * Indica si el examen está publicado y disponible para los alumnos
     */
    @Builder.Default
    private boolean published = false;

    /**
     * Indica si las preguntas deben mostrarse en orden aleatorio
     */
    @Builder.Default
    private boolean shuffleQuestions = false;

    /**
     * Indica si las opciones de las preguntas deben mostrarse en orden aleatorio
     */
    @Builder.Default
    private boolean shuffleOptions = false;

    /**
     * Indica si se muestran los resultados al finalizar
     */
    @Builder.Default
    private boolean showResultsOnCompletion = true;

    /**
     * Número máximo de intentos permitidos (null = ilimitado)
     */
    private Integer maxAttempts;

    /**
     * Indica si el examen está activo (borrado lógico)
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Fecha de creación
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Fecha de última modificación
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Fecha de eliminación lógica
     */
    private LocalDateTime deletedAt;
}

