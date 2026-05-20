package com.examia.service;

import com.examia.dto.ForgotPasswordResponse;
import com.examia.exception.InvalidResetCodeException;
import com.examia.exception.UserNotFoundException;
import com.examia.model.PasswordResetToken;
import com.examia.model.User;
import com.examia.repository.PasswordResetTokenRepository;
import com.examia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;

    public ForgotPasswordResponse sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe un usuario con el email '" + email + "'"
                ));

        String recoveryEmail = user.getRecoveryEmail() != null ? user.getRecoveryEmail() : email;

        tokenRepository.deleteByEmail(email);

        String code = generateCode();
        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .build();
        tokenRepository.save(token);

        sendEmail(recoveryEmail, code);

        return ForgotPasswordResponse.builder()
                .maskedEmail(maskEmail(recoveryEmail))
                .message("Código enviado correctamente")
                .build();
    }

    public void verifyCode(String email, String code) {
        PasswordResetToken token = tokenRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new InvalidResetCodeException("No existe una solicitud de recuperación para este email"));

        if (token.isUsed()) {
            throw new InvalidResetCodeException("El código ya fue utilizado");
        }
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new InvalidResetCodeException("El código ha expirado. Solicitá uno nuevo.");
        }
        if (!token.getCode().equals(code)) {
            throw new InvalidResetCodeException("El código ingresado es incorrecto");
        }
    }

    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetToken token = tokenRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new InvalidResetCodeException("No existe una solicitud de recuperación para este email"));

        if (token.isUsed()) {
            throw new InvalidResetCodeException("El código ya fue utilizado");
        }
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new InvalidResetCodeException("El código ha expirado. Solicitá uno nuevo.");
        }
        if (!token.getCode().equals(code)) {
            throw new InvalidResetCodeException("El código ingresado es incorrecto");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String visible = local.substring(0, 2);
        String masked = "*".repeat(Math.min(local.length() - 2, 10));
        return visible + masked + domain;
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("Examia - Código de recuperación de contraseña");
        message.setText(
                "Hola,\n\n" +
                "Tu código de verificación para recuperar tu contraseña de Examia es:\n\n" +
                "  " + code + "\n\n" +
                "Este código es válido por " + EXPIRY_MINUTES + " minutos.\n\n" +
                "Si no solicitaste este código, ignorá este mensaje.\n\n" +
                "— Equipo Examia"
        );
        mailSender.send(message);
    }
}
