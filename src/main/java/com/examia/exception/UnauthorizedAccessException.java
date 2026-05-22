package com.examia.exception;

/**
 * Excepción lanzada cuando el usuario no tiene permiso para realizar una acción.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

