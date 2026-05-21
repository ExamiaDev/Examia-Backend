package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidResetCodeExceptionTest {

    @Test
    void invalidResetCodeExceptionWithMessage() {
        String message = "Código de reset inválido";
        InvalidResetCodeException exception = new InvalidResetCodeException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void invalidResetCodeExceptionWithMessageAndCause() {
        String message = "Código de reset inválido";
        Throwable cause = new RuntimeException("Cause");
        InvalidResetCodeException exception = new InvalidResetCodeException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

