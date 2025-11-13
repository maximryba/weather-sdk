package com.weather;

import com.weather.config.OperationMode;
import com.weather.config.SDKConfig;
import com.weather.exception.CityNotFoundException;
import com.weather.exception.WeatherApiException;
import com.weather.exception.WeatherSDKException;
import com.weather.model.WeatherData;
import com.weather.service.PollingService;
import com.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main entry point for the Weather SDK providing weather data retrieval functionality.
 *
 * <p>This class implements the Singleton pattern per API key to ensure that only one instance
 * exists for each API key, preventing resource conflicts and ensuring proper resource management.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Singleton instance management per API key</li>
 *   <li>Support for both ON_DEMAND and POLLING operation modes</li>
 *   <li>Automatic polling service management in polling mode</li>
 *   <li>Thread-safe instance creation and cleanup</li>
 *   <li>Global shutdown capability for all instances</li>
 * </ul>
 *
 * <p><b>Operation Modes:</b>
 * <ul>
 *   <li><b>ON_DEMAND:</b> Fetches weather data only when explicitly requested</li>
 *   <li><b>POLLING:</b> Automatically refreshes cached data every 5 minutes in background</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * // Get SDK instance
 * WeatherSDK sdk = WeatherSDK.getInstance("your-api-key", OperationMode.ON_DEMAND);
 *
 * // Get weather data
 * WeatherData data = sdk.getWeather("London");
 *
 * // Cleanup when done (or use shutdownAll())
 * WeatherSDK.removeInstance("your-api-key");
 * }
 * </pre>
 *
 * <p><b>Thread Safety:</b> All public methods are thread-safe. Instance management uses
 * concurrent collections and synchronized methods where necessary.
 *
 * @see OperationMode
 * @see SDKConfig
 * @see WeatherData
 * @see WeatherService
 * @see PollingService
 */
public class WeatherSDK {
    private static final Logger logger = LoggerFactory.getLogger(WeatherSDK.class);

    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();

    private final WeatherService weatherService;
    private final PollingService pollingService;
    private final SDKConfig config;

    /**
     * Constructs a new WeatherSDK instance with the specified configuration.
     *
     * <p>Initializes the weather service and, if in POLLING mode, starts a background
     * polling service that refreshes cached data every 5 minutes.
     *
     * @param config the SDK configuration containing API key, operation mode, and other settings
     * @throws NullPointerException if config is null
     * @throws WeatherSDKException if API key is invalid or service initialization fails
     */
    private WeatherSDK(SDKConfig config) {
        this.config = config;
        this.weatherService = new WeatherService(config);

        // Initialize polling service if polling mode is required
        if (config.getMode() == OperationMode.POLLING) {
            this.pollingService = new PollingService(
                    weatherService,
                    weatherService.getCache(),
                    5 // update every 5 minutes
            );
            this.pollingService.start();
        } else {
            this.pollingService = null;
        }

        logger.info("WeatherSDK initialized with mode: {}", config.getMode());
    }

    /**
     * Gets or creates a WeatherSDK instance for the specified API key and operation mode.
     *
     * <p>This method implements the Singleton pattern per API key, ensuring that only one
     * instance exists for each unique API key. If an instance already exists for the given
     * API key, it is returned. Otherwise, a new instance is created and cached.
     *
     * @param apiKey the OpenWeatherMap API key (must be non-null and non-empty)
     * @param mode the operation mode (ON_DEMAND or POLLING)
     * @return a WeatherSDK instance for the specified API key
     * @throws WeatherSDKException if apiKey is null or empty
     * @throws NullPointerException if mode is null
     */
    public static WeatherSDK getInstance(String apiKey, OperationMode mode) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }

        return instances.computeIfAbsent(apiKey, key ->
                new WeatherSDK(new SDKConfig(key, mode))
        );
    }

    /**
     * Removes and cleans up the WeatherSDK instance for the specified API key.
     *
     * <p>This method stops any background polling services and releases all resources
     * associated with the instance. After calling this method, the instance should not
     * be used anymore.
     *
     * @param apiKey the API key whose instance should be removed
     */
    public static synchronized void removeInstance(String apiKey) {
        WeatherSDK instance = instances.remove(apiKey);
        if (instance != null) {
            instance.cleanup();
            logger.info("WeatherSDK instance removed for API key: {}", apiKey);
        }
    }

    /**
     * Shuts down all WeatherSDK instances and releases all resources.
     *
     * <p>This method should be called during application shutdown to ensure proper
     * cleanup of all SDK instances. It stops all background polling services and
     * closes all HTTP connections.
     *
     * <p><b>Important:</b> After calling this method, all SDK instances become unusable.
     */
    public static synchronized void shutdownAll() {
        logger.info("Shutting down all WeatherSDK instances");
        for (WeatherSDK instance : instances.values()) {
            instance.cleanup();
        }
        instances.clear();
    }

    /**
     * Retrieves weather data for the specified city.
     *
     * <p>In ON_DEMAND mode, this method checks the cache first and fetches from the
     * API only if cached data is not available or expired. In POLLING mode, the data
     * is typically already cached and refreshed periodically.
     *
     * @param cityName the name of the city to get weather data for
     * @return WeatherData object containing the weather information
     * @throws WeatherSDKException if cityName is null or empty
     * @throws CityNotFoundException if the city is not found by the API
     * @throws WeatherApiException if there's an API communication error
     *
     * @see WeatherData
     */
    public WeatherData getWeather(String cityName) {
        return weatherService.getWeatherByCity(cityName);
    }

    /**
     * Refreshes the weather cache by clearing all cached entries.
     *
     * <p>This forces the SDK to fetch fresh data from the API on subsequent requests.
     * Useful when you want to ensure you have the most recent weather data.
     */
    public void refreshCache() {
        weatherService.refreshCache();
    }

    /**
     * Cleans up resources used by this SDK instance.
     *
     * <p>Stops background polling services (if running) and releases HTTP client resources.
     * This method is called automatically when removing instances via {@link #removeInstance(String)}
     * or {@link #shutdownAll()}.
     */
    private void cleanup() {
        logger.info("Cleaning up WeatherSDK resources");

        if (pollingService != null) {
            pollingService.stop();
        }

        weatherService.cleanup();
    }

    /**
     * Returns the configuration used by this SDK instance.
     * Primarily intended for testing and internal use.
     *
     * @return the SDK configuration
     */
    SDKConfig getConfig() {
        return config;
    }

    /**
     * Checks if polling service is active for this SDK instance.
     * Primarily intended for testing and internal use.
     *
     * @return true if polling service is running, false otherwise
     */
    boolean isPollingActive() {
        return pollingService != null && pollingService.isRunning();
    }
}