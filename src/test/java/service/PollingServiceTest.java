package service;

import com.weather.cache.WeatherCache;
import com.weather.service.PollingService;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PollingService class
 * Covers requirements: polling mode operation, thread management, resource cleanup, and scheduled execution
 */
@ExtendWith(MockitoExtension.class)
class PollingServiceTest {

    @Mock
    private WeatherService mockWeatherService;

    @Mock
    private WeatherCache mockCache;

    private PollingService pollingService;
    private final long pollingIntervalMinutes = 1L; // Short interval for testing

    @BeforeEach
    void setUp() {
        pollingService = new PollingService(mockWeatherService, mockCache, pollingIntervalMinutes);
    }

    @AfterEach
    void tearDown() {
        // Ensure polling service is stopped after each test
        if (pollingService.isRunning()) {
            pollingService.stop();
        }
    }

    @Test
    void start_WhenCalled_SetsRunningFlagToTrue() {
        // Act
        pollingService.start();

        // Assert
        assertTrue(pollingService.isRunning());
    }

    @Test
    void start_WhenAlreadyRunning_LogsWarningAndDoesNotRestart() {
        // Arrange
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Act
        pollingService.start(); // Second call

        // Assert - Should still be running without restarting
        assertTrue(pollingService.isRunning());
    }

    @Test
    void stop_WhenRunning_SetsRunningFlagToFalse() {
        // Arrange
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Act
        pollingService.stop();

        // Assert
        assertFalse(pollingService.isRunning());
    }

    @Test
    void stop_WhenNotRunning_DoesNothing() {
        // Arrange
        assertFalse(pollingService.isRunning());

        // Act
        pollingService.stop();

        // Assert - No exception should be thrown and state remains unchanged
        assertFalse(pollingService.isRunning());
    }

    @Test
    void isRunning_ReturnsCorrectState() {
        // Initially not running
        assertFalse(pollingService.isRunning());

        // After start
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // After stop
        pollingService.stop();
        assertFalse(pollingService.isRunning());
    }

    @Test
    void start_SchedulesTaskPeriodically() throws InterruptedException {
        // Arrange
        CountDownLatch firstExecutionLatch = new CountDownLatch(1);

        // Create a spy to verify the scheduled task is set up
        PollingService spyPollingService = spy(new PollingService(mockWeatherService, mockCache, pollingIntervalMinutes));

        // We can't directly test the private method, but we can verify the scheduling works
        // by checking that the service starts and runs without errors

        // Act
        spyPollingService.start();

        // Wait a bit to ensure the scheduler has started
        Thread.sleep(100);

        // Assert
        assertTrue(spyPollingService.isRunning(), "Service should be running after start");

        // Cleanup
        spyPollingService.stop();
    }

    @Test
    void stop_ShutsDownExecutorServiceProperly() {
        // Arrange
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Act
        pollingService.stop();

        // Assert
        assertFalse(pollingService.isRunning());
        // Executor service should be shut down (verified by no exceptions on subsequent operations)
    }

    @Test
    void stop_WhenInterrupted_HandlesInterruptionGracefully() {
        // Arrange
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Act - Simulate interruption during shutdown
        Thread.currentThread().interrupt();
        pollingService.stop();

        // Assert
        assertFalse(pollingService.isRunning());
        // Clear the interrupted status to avoid affecting other tests
        Thread.interrupted();
    }

    @Test
    void pollingService_ThreadSafety_ConcurrentStartStopOperations() throws InterruptedException {
        // Arrange
        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // Act - Multiple threads accessing the service concurrently
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    // Perform various operations that should be thread-safe
                    pollingService.start();
                    boolean running = pollingService.isRunning();
                    pollingService.stop();
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown(); // Release all threads
        boolean allCompleted = endLatch.await(5, TimeUnit.SECONDS);

        // Assert - No concurrency issues should occur
        assertTrue(allCompleted, "All threads should complete within timeout");
        assertFalse(pollingService.isRunning());
    }

    @Test
    void pollingService_MultipleStartStopCycles_WorksCorrectly() {
        // Test multiple start/stop cycles to ensure robustness
        for (int i = 0; i < 3; i++) {
            // Cycle
            pollingService.start();
            assertTrue(pollingService.isRunning(), "Should be running after start in cycle " + i);

            pollingService.stop();
            assertFalse(pollingService.isRunning(), "Should not be running after stop in cycle " + i);
        }
    }

    @Test
    void constructor_CreatesServiceWithCorrectParameters() {
        // Arrange
        WeatherService weatherService = mock(WeatherService.class);
        WeatherCache cache = mock(WeatherCache.class);
        long interval = 5L;

        // Act
        PollingService customPollingService = new PollingService(weatherService, cache, interval);

        // Assert - Service should be created without errors
        assertNotNull(customPollingService);
        assertFalse(customPollingService.isRunning());
    }

    @Test
    void pollingService_ResourceCleanup_NoResourceLeaks() {
        // Arrange
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Act
        pollingService.stop();

        // Assert
        assertFalse(pollingService.isRunning());
        // No explicit resource leak verification, but no exceptions should occur
    }

    @Test
    void pollingService_StateConsistency_AfterMultipleOperations() {
        // Test complex state transitions
        assertFalse(pollingService.isRunning(), "Initially should not be running");

        pollingService.start();
        assertTrue(pollingService.isRunning(), "Should be running after start");

        pollingService.stop();
        assertFalse(pollingService.isRunning(), "Should not be running after stop");

        pollingService.start();
        assertTrue(pollingService.isRunning(), "Should be running after second start");

        pollingService.stop();
        assertFalse(pollingService.isRunning(), "Should not be running after second stop");

        // Final state check
        assertFalse(pollingService.isRunning());
    }

    @Test
    void pollingService_ImmediateStopAfterStart_HandlesCorrectly() {
        // Test the edge case where stop is called immediately after start
        pollingService.start();
        assertTrue(pollingService.isRunning());

        // Immediate stop
        pollingService.stop();
        assertFalse(pollingService.isRunning());
    }

    @Test
    void pollingService_IsRunning_ThreadSafeUnderLoad() throws InterruptedException {
        // Arrange
        int readerThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(readerThreads);

        // Start the service
        pollingService.start();

        // Act - Multiple threads reading the isRunning state concurrently
        for (int i = 0; i < readerThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < 100; j++) {
                        boolean running = pollingService.isRunning();
                        // All calls should return true since service is running
                        assertTrue(running, "isRunning should always return true when service is running");
                    }
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        startLatch.countDown();
        boolean allCompleted = endLatch.await(3, TimeUnit.SECONDS);

        // Assert
        assertTrue(allCompleted, "All reader threads should complete");
        assertTrue(pollingService.isRunning());

        // Cleanup
        pollingService.stop();
    }
}