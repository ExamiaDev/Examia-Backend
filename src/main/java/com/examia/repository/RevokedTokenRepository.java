package com.examia.repository;

import com.examia.model.RevokedToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RevokedTokenRepository extends MongoRepository<RevokedToken, String> {
    boolean existsByToken(String token);
}
