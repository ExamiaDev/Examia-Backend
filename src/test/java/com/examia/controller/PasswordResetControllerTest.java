package com.examia.controller;

import com.examia.dto.ForgotPasswordRequest;
import com.examia.dto.ForgotPasswordResponse;
import com.examia.dto.ResetPasswordRequest;
import com.examia.dto.VerifyResetCodeRequest;
import com.examia.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    private final PasswordResetService passwordResetService = mock(PasswordResetService.class);
    private final PasswordResetController controller = new PasswordResetController(passwordResetService);

    @Test
    void forgotPasswordShouldReturnOkResponse() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("usuario@ejemplo.com");

        ForgotPasswordResponse expectedResponse = ForgotPasswordResponse.builder()
                .maskedEmail("us*****@ejemplo.com")
                .message("Código enviado correctamente")
                .build();

        when(passwordResetService.sendResetCode("usuario@ejemplo.com")).thenReturn(expectedResponse);

        ResponseEntity<ForgotPasswordResponse> response = controller.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Código enviado correctamente", response.getBody().getMessage());
        assertEquals("us*****@ejemplo.com", response.getBody().getMaskedEmail());
        verify(passwordResetService).sendResetCode("usuario@ejemplo.com");
    }

    @Test
    void verifyCodeShouldReturnOkResponse() {
        VerifyResetCodeRequest request = new VerifyResetCodeRequest();
        request.setEmail("usuario@ejemplo.com");
        request.setCode("123456");

        doNothing().when(passwordResetService).verifyCode(anyString(), anyString());

        ResponseEntity<Map<String, String>> response = controller.verifyCode(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Código verificado correctamente", response.getBody().get("message"));
        verify(passwordResetService).verifyCode("usuario@ejemplo.com", "123456");
    }

    @Test
    void resetPasswordShouldReturnOkResponse() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("usuario@ejemplo.com");
        request.setCode("123456");
        request.setNewPassword("newPassword123");

        doNothing().when(passwordResetService).resetPassword(anyString(), anyString(), anyString());

        ResponseEntity<Map<String, String>> response = controller.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Contraseña actualizada correctamente", response.getBody().get("message"));
        verify(passwordResetService).resetPassword("usuario@ejemplo.com", "123456", "newPassword123");
    }
}

