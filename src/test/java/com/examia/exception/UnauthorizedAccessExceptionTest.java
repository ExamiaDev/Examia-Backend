package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UnauthorizedAccessException.
 */
class UnauthorizedAccessExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Not authorized";
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageAndCause() {
        String message = "Not authorized";
        Throwable cause = new RuntimeException("Underlying cause");
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}

