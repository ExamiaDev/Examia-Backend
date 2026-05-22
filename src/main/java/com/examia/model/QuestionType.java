package com.examia.model;

/**
 * Enum que representa los tipos de preguntas disponibles en un examen.
 */
public enum QuestionType {
    /**
     * Pregunta de opción múltiple (una sola respuesta correcta)
     */
    MULTIPLE_CHOICE,

    /**
     * Pregunta de selección múltiple (varias respuestas correctas)
     */
    MULTIPLE_SELECTION,

    /**
     * Pregunta de verdadero o falso
     */
    TRUE_FALSE,

    /**
     * Pregunta de respuesta corta
     */
    SHORT_ANSWER,

    /**
     * Pregunta de desarrollo o respuesta larga
     */
    LONG_ANSWER,

    /**
     * Pregunta de completar espacios en blanco
     */
    FILL_IN_THE_BLANK,

    /**
     * Pregunta de ordenamiento
     */
    ORDERING,

    /**
     * Pregunta de relacionar/emparejar
     */
    MATCHING
}

