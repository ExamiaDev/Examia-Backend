package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "revoked_tokens")
public class RevokedToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    // MongoDB elimina el documento automáticamente cuando expiresAt llega a su valor
    @Indexed(expireAfterSeconds = 0)
    private Date expiresAt;
}
