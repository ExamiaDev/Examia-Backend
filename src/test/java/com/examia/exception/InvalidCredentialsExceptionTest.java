package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void invalidCredentialsExceptionWithMessage() {
        String message = "Credenciales inválidas";
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void invalidCredentialsExceptionWithMessageAndCause() {
        String message = "Credenciales inválidas";
        Throwable cause = new RuntimeException("Cause");
        InvalidCredentialsException exception = new InvalidCredentialsException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

