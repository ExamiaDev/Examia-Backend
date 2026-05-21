package com.examia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordResponseTest {

    @Test
    void builderShouldCreateResponseWithAllFields() {
        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .maskedEmail("us****@ejemplo.com")
                .message("Código enviado correctamente")
                .build();

        assertEquals("us****@ejemplo.com", response.getMaskedEmail());
        assertEquals("Código enviado correctamente", response.getMessage());
    }

    @Test
    void settersShouldUpdateFields() {
        ForgotPasswordResponse response = new ForgotPasswordResponse();

        response.setMaskedEmail("te****@test.com");
        response.setMessage("Test message");

        assertEquals("te****@test.com", response.getMaskedEmail());
        assertEquals("Test message", response.getMessage());
    }

    @Test
    void noArgsConstructorShouldCreateEmptyResponse() {
        ForgotPasswordResponse response = new ForgotPasswordResponse();

        assertNull(response.getMaskedEmail());
        assertNull(response.getMessage());
    }

    @Test
    void allArgsConstructorShouldCreateResponse() {
        ForgotPasswordResponse response = new ForgotPasswordResponse(
                "ma****@ejemplo.com",
                "Mensaje de prueba"
        );

        assertEquals("ma****@ejemplo.com", response.getMaskedEmail());
        assertEquals("Mensaje de prueba", response.getMessage());
    }

    @Test
    void equalsShouldWorkCorrectly() {
        ForgotPasswordResponse response1 = ForgotPasswordResponse.builder()
                .maskedEmail("test@test.com")
                .message("message")
                .build();

        ForgotPasswordResponse response2 = ForgotPasswordResponse.builder()
                .maskedEmail("test@test.com")
                .message("message")
                .build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .maskedEmail("test@test.com")
                .message("Test message")
                .build();

        String str = response.toString();

        assertTrue(str.contains("test@test.com"));
        assertTrue(str.contains("Test message"));
    }
}
