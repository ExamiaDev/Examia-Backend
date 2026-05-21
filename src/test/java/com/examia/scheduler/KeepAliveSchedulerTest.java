package com.examia.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for KeepAliveScheduler.
 */
@ExtendWith(MockitoExtension.class)
class KeepAliveSchedulerTest {

    private KeepAliveScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new KeepAliveScheduler("https://examia-backend.onrender.com");
    }

    @Test
    void keepAlive_withValidUrl_doesNotThrow() {
        // The actual HTTP call might fail, but the method should handle it gracefully
        assertDoesNotThrow(() -> scheduler.keepAlive());
    }

    @Test
    void keepAlive_withEmptyUrl_doesNotThrow() {
        KeepAliveScheduler schedulerWithEmptyUrl = new KeepAliveScheduler("");
        assertDoesNotThrow(() -> schedulerWithEmptyUrl.keepAlive());
    }

    @Test
    void keepAlive_withNullUrl_doesNotThrow() {
        KeepAliveScheduler schedulerWithNullUrl = new KeepAliveScheduler(null);
        assertDoesNotThrow(() -> schedulerWithNullUrl.keepAlive());
    }

    @Test
    void keepAlive_withBlankUrl_doesNotThrow() {
        KeepAliveScheduler schedulerWithBlankUrl = new KeepAliveScheduler("   ");
        assertDoesNotThrow(() -> schedulerWithBlankUrl.keepAlive());
    }

    @Test
    void constructor_createsInstanceSuccessfully() {
        KeepAliveScheduler newScheduler = new KeepAliveScheduler("https://test.com");
        assertDoesNotThrow(() -> ReflectionTestUtils.getField(newScheduler, "applicationUrl"));
    }
}

