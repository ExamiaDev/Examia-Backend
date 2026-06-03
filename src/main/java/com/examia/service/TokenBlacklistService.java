package com.examia.service;

import com.examia.model.RevokedToken;
import com.examia.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    public void revokeToken(String token, Date expiresAt) {
        RevokedToken revoked = RevokedToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        revokedTokenRepository.save(revoked);
    }

    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByToken(token);
    }
}
