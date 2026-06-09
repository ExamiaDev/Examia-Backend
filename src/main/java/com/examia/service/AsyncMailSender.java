package com.examia.service;

import com.examia.service.mail.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envía correos de forma asíncrona delegando en el {@link EmailProvider}
 * activo (SMTP en local, API HTTP de Brevo en Render). Cualquier fallo se
 * loguea pero no se propaga al hilo del request original, ya que el envío
 * corre en otro hilo gracias a {@code @Async}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMailSender {

    private final EmailProvider emailProvider;

    @Async
    public void send(String to, String subject, String body) {
        try {
            emailProvider.send(to, subject, body);
        } catch (Exception ex) {
            log.error("No se pudo enviar el email a {}: {}", to, ex.getMessage(), ex);
        }
    }
}
