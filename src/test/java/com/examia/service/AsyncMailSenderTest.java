package com.examia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AsyncMailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AsyncMailSender asyncMailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(asyncMailSender, "mailFrom", "noreply@examia.com");
    }

    @Test
    void send_buildsCorrectMessageAndDelegatesToMailSender() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        asyncMailSender.send("recipient@example.com", "Test Subject", "Test Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("noreply@examia.com", sent.getFrom());
        assertEquals("recipient@example.com", sent.getTo()[0]);
        assertEquals("Test Subject", sent.getSubject());
        assertEquals("Test Body", sent.getText());
    }
}
