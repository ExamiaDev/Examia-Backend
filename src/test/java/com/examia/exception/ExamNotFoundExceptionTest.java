package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExamNotFoundException.
 */
class ExamNotFoundExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Exam not found";
        ExamNotFoundException exception = new ExamNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageAndCause() {
        String message = "Exam not found";
        Throwable cause = new RuntimeException("Underlying cause");
        ExamNotFoundException exception = new ExamNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        ExamNotFoundException exception = new ExamNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}

