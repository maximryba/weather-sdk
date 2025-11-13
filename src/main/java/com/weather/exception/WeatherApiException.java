package com.weather.exception;

/**
 * Exception thrown when the OpenWeatherMap API returns an error response.
 *
 * <p>This exception wraps HTTP error responses from the OpenWeatherMap API
 * and includes the HTTP status code for proper error handling.
 *
 * <p><b>Common HTTP Status Codes:</b>
 * <ul>
 *   <li>401 Unauthorized - Invalid API key</li>
 *   <li>404 Not Found - City not found (also throws {@link CityNotFoundException})</li>
 *   <li>429 Too Many Requests - API rate limit exceeded</li>
 *   <li>5xx - Server errors</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>
 * {@code
 * try {
 *     WeatherData data = sdk.getWeather("London");
 * } catch (WeatherApiException e) {
 *     System.out.println("API Error: " + e.getMessage());
 *     System.out.println("Status Code: " + e.getStatusCode());
 * }
 * }
 * </pre>
 *
 * @see WeatherSDKException
 * @see CityNotFoundException
 */
public class WeatherApiException extends WeatherSDKException {
    private final int statusCode;

    /**
     * Constructs a new WeatherApiException with the specified message and status code.
     *
     * @param message the detailed error message
     * @param statusCode the HTTP status code returned by the API
     */
    public WeatherApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new WeatherApiException with the specified message, status code, and cause.
     *
     * @param message the detailed error message
     * @param statusCode the HTTP status code returned by the API
     * @param cause the underlying cause of this exception
     */
    public WeatherApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code that caused this exception.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}