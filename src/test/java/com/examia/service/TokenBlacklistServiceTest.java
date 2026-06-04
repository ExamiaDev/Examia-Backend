package com.examia.service;

import com.examia.repository.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenBlacklistServiceTest {

    private RevokedTokenRepository revokedTokenRepository;
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        revokedTokenRepository = mock(RevokedTokenRepository.class);
        tokenBlacklistService = new TokenBlacklistService(revokedTokenRepository);
    }

    @Test
    void revokeTokenShouldSaveRevokedTokenToRepository() {
        String token = "test.jwt.token";
        Date expiresAt = new Date(System.currentTimeMillis() + 3600_000);

        tokenBlacklistService.revokeToken(token, expiresAt);

        verify(revokedTokenRepository).save(argThat(revoked ->
            revoked.getToken().equals(token) && revoked.getExpiresAt().equals(expiresAt)
        ));
    }

    @Test
    void isRevokedWhenTokenExistsShouldReturnTrue() {
        when(revokedTokenRepository.existsByToken("revoked-token")).thenReturn(true);

        assertTrue(tokenBlacklistService.isRevoked("revoked-token"));
    }

    @Test
    void isRevokedWhenTokenDoesNotExistShouldReturnFalse() {
        when(revokedTokenRepository.existsByToken("valid-token")).thenReturn(false);

        assertFalse(tokenBlacklistService.isRevoked("valid-token"));
    }
}
