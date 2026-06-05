package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Respuesta de un alumno a una pregunta específica del examen.
 * Clase embebida dentro de Submission.
 */
@Data
@Builder
@NoArgsConstructor(onConstructor_ = {@PersistenceCreator})
@AllArgsConstructor
public class StudentAnswer {

    /** ID de la pregunta respondida (referencia a Question.id dentro del Exam) */
    private String questionId;

    /** Opciones seleccionadas (MULTIPLE_CHOICE, MULTIPLE_SELECTION, TRUE_FALSE) */
    @Builder.Default
    private List<Integer> selectedOptions = new ArrayList<>();

    /** Respuesta en texto libre (LONG_ANSWER, SHORT_ANSWER, FILL_IN_THE_BLANK) */
    private String textAnswer;

    /** Orden ingresado por el alumno (ORDERING) */
    @Builder.Default
    private List<String> orderAnswer = new ArrayList<>();

    /** Pares ingresados por el alumno (MATCHING) */
    private Map<String, String> matchingAnswer;

    /** Árbol armado por el alumno (DECISION_TREE) */
    private DecisionTreeDefinition decisionTree;

    /** Tabla armada por el alumno (MATRIX) */
    @Builder.Default
    private List<String> matrixColumnHeaders = new ArrayList<>();

    @Builder.Default
    private List<ArrayList<String>> matrixRows = new ArrayList<>();

    /** Puntaje obtenido en esta pregunta (asignado por el docente o auto-corrección) */
    private Double earnedScore;

    /** Comentario del docente sobre esta respuesta */
    private String teacherFeedback;
}
