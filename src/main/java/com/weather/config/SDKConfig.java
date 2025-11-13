package com.weather.config;

/**
 * Configuration class for the Weather SDK containing all configurable parameters.
 *
 * <p>This class holds the configuration settings used by the Weather SDK, including
 * API credentials, operation mode, cache settings, and API endpoints.
 *
 * <p><b>Default Values:</b>
 * <ul>
 *   <li>baseUrl: "https://api.openweathermap.org/data/2.5"</li>
 *   <li>cacheTtlMinutes: 10 minutes</li>
 *   <li>maxCities: 10 cities</li>
 * </ul>
 *
 * <p><b>Example with Custom Configuration:</b>
 * <pre>
 * {@code
 * SDKConfig config = new SDKConfig(
 *     "your-api-key",
 *     OperationMode.POLLING,
 *     "https://api.openweathermap.org/data/2.5",
 *     15,  // 15 minute cache TTL
 *     20   // Max 20 cities in cache
 * );
 * }
 * </pre>
 *
 * @see OperationMode
 */
public class SDKConfig {
    private final String apiKey;
    private final OperationMode mode;
    private final String baseUrl;
    private final long cacheTtlMinutes;
    private final int maxCities;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";

    /**
     * Constructs a new SDKConfig with default values for base URL, cache TTL, and max cities.
     *
     * @param apiKey the OpenWeatherMap API key
     * @param mode the operation mode (ON_DEMAND or POLLING)
     * @throws NullPointerException if apiKey or mode is null
     * @throws IllegalArgumentException if apiKey is empty
     */
    public SDKConfig(String apiKey, OperationMode mode) {
        this(apiKey, mode, BASE_URL, 10, 10);
    }

    /**
     * Constructs a new SDKConfig with all customizable parameters.
     *
     * @param apiKey the OpenWeatherMap API key
     * @param mode the operation mode (ON_DEMAND or POLLING)
     * @param baseUrl the base URL for the OpenWeatherMap API
     * @param cacheTtlMinutes the time-to-live for cache entries in minutes
     * @param maxCities the maximum number of cities that can be stored in the cache
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if apiKey is empty, or cacheTtlMinutes/maxCities are not positive
     */
    public SDKConfig(String apiKey, OperationMode mode, String baseUrl,
                     long cacheTtlMinutes, int maxCities) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.baseUrl = baseUrl;
        this.cacheTtlMinutes = cacheTtlMinutes;
        this.maxCities = maxCities;
    }

    /**
     * Returns the OpenWeatherMap API key.
     *
     * @return the API key
     */
    public String getApiKey() { return apiKey; }

    /**
     * Returns the operation mode.
     *
     * @return the operation mode (ON_DEMAND or POLLING)
     */
    public OperationMode getMode() { return mode; }

    /**
     * Returns the base URL for the OpenWeatherMap API.
     *
     * @return the base URL
     */
    public String getBaseUrl() { return baseUrl; }

    /**
     * Returns the cache time-to-live in minutes.
     *
     * @return the cache TTL in minutes
     */
    public long getCacheTtlMinutes() { return cacheTtlMinutes; }

    /**
     * Returns the maximum number of cities that can be cached.
     *
     * @return the maximum number of cacheable cities
     */
    public int getMaxCities() { return maxCities; }
}