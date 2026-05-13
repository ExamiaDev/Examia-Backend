package com.examia.exception;

/**
 * Excepción lanzada cuando las credenciales de inicio de sesión son incorrectas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

