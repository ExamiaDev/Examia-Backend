package com.examia.repository;

import com.examia.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findTopByEmailOrderByExpiresAtDesc(String email);

    void deleteByEmail(String email);
}
