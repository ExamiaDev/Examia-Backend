package com.examia.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Implementación SMTP del {@link EmailProvider}.
 *
 * <p>Es la implementación por defecto (ver {@code matchIfMissing = true}) y la
 * que se usa en desarrollo local, donde no hay restricciones de red para el
 * tráfico SMTP saliente.</p>
 *
 * <p>En plataformas como Render (especialmente plan Free) el tráfico SMTP
 * outbound suele estar bloqueado; en ese caso conviene activar
 * {@link BrevoApiEmailProvider} configurando {@code MAIL_PROVIDER=brevo-api}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Value("${mail.from:${spring.mail.username:}}")
    private String mailFrom;

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.debug("Email SMTP enviado a {}", to);
    }
}

