package com.examia.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email que ya existe.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
