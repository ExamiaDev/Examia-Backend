package com.examia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuración de MongoDB.
 * Habilita la auditoría para campos como @CreatedDate.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}

