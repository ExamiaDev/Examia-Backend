package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void userNotFoundExceptionWithMessage() {
        String message = "Usuario no encontrado";
        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void userNotFoundExceptionWithMessageAndCause() {
        String message = "Usuario no encontrado";
        Throwable cause = new RuntimeException("Cause");
        UserNotFoundException exception = new UserNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

