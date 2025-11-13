package com.weather.exception;

/**
 * Base exception class for all Weather SDK related exceptions.
 *
 * <p>This is the parent class for all exceptions thrown by the Weather SDK.
 * It provides a common base for catching any SDK-related errors.
 *
 * <p><b>Exception Hierarchy:</b>
 * <pre>
 * WeatherSDKException
 * ├── WeatherApiException
 * └── CityNotFoundException
 * </pre>
 *
 * <p><b>Usage:</b>
 * <pre>
 * {@code
 * try {
 *     WeatherData data = sdk.getWeather("London");
 * } catch (WeatherSDKException e) {
 *     // Handle any SDK-related exception
 *     logger.error("SDK error occurred", e);
 * }
 * }
 * </pre>
 *
 * @see RuntimeException
 * @see WeatherApiException
 * @see CityNotFoundException
 */
public class WeatherSDKException extends RuntimeException {
    /**
     * Constructs a new WeatherSDKException with the specified detail message.
     *
     * @param message the detailed error message
     */
    public WeatherSDKException(String message) {
        super(message);
    }

    /**
     * Constructs a new WeatherSDKException with the specified detail message and cause.
     *
     * @param message the detailed error message
     * @param cause the underlying cause of this exception
     */
    public WeatherSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}