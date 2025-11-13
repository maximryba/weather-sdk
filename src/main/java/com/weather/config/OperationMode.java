package com.weather.config;

import com.weather.WeatherSDK;

/**
 * Enumeration defining the operational modes of the Weather SDK.
 *
 * <p>The SDK supports two operation modes:
 * <ul>
 *   <li><b>ON_DEMAND:</b> Weather data is fetched only when explicitly requested by the client.
 *       This mode is more resource-efficient but may have higher latency on cache misses.</li>
 *   <li><b>POLLING:</b> Weather data is automatically refreshed in the background at regular intervals.
 *       This mode provides faster response times but consumes more resources due to background updates.</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <pre>
 * {@code
 * // On-demand mode for occasional requests
 * WeatherSDK sdk1 = WeatherSDK.getInstance("key1", OperationMode.ON_DEMAND);
 *
 * // Polling mode for frequent access with low latency requirements
 * WeatherSDK sdk2 = WeatherSDK.getInstance("key2", OperationMode.POLLING);
 * }
 * </pre>
 *
 * @see WeatherSDK
 * @see SDKConfig
 */
public enum OperationMode {
    /**
     * Fetches weather data only when explicitly requested.
     * Suitable for applications with infrequent weather data requests.
     */
    ON_DEMAND,

    /**
     * Automatically refreshes cached weather data in the background.
     * Suitable for applications requiring fast response times and frequent data access.
     */
    POLLING
}