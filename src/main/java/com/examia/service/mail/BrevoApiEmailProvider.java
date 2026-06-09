package com.examia.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Implementación basada en la API HTTP de Brevo
 * (<a href="https://developers.brevo.com/">developers.brevo.com</a>).
 *
 * <p>Usa el endpoint {@code POST /v3/smtp/email} sobre HTTPS (puerto 443), lo
 * cual evita el bloqueo de puertos SMTP outbound que aplican algunas
 * plataformas PaaS como Render en su plan Free.</p>
 *
 * <p>Se activa cuando {@code mail.provider=brevo-api}. Requiere:</p>
 * <ul>
 *     <li>{@code BREVO_API_KEY} — clave de API generada en el panel de Brevo
 *         (Account &gt; SMTP &amp; API &gt; API Keys).</li>
 *     <li>{@code MAIL_FROM} — email remitente verificado en Brevo.</li>
 *     <li>{@code MAIL_FROM_NAME} — (opcional) nombre amigable del remitente.</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mail.provider", havingValue = "brevo-api")
public class BrevoApiEmailProvider implements EmailProvider {

    private final RestClient restClient;
    private final String mailFrom;
    private final String mailFromName;

    public BrevoApiEmailProvider(
            RestClient.Builder restClientBuilder,
            @Value("${mail.brevo.api-url:https://api.brevo.com/v3/smtp/email}") String apiUrl,
            @Value("${mail.brevo.api-key:}") String apiKey,
            @Value("${mail.from:}") String mailFrom,
            @Value("${mail.from-name:Examia}") String mailFromName
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "mail.provider=brevo-api requiere la propiedad 'mail.brevo.api-key' (env BREVO_API_KEY)."
            );
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException(
                    "mail.provider=brevo-api requiere la propiedad 'mail.from' (env MAIL_FROM) verificada en Brevo."
            );
        }

        // Detectamos errores típicos de configuración que provocan 401 en Brevo:
        // - copiar/pegar con espacios o saltos invisibles
        // - usar la "SMTP key" (login SMTP) en lugar de la API key v3
        String sanitizedKey = apiKey.trim();
        if (!sanitizedKey.equals(apiKey)) {
            log.warn("BREVO_API_KEY contenía espacios/saltos al inicio o final. " +
                    "Se recortaron, pero revisá el valor en Render para evitar problemas futuros.");
        }
        if (!sanitizedKey.startsWith("xkeysib-")) {
            log.warn("BREVO_API_KEY no empieza con 'xkeysib-'. " +
                    "¿Estás usando la SMTP key en lugar de la API key v3? " +
                    "Generala en Brevo > Account > SMTP & API > API Keys.");
        }
        log.info("Brevo API configurado: from='{}' name='{}' keyLen={} keyPrefix='{}'",
                mailFrom, mailFromName, sanitizedKey.length(), maskKey(sanitizedKey));

        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.restClient = restClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("api-key", sanitizedKey)
                .defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void send(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", mailFrom, "name", mailFromName),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        try {
            restClient.post()
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Email Brevo API enviado a {}", to);
        } catch (HttpStatusCodeException ex) {
            // Brevo devuelve el motivo del error en el body (p.ej. {"code":"unauthorized","message":"Key not found"}).
            // RestClient.retrieve() no lo expone en el mensaje, así que lo extraemos manualmente.
            String responseBody = ex.getResponseBodyAsString();
            log.error("Falló el envío de email vía Brevo API a {}: {} {} - body: {}",
                    to, ex.getStatusCode().value(), ex.getStatusText(),
                    responseBody.isBlank() ? "<vacío>" : responseBody);
            throw ex;
        } catch (RestClientException ex) {
            log.error("Falló el envío de email vía Brevo API a {}: {}", to, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Devuelve una versión enmascarada de la API key para loggear sin exponerla.
     * Ejemplo: "xkeysib-abc...xyz" (sólo los primeros y últimos chars).
     */
    private static String maskKey(String key) {
        if (key == null || key.length() <= 12) {
            return "***";
        }
        return key.substring(0, 11) + "..." + key.substring(key.length() - 3);
    }
}




