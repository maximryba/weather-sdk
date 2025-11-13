package com.weather.service;

import com.weather.cache.WeatherCache;
import com.weather.config.SDKConfig;
import com.weather.exception.CityNotFoundException;
import com.weather.exception.WeatherSDKException;
import com.weather.http.WeatherHttpClient;
import com.weather.model.OpenWeatherResponse;
import com.weather.model.WeatherData;
import com.weather.model.WeatherDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main service class that provides weather data retrieval functionality
 * with built-in caching and error handling.
 *
 * <p>This service implements the core business logic for the Weather SDK:
 * <ul>
 *   <li>Fetching weather data from the OpenWeatherMap API</li>
 *   <li>Caching responses with configurable TTL and size limits</li>
 *   <li>Converting API responses to standardized WeatherData objects</li>
 *   <li>Handling various error conditions and exceptions</li>
 *   <li>Providing both simple creation and flexible builder patterns</li>
 * </ul>
 *
 * <p><b>Data Flow:</b>
 * <ol>
 *   <li>Check cache for existing valid data</li>
 *   <li>If cache miss, fetch from API</li>
 *   <li>Convert API response to domain model</li>
 *   <li>Cache the result for future requests</li>
 *   <li>Return weather data to caller</li>
 * </ol>
 *
 * <p><b>Usage Examples:</b>
 * <pre>
 * {@code
 * // Simple creation
 * WeatherService service = WeatherService.create(config);
 *
 * // Builder pattern for testing or customization
 * WeatherService service = WeatherService.builder()
 *     .withConfig(config)
 *     .withHttpClient(customClient)
 *     .withCache(customCache)
 *     .build();
 *
 * WeatherData data = service.getWeatherByCity("London");
 * }
 * </pre>
 *
 * @see SDKConfig
 * @see WeatherData
 * @see WeatherCache
 * @see WeatherHttpClient
 */
