package com.examia.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeepAliveSchedulerTest {

    private KeepAliveScheduler scheduler;
    private RestTemplate mockRestTemplate;

    @BeforeEach
    void setUp() {
        scheduler = new KeepAliveScheduler("https://examia-backend.onrender.com");
        mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(scheduler, "restTemplate", mockRestTemplate);
    }

    @Test
    void keepAlive_withValidUrl_pingHealthEndpoint() {
        when(mockRestTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{\"status\":\"UP\"}");

        assertDoesNotThrow(() -> scheduler.keepAlive());

        verify(mockRestTemplate).getForObject(
                "https://examia-backend.onrender.com/actuator/health", String.class);
    }

    @Test
    void keepAlive_whenHttpCallFails_doesNotThrow() {
        when(mockRestTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> scheduler.keepAlive());
    }

    @Test
    void keepAlive_withEmptyUrl_doesNotCallRestTemplate() {
        KeepAliveScheduler schedulerWithEmptyUrl = new KeepAliveScheduler("");

        assertDoesNotThrow(() -> schedulerWithEmptyUrl.keepAlive());
    }

    @Test
    void keepAlive_withNullUrl_doesNotCallRestTemplate() {
        KeepAliveScheduler schedulerWithNullUrl = new KeepAliveScheduler(null);

        assertDoesNotThrow(() -> schedulerWithNullUrl.keepAlive());
    }

    @Test
    void keepAlive_withBlankUrl_doesNotCallRestTemplate() {
        KeepAliveScheduler schedulerWithBlankUrl = new KeepAliveScheduler("   ");

        assertDoesNotThrow(() -> schedulerWithBlankUrl.keepAlive());
    }
}
