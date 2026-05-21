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

}
