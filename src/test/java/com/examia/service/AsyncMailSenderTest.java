package com.examia.service;

import com.examia.service.mail.EmailProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AsyncMailSenderTest {

    @Mock
    private EmailProvider emailProvider;

    @InjectMocks
    private AsyncMailSender asyncMailSender;

    @Test
    void send_delegatesToConfiguredEmailProvider() {
        doNothing().when(emailProvider).send("recipient@example.com", "Test Subject", "Test Body");

        asyncMailSender.send("recipient@example.com", "Test Subject", "Test Body");

        verify(emailProvider).send("recipient@example.com", "Test Subject", "Test Body");
    }

    @Test
    void send_swallowsProviderExceptionsToProtectAsyncCaller() {
        doThrow(new RuntimeException("smtp down"))
                .when(emailProvider).send("recipient@example.com", "Test Subject", "Test Body");

        // No debe propagar la excepción — corre en hilo async y solo loguea
        assertDoesNotThrow(() ->
                asyncMailSender.send("recipient@example.com", "Test Subject", "Test Body"));

        verify(emailProvider).send("recipient@example.com", "Test Subject", "Test Body");
    }
}
