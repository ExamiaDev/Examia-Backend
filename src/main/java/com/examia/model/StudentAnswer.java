package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Respuesta de un alumno a una pregunta específica del examen.
 * Clase embebida dentro de Submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {

    /** ID de la pregunta respondida (referencia a Question.id dentro del Exam) */
    private String questionId;

    /** Opciones seleccionadas (MULTIPLE_CHOICE, MULTIPLE_SELECTION, TRUE_FALSE) */
    private List<Integer> selectedOptions;

    /** Respuesta en texto libre (LONG_ANSWER, SHORT_ANSWER, FILL_IN_THE_BLANK) */
    private String textAnswer;

    /** Orden ingresado por el alumno (ORDERING) */
    private List<String> orderAnswer;

    /** Pares ingresados por el alumno (MATCHING) */
    private Map<String, String> matchingAnswer;

    /** Árbol armado por el alumno (DECISION_TREE) */
    private DecisionTreeDefinition decisionTree;

    /** Tabla armada por el alumno (MATRIX) */
    private List<String> matrixColumnHeaders;

    private List<List<String>> matrixRows;

    /** Puntaje obtenido en esta pregunta (asignado por el docente o auto-corrección) */
    private Double earnedScore;

    /** Comentario del docente sobre esta respuesta */
    private String teacherFeedback;
}
