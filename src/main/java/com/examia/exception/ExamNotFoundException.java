package com.examia.exception;

/**
 * Excepción lanzada cuando no se encuentra un examen.
 */
public class ExamNotFoundException extends RuntimeException {

    public ExamNotFoundException(String message) {
        super(message);
    }

    public ExamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

