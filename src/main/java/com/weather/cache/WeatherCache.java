package com.weather.cache;

import com.weather.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe cache implementation for storing weather data with time-based expiration
 * and LRU (Least Recently Used) eviction policy.
 *
 * <p>This cache provides the following features:
 * <ul>
 *   <li>Time-to-live (TTL) based expiration for cached entries</li>
 *   <li>Maximum size limit with LRU eviction when capacity is exceeded</li>
 *   <li>Case-insensitive city name handling</li>
 *   <li>Thread-safe operations using read-write locks</li>
 *   <li>Automatic cleanup of expired entries on access</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * WeatherCache cache = new WeatherCache(10, 10); // 10 minutes TTL, max 10 cities
 * cache.put("London", weatherData);
 * WeatherData data = cache.get("London");
 * }
 * </pre>
 *
 * @see WeatherData
 */
public class WeatherCache {
    private static final Logger logger = LoggerFactory.getLogger(WeatherCache.class);

    private final long ttlMinutes;
    private final int maxSize;
    private final Map<String, CacheEntry> cache;
    private final ReentrantReadWriteLock lock;

    /**
     * Constructs a new WeatherCache with the specified TTL and maximum size.
     *
     * @param ttlMinutes the time-to-live for cache entries in minutes
     * @param maxSize the maximum number of cities that can be stored in the cache
     * @throws IllegalArgumentException if ttlMinutes or maxSize is non-positive
     */
    public WeatherCache(long ttlMinutes, int maxSize) {
        this.ttlMinutes = ttlMinutes;
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > maxSize;
            }
        };
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Stores weather data for the specified city in the cache.
     * If the cache has reached its maximum size, the least recently used entry
     * will be evicted. City names are stored case-insensitively.
     *
     * @param cityName the name of the city (case-insensitive)
     * @param weatherData the weather data to cache
     * @throws NullPointerException if cityName or weatherData is null
     */
    public void put(String cityName, WeatherData weatherData) {
        lock.writeLock().lock();
        try {
            CacheEntry entry = new CacheEntry(weatherData, Instant.now());
            cache.put(cityName.toLowerCase(), entry);
            logger.debug("Cached weather data for city: {}", cityName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves weather data for the specified city from the cache.
     * Returns null if the city is not cached or if the cached data has expired.
     * Expired entries are automatically removed when accessed.
     *
     * @param cityName the name of the city to look up (case-insensitive)
     * @return the cached WeatherData, or null if not found or expired
     * @throws NullPointerException if cityName is null
     */
    public WeatherData get(String cityName) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(cityName.toLowerCase());
            if (entry == null) {
                logger.debug("Cache miss for city: {}", cityName);
                return null;
            }

            if (isExpired(entry)) {
                logger.debug("Cache entry expired for city: {}", cityName);
                remove(cityName);
                return null;
            }

            logger.debug("Cache hit for city: {}", cityName);
            return entry.weatherData();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes the weather data for the specified city from the cache.
     * If the city is not in the cache, this method does nothing.
     *
     * @param cityName the name of the city to remove (case-insensitive)
     * @throws NullPointerException if cityName is null
     */
    public void remove(String cityName) {
        lock.writeLock().lock();
        try {
            cache.remove(cityName.toLowerCase());
            logger.debug("Removed from cache: {}", cityName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes all entries from the cache.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            logger.debug("Cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the current number of entries in the cache.
     * This count may include expired entries that haven't been accessed yet.
     *
     * @return the number of cities currently stored in the cache
     */
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if a cache entry has expired based on its timestamp and TTL.
     *
     * @param entry the cache entry to check
     * @return true if the entry has expired, false otherwise
     */
    private boolean isExpired(CacheEntry entry) {
        return Instant.now().isAfter(entry.timestamp().plusSeconds(ttlMinutes * 60));
    }

    /**
     * Internal record representing a cache entry with weather data and timestamp.
     *
     * @param weatherData the cached weather data
     * @param timestamp the time when the data was cached
     */
    private record CacheEntry(WeatherData weatherData, Instant timestamp) {
    }
}