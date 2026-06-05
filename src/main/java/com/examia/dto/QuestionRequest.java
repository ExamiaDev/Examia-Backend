package com.examia.dto;

import com.examia.model.DecisionTreeDefinition;

import com.examia.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO para crear o actualizar una pregunta dentro de un examen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    /**
     * ID de la pregunta (solo para actualizaciones)
     */
    private String id;

    /**
     * Tipo de pregunta
     */
    @NotNull(message = "El tipo de pregunta es obligatorio")
    private QuestionType type;

    /**
     * Texto de la pregunta
     */
    @NotBlank(message = "El texto de la pregunta es obligatorio")
    private String text;

    /**
     * Opciones disponibles para preguntas de opción múltiple
     */
    private List<String> options;

    /**
     * Índice(s) de la(s) respuesta(s) correcta(s)
     */
    private List<Integer> correctAnswers;

    /**
     * Respuesta correcta para preguntas de tipo texto
     */
    private String correctTextAnswer;

    /**
     * Pares de relacionamiento para preguntas tipo MATCHING
     */
    private Map<String, String> matchingPairs;

    /**
     * Encabezados de columnas para preguntas MATRIX
     */
    private List<String> matrixColumnHeaders;

    /**
     * Filas de la tabla MATRIX
     */
    private List<ArrayList<String>> matrixRows;

    /**
     * Orden correcto para preguntas de tipo ORDERING
     */
    private List<String> correctOrder;

    /**
     * Definición del árbol para preguntas DECISION_TREE
     */
    private DecisionTreeDefinition decisionTree;

    /**
     * Puntos asignados a esta pregunta
     */
    @Positive(message = "Los puntos deben ser positivos")
    private Double points;

    /**
     * Explicación o retroalimentación de la respuesta correcta
     */
    private String explanation;

    /**
     * URL de imagen asociada a la pregunta
     */
    private String imageUrl;

    /**
     * Tema o sección a la que pertenece la pregunta dentro del examen
     */
    private String topic;

    /**
     * Color hexadecimal asignado al tema
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

