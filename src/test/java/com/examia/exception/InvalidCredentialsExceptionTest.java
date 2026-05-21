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

}
