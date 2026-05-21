package com.examia.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAlreadyExistsExceptionTest {

    @Test
    void userAlreadyExistsExceptionWithMessage() {
        String message = "Usuario ya existe";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(message);

        assertEquals(message, exception.getMessage());
    }

}
