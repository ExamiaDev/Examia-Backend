package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerifyResetCodeRequestTest {

    @Test
    void settersShouldUpdateFields() {
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();

        request.setEmail("test@ejemplo.com");
        request.setCode("123456");

        assertEquals("test@ejemplo.com", request.getEmail());
        assertEquals("123456", request.getCode());
    }

    @Test
    void noArgsConstructorShouldCreateEmptyRequest() {
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();

        assertNull(request.getEmail());
        assertNull(request.getCode());
    }

    @Test
    void equalsShouldWorkCorrectly() {
        VerifyResetCodeRequest request1 = new VerifyResetCodeRequest();
        request1.setEmail("test@test.com");
        request1.setCode("123456");

        VerifyResetCodeRequest request2 = new VerifyResetCodeRequest();
        request2.setEmail("test@test.com");
        request2.setCode("123456");

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("test@test.com");
        request.setCode("123456");

        String str = request.toString();

        assertTrue(str.contains("test@test.com"));
        assertTrue(str.contains("123456"));
    }
}