public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherHttpClient httpClient;
    private final WeatherCache cache;
    private final SDKConfig config;

    /**
     * Constructs a new WeatherService with the specified configuration.
     * Creates default HTTP client and cache instances based on the configuration.
     *
     * @param config the SDK configuration containing API keys, URLs, and cache settings
     * @throws NullPointerException if config is null
     */
    public WeatherService(SDKConfig config) {
        this.config = config;
        this.httpClient = new WeatherHttpClient(config.getBaseUrl(), config.getApiKey());
        this.cache = new WeatherCache(config.getCacheTtlMinutes(), config.getMaxCities());
    }

    /**
     * Package-private constructor for testing - allows dependency injection of mocked components.
     *
     * @param config the SDK configuration
     * @param httpClient the HTTP client instance (can be mocked for testing)
     * @param cache the cache instance (can be mocked for testing)
     */
    WeatherService(SDKConfig config, WeatherHttpClient httpClient, WeatherCache cache) {
        this.config = config;
        this.httpClient = httpClient;
        this.cache = cache;
    }

    /**
     * Creates a new WeatherService instance with the specified configuration
     * using default HTTP client and cache implementations.
     *
     * @param config the SDK configuration
     * @return a new WeatherService instance
     * @throws NullPointerException if config is null
     */
    public static WeatherService create(SDKConfig config) {
        return builder().withConfig(config).build();
    }

    /**
     * Builder for creating WeatherService instances with custom components.
     *
     * <p>This builder pattern is useful for:
     * <ul>
     *   <li>Testing with mocked dependencies</li>
     *   <li>Customizing HTTP client behavior</li>
     *   <li>Using alternative cache implementations</li>
     *   <li>Dependency injection scenarios</li>
     * </ul>
     */
    public static class Builder {
        private SDKConfig config;
        private WeatherHttpClient httpClient;
        private WeatherCache cache;

        /**
         * Sets the SDK configuration for the service.
         *
         * @param config the SDK configuration
         * @return this builder instance for method chaining
         */
        public Builder withConfig(SDKConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets a custom HTTP client implementation.
         * If not provided, a default client will be created using the configuration.
         *
         * @param httpClient the custom HTTP client instance
         * @return this builder instance for method chaining
         */
        public Builder withHttpClient(WeatherHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets a custom cache implementation.
         * If not provided, a default cache will be created using the configuration.
         *
         * @param cache the custom cache instance
         * @return this builder instance for method chaining
         */
        public Builder withCache(WeatherCache cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Builds the WeatherService instance with the configured components.
         *
         * @return a new WeatherService instance
         * @throws IllegalStateException if configuration is not provided
         */
        public WeatherService build() {
            if (config == null) {
                throw new IllegalStateException("Config is required");
            }
            if (httpClient == null) {
                httpClient = new WeatherHttpClient(config.getBaseUrl(), config.getApiKey());
            }
            if (cache == null) {
                cache = new WeatherCache(config.getCacheTtlMinutes(), config.getMaxCities());
            }
            return new WeatherService(config, httpClient, cache);
        }
    }

    /**
     * Creates a new builder for constructing WeatherService instances with custom components.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves weather data for the specified city.
     *
     * <p>This method implements the following logic:
     * <ol>
     *   <li>Validates the city name (non-null, non-empty)</li>
     *   <li>Checks the cache for valid, non-expired data</li>
     *   <li>If cache hit, returns cached data immediately</li>
     *   <li>If cache miss, fetches fresh data from the API</li>
     *   <li>Converts API response to WeatherData format</li>
     *   <li>Caches the result for future requests</li>
     *   <li>Returns the weather data</li>
     * </ol>
     *
     * @param cityName the name of the city to get weather data for
     * @return WeatherData object containing the weather information
     * @throws WeatherSDKException if cityName is null or empty
     * @throws CityNotFoundException if the city is not found by the API
     * @throws WeatherSDKException if there's an API communication error
     *
     * @see WeatherData
     */
    public WeatherData getWeatherByCity(String cityName) {
        logger.info("Getting weather for city: {}", cityName);

        if (cityName == null || cityName.trim().isEmpty()) {
            throw new WeatherSDKException("City name cannot be null or empty");
        }

        String normalizedCityName = cityName.trim();

        // Try to get data from cache
        WeatherData cachedData = cache.get(normalizedCityName);
        if (cachedData != null) {
            logger.debug("Returning cached data for city: {}", normalizedCityName);
            return cachedData;
        }

        // If no data in cache - fetch from API
        logger.debug("Fetching fresh data from API for city: {}", normalizedCityName);
        OpenWeatherResponse apiResponse = httpClient.getWeatherByCity(normalizedCityName);

        if (apiResponse == null) {
            throw new CityNotFoundException(normalizedCityName);
        }

        WeatherData weatherData = WeatherDataMapper.fromOpenWeatherResponse(apiResponse);

        // Save to cache
        cache.put(normalizedCityName, weatherData);

        logger.info("Successfully retrieved weather data for city: {}", normalizedCityName);
        return weatherData;
    }

    /**
     * Refreshes the weather cache by clearing all cached entries.
     *
     * <p>In a production implementation, this would typically refresh data
     * for all cached cities rather than simply clearing the cache. The current
     * implementation clears the cache to force fresh API calls on next request.
     *
     * <p><b>Note:</b> This method is primarily useful for testing or manual
     * cache management. For automatic refresh, use {@link PollingService}.
     */
    public void refreshCache() {
        logger.info("Refreshing weather cache");
        // In real implementation, this would update all cities from cache
        // But for simplicity we just clear the cache
        cache.clear();
    }

    /**
     * Cleans up resources used by the weather service.
     *
     * <p>This method should be called when the service is no longer needed
     * to prevent resource leaks. It primarily closes the HTTP client connections.
     *
     * <p><b>Important:</b> After calling this method, the service should not be reused.
     */
    public void cleanup() {
        logger.info("Cleaning up weather service resources");
        httpClient.close();
    }

    /**
     * Returns the cache instance used by this service.
     *
     * @return the WeatherCache instance
     */
    public WeatherCache getCache() {
        return cache;
    }

    /**
     * Package-private getter for the HTTP client, primarily for testing.
     *
     * @return the WeatherHttpClient instance
     */
    WeatherHttpClient getHttpClient() {
        return httpClient;
    }
}