package com.examia.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Scheduler que mantiene activa la instancia de Render haciendo pings periódicos.
 *
 * Render en el plan gratuito pone en "sleep" las instancias después de 15 minutos
 * de inactividad. Este scheduler hace un ping cada 14 minutos para evitarlo.
 *
 * Se puede desactivar configurando keep-alive.enabled=false
 */
@Component
@ConditionalOnProperty(name = "keep-alive.enabled", havingValue = "true", matchIfMissing = false)
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final RestTemplate restTemplate;
    private final String applicationUrl;

    public KeepAliveScheduler(
            @Value("${keep-alive.url:}") String applicationUrl) {
        this.restTemplate = new RestTemplate();
        this.applicationUrl = applicationUrl;
    }

    /**
     * Ejecuta un ping al health endpoint cada 14 minutos (840000 ms).
     * El intervalo de 14 minutos es menor que el timeout de inactividad de Render (15 min).
     */
    @Scheduled(fixedRateString = "${keep-alive.interval:840000}")
    public void keepAlive() {
        if (applicationUrl == null || applicationUrl.isBlank()) {
            logger.warn("Keep-alive URL not configured. Set 'keep-alive.url' property.");
            return;
        }

        try {
            String healthUrl = applicationUrl + "/actuator/health";
            restTemplate.getForObject(healthUrl, String.class);
            logger.info("Keep-alive ping successful to: {}", healthUrl);
        } catch (Exception e) {
            logger.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}

