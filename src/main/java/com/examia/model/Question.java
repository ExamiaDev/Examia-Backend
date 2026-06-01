package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Clase embebida que representa una pregunta dentro de un examen.
 * No es un documento independiente, se almacena dentro del documento Exam.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    /**
     * Identificador único de la pregunta dentro del examen
     */
    private String id;

    /**
     * Tipo de pregunta (MULTIPLE_CHOICE, TRUE_FALSE, etc.)
     */
    private QuestionType type;

    /**
     * Texto de la pregunta
     */
    private String text;

    /**
     * Opciones disponibles para preguntas de opción múltiple,
     * selección múltiple, verdadero/falso, etc.
     */
    private List<String> options;

    /**
     * Índice(s) de la(s) respuesta(s) correcta(s).
     * Para MULTIPLE_CHOICE: un solo índice
     * Para MULTIPLE_SELECTION: varios índices
     * Para TRUE_FALSE: 0 = Verdadero, 1 = Falso
     */
    private List<Integer> correctAnswers;

    /**
     * Respuesta correcta para preguntas de tipo texto (SHORT_ANSWER, FILL_IN_THE_BLANK)
     */
    private String correctTextAnswer;

    /**
     * Pares de relacionamiento para preguntas tipo MATCHING
     * Key: elemento izquierdo, Value: elemento derecho correspondiente
     */
    private Map<String, String> matchingPairs;

    /**
     * Orden correcto para preguntas de tipo ORDERING
     */
    private List<String> correctOrder;

    /**
     * Puntos asignados a esta pregunta
     */
    private Double points;

    /**
     * Explicación o retroalimentación de la respuesta correcta
     */
    private String explanation;

    /**
     * URL de imagen asociada a la pregunta (opcional)
     */
    private String imageUrl;

    /**
     * Tema o sección a la que pertenece la pregunta dentro del examen
     */
    private String topic;

    /**
     * Color hexadecimal asignado al tema (ej. "#1565c0")
     */
    private String topicColor;

    /**
     * Orden de la pregunta en el examen
     */
    private Integer order;

    /**
     * Indica si la pregunta es obligatoria
     */
    @Builder.Default
    private boolean required = true;
}

