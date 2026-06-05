package com.examia.service;

import com.examia.dto.ForgotPasswordResponse;
import com.examia.exception.InvalidResetCodeException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.PasswordResetToken;
import com.examia.model.Role;
import com.examia.model.User;
import com.examia.repository.PasswordResetTokenRepository;
import com.examia.repository.UserRepository;
import com.examia.service.AsyncMailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AsyncMailSender asyncMailSender;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("123")
                .email("usuario@ejemplo.com")
                .recoveryEmail("recovery@ejemplo.com")
                .nombre("Juan")
                .apellido("Perez")
                .password("encodedPassword")
                .role(Role.ALUMNO)
                .enabled(true)
                .build();
    }

    // ==================== TESTS DE sendResetCode ====================

    @Test
    void sendResetCodeWhenUserExistsShouldSendEmail() {
        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).deleteByEmail(anyString());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(asyncMailSender).send(anyString(), anyString(), anyString());

        ForgotPasswordResponse response = passwordResetService.sendResetCode("usuario@ejemplo.com");

        assertNotNull(response);
        assertEquals("Código enviado correctamente", response.getMessage());
        assertTrue(response.getMaskedEmail().startsWith("re"));
        assertTrue(response.getMaskedEmail().contains("@"));

        verify(tokenRepository).deleteByEmail("usuario@ejemplo.com");
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(asyncMailSender).send(anyString(), anyString(), anyString());
    }

    @Test
    void sendResetCodeWhenUserHasNoRecoveryEmailShouldUsePrimaryEmail() {
        User userWithoutRecovery = User.builder()
                .id("123")
                .email("usuario@ejemplo.com")
                .recoveryEmail(null)
                .nombre("Juan")
                .apellido("Perez")
                .build();

        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(userWithoutRecovery));
        doNothing().when(tokenRepository).deleteByEmail(anyString());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(asyncMailSender).send(anyString(), anyString(), anyString());

        ForgotPasswordResponse response = passwordResetService.sendResetCode("usuario@ejemplo.com");

        assertNotNull(response);
        assertTrue(response.getMaskedEmail().startsWith("us"));
    }

    @Test
    void sendResetCodeWhenUserNotFoundShouldThrowException() {
        when(userRepository.findByEmail("noexiste@ejemplo.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> passwordResetService.sendResetCode("noexiste@ejemplo.com")
        );

        assertTrue(exception.getMessage().contains("noexiste@ejemplo.com"));
        verify(tokenRepository, never()).save(any());
        verify(asyncMailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void sendResetCodeShouldGenerateSixDigitCode() {
        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).deleteByEmail(anyString());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(tokenRepository.save(tokenCaptor.capture())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(asyncMailSender).send(anyString(), anyString(), anyString());

        passwordResetService.sendResetCode("usuario@ejemplo.com");

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getCode());
        assertEquals(6, savedToken.getCode().length());
        assertTrue(savedToken.getCode().matches("\\d{6}"));
    }

    // ==================== TESTS DE verifyCode ====================

    @Test
    void verifyCodeWhenValidShouldNotThrowException() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(validToken));

        assertDoesNotThrow(() -> passwordResetService.verifyCode("usuario@ejemplo.com", "123456"));
    }

    @Test
    void verifyCodeWhenNoTokenExistsShouldThrowException() {
        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.empty());

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.verifyCode("usuario@ejemplo.com", "123456")
        );

        assertTrue(exception.getMessage().contains("No existe una solicitud"));
    }

    @Test
    void verifyCodeWhenAlreadyUsedShouldThrowException() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(true)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(usedToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.verifyCode("usuario@ejemplo.com", "123456")
        );

        assertEquals("El código ya fue utilizado", exception.getMessage());
    }

    @Test
    void verifyCodeWhenExpiredShouldThrowException() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(expiredToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.verifyCode("usuario@ejemplo.com", "123456")
        );

        assertTrue(exception.getMessage().contains("expirado"));
    }

    @Test
    void verifyCodeWhenIncorrectCodeShouldThrowException() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(validToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.verifyCode("usuario@ejemplo.com", "999999")
        );

        assertTrue(exception.getMessage().contains("incorrecto"));
    }

    // ==================== TESTS DE resetPassword ====================

    @Test
    void resetPasswordWhenValidShouldUpdatePassword() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(validToken));
        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(validToken);

        assertDoesNotThrow(() ->
            passwordResetService.resetPassword("usuario@ejemplo.com", "123456", "newPassword123")
        );

        verify(userRepository).save(argThat(user ->
            user.getPassword().equals("encodedNewPassword")
        ));
        verify(tokenRepository).save(argThat(token -> token.isUsed()));
    }

    @Test
    void resetPasswordWhenNoTokenShouldThrowException() {
        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.empty());

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.resetPassword("usuario@ejemplo.com", "123456", "newPassword")
        );

        assertTrue(exception.getMessage().contains("No existe una solicitud"));
    }

    @Test
    void resetPasswordWhenTokenAlreadyUsedShouldThrowException() {
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(true)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(usedToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.resetPassword("usuario@ejemplo.com", "123456", "newPassword")
        );

        assertEquals("El código ya fue utilizado", exception.getMessage());
    }

    @Test
    void resetPasswordWhenTokenExpiredShouldThrowException() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(expiredToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.resetPassword("usuario@ejemplo.com", "123456", "newPassword")
        );

        assertTrue(exception.getMessage().contains("expirado"));
    }

    @Test
    void resetPasswordWhenIncorrectCodeShouldThrowException() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(validToken));

        InvalidResetCodeException exception = assertThrows(
                InvalidResetCodeException.class,
                () -> passwordResetService.resetPassword("usuario@ejemplo.com", "999999", "newPassword")
        );

        assertTrue(exception.getMessage().contains("incorrecto"));
    }

    @Test
    void resetPasswordWhenUserNotFoundShouldThrowException() {
        PasswordResetToken validToken = PasswordResetToken.builder()
                .email("usuario@ejemplo.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findTopByEmailOrderByExpiresAtDesc("usuario@ejemplo.com"))
                .thenReturn(Optional.of(validToken));
        when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> passwordResetService.resetPassword("usuario@ejemplo.com", "123456", "newPassword")
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    // ==================== TESTS DE maskEmail (casos edge) ====================

    @Test
    void sendResetCodeWithShortEmailShouldNotCrash() {
        User userWithShortEmail = User.builder()
                .id("123")
                .email("ab@ejemplo.com")
                .recoveryEmail("ab@ejemplo.com")
                .nombre("Juan")
                .apellido("Perez")
                .build();

        when(userRepository.findByEmail("ab@ejemplo.com")).thenReturn(Optional.of(userWithShortEmail));
        doNothing().when(tokenRepository).deleteByEmail(anyString());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(asyncMailSender).send(anyString(), anyString(), anyString());

        ForgotPasswordResponse response = passwordResetService.sendResetCode("ab@ejemplo.com");

        assertNotNull(response);
        // El email con solo 2 caracteres antes del @ se devuelve sin máscara
        assertEquals("ab@ejemplo.com", response.getMaskedEmail());
    }
}

