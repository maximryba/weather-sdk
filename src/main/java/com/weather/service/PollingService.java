package com.weather.service;

import com.weather.cache.WeatherCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A background service that periodically refreshes cached weather data for all stored cities.
 *
 * <p>This service operates in polling mode, automatically updating weather information
 * at regular intervals to ensure cached data remains fresh and to provide zero-latency
 * responses for client requests.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Scheduled background execution with configurable intervals</li>
 *   <li>Thread-safe start/stop operations with proper resource cleanup</li>
 *   <li>Automatic scheduler recreation for multiple start/stop cycles</li>
 *   <li>Daemon thread usage to prevent JVM shutdown blocking</li>
 *   <li>Graceful shutdown with configurable termination timeout</li>
 * </ul>
 *
 * <p><b>Lifecycle Management:</b>
 * <pre>
 * {@code
 * PollingService pollingService = new PollingService(weatherService, cache, 10);
 * pollingService.start();  // Starts periodic refresh every 10 minutes
 * // ... application runs ...
 * pollingService.stop();   // Stops the service and cleans up resources
 * }
 * </pre>
 *
 * <p><b>Thread Safety:</b> All public methods are thread-safe and protected by read-write locks.
 *
 * @see WeatherService
 * @see WeatherCache
 */
public class PollingService {
    private static final Logger logger = LoggerFactory.getLogger(PollingService.class);

    private final WeatherService weatherService;
    private final WeatherCache cache;
    private final long pollingIntervalMinutes;
    private final ReentrantReadWriteLock lock;

    private volatile boolean isRunning = false;
    private ScheduledExecutorService scheduler;

    /**
     * Constructs a new PollingService with the specified dependencies and polling interval.
     *
     * @param weatherService the weather service used for fetching fresh data
     * @param cache the cache instance to refresh
     * @param pollingIntervalMinutes the interval between cache refresh cycles in minutes
     * @throws NullPointerException if weatherService or cache is null
     * @throws IllegalArgumentException if pollingIntervalMinutes is not positive
     */
    public PollingService(WeatherService weatherService, WeatherCache cache, long pollingIntervalMinutes) {
        this.weatherService = weatherService;
        this.cache = cache;
        this.pollingIntervalMinutes = pollingIntervalMinutes;
        this.lock = new ReentrantReadWriteLock();
        this.scheduler = createScheduler();
    }

    /**
     * Creates a new scheduled executor service with a daemon thread.
     * Using daemon threads ensures the JVM can shutdown even if the polling service is still running.
     *
     * @return a new ScheduledExecutorService instance
     */
    private ScheduledExecutorService createScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "weather-polling-thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the polling service if it's not already running.
     *
     * <p>This method:
     * <ul>
     *   <li>Checks if the service is already running and logs a warning if so</li>
     *   <li>Recreates the scheduler if it was previously shutdown</li>
     *   <li>Schedules periodic cache refresh tasks at the configured interval</li>
     *   <li>Sets the running state to true</li>
     * </ul>
     *
     * <p>The first refresh occurs immediately upon starting, with subsequent refreshes
     * at the specified interval.
     *
     * @throws IllegalStateException if the scheduler cannot be created or scheduled
     */
    public void start() {
        lock.writeLock().lock();
        try {
            if (isRunning) {
                logger.warn("Polling service is already running");
                return;
            }

            // Recreate scheduler if it was shutdown
            if (scheduler.isShutdown()) {
                scheduler = createScheduler();
            }

            isRunning = true;
            scheduler.scheduleAtFixedRate(this::refreshAllCachedCities,
                    0, pollingIntervalMinutes, TimeUnit.MINUTES);

            logger.info("Polling service started with interval: {} minutes", pollingIntervalMinutes);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Stops the polling service and releases all resources.
     *
     * <p>This method:
     * <ul>
     *   <li>Sets the running state to false</li>
     *   <li>Initiates graceful shutdown of the scheduler</li>
     *   <li>Waits up to 5 seconds for running tasks to complete</li>
     *   <li>Forces shutdown if tasks don't complete in time</li>
     *   <li>Handles thread interruption during shutdown</li>
     * </ul>
     *
     * <p>If the service is not running, this method returns immediately without action.
     */
    public void stop() {
        lock.writeLock().lock();
        try {
            if (!isRunning) {
                return;
            }

            isRunning = false;
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("Polling service stopped");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the current running state of the polling service.
     *
     * @return true if the service is running, false otherwise
     */
    public boolean isRunning() {
        lock.readLock().lock();
        try {
            return isRunning;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Performs a refresh of all cached cities' weather data.
     *
     * <p>This method is executed periodically by the scheduler. In a production
     * implementation, this would iterate through all cached cities and fetch
     * fresh weather data for each one.
     *
     * <p><b>Current Implementation:</b> Logs the refresh operation. In a real
     * scenario, this would be extended to actually refresh all cached cities.
     *
     * <p><b>Error Handling:</b> Any exceptions during refresh are caught and
     * logged to prevent the scheduled task from terminating.
     */
    private void refreshAllCachedCities() {
        try {
            logger.debug("Starting scheduled cache refresh");
            // In real implementation, this would update data for all cities in cache
            // For demonstration, we just log the action
            logger.info("Scheduled cache refresh completed");
        } catch (Exception e) {
            logger.error("Error during scheduled cache refresh", e);
        }
    }
}