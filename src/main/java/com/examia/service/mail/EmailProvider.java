package com.examia.service.mail;

/**
 * Abstracción del envío de correo electrónico.
 *
 * <p>La aplicación soporta múltiples implementaciones seleccionadas
 * mediante la propiedad {@code mail.provider}:</p>
 * <ul>
 *     <li>{@code smtp} — usa {@link org.springframework.mail.javamail.JavaMailSender}
 *         contra un relay SMTP (ideal para desarrollo local).</li>
 *     <li>{@code brevo-api} — usa la API HTTP de Brevo (necesario en entornos
 *         como Render donde el tráfico SMTP outbound está bloqueado).</li>
 * </ul>
 */
public interface EmailProvider {

    /**
     * Envía un correo de texto plano.
     *
     * @param to      destinatario
     * @param subject asunto
     * @param body    cuerpo del mensaje en texto plano
     */
    void send(String to, String subject, String body);
}

