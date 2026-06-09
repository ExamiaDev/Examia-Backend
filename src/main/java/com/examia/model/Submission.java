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
import java.util.ArrayList;
import java.util.List;

/**
 * Entrega de un alumno para un examen.
 * Colección independiente en MongoDB (no embebida en Exam).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "submissions")
public class Submission {

    @Id
    private String id;

    /** ID del examen al que corresponde esta entrega */
    @Indexed
    private String examId;

    /** ID del alumno que realizó la entrega */
    @Indexed
    private String studentId;

    /** ID del profesor (desnormalizado para queries eficientes) */
    @Indexed
    private String professorId;

    /** ID de la materia (desnormalizado) */
    private String subjectId;

    /** Respuestas del alumno, una por pregunta */
    @Builder.Default
    private List<StudentAnswer> answers = new ArrayList<>();

    /** Estado actual de la entrega */
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    /** Puntaje total asignado por el docente */
    private Double totalScore;

    /** Comentario general del docente */
    private String teacherFeedback;

    /** Infracciones de vigilancia detectadas durante el examen */
    @Builder.Default
    private List<ProctoringViolation> violations = new ArrayList<>();

    /** Tiempo que tardó el alumno en completar el examen, en segundos */
    private Integer timeTakenSeconds;

    /** Borrado lógico */
    @Builder.Default
    private boolean active = true;

    /** Fecha de entrega (seteada automáticamente al crear) */
    @CreatedDate
    private LocalDateTime submittedAt;

    /** Fecha de última modificación */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /** Fecha en que el docente completó la corrección */
    private LocalDateTime gradedAt;
}
