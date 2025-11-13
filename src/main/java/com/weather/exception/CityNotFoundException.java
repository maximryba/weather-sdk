package com.weather.exception;

/**
 * Exception thrown when a requested city is not found by the OpenWeatherMap API.
 *
 * <p>This exception typically occurs when:
 * <ul>
 *   <li>The city name is misspelled</li>
 *   <li>The city does not exist in the OpenWeatherMap database</li>
 *   <li>The city name is in an unsupported format</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>
 * {@code
 * try {
 *     WeatherData data = sdk.getWeather("NonExistentCity");
 * } catch (CityNotFoundException e) {
 *     System.out.println("City not found: " + e.getMessage());
 * }
 * }
 * </pre>
 *
 * @see WeatherSDKException
 */
public class CityNotFoundException extends WeatherSDKException {
    /**
     * Constructs a new CityNotFoundException with the specified city name.
     *
     * @param cityName the name of the city that was not found
     */
    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName);
    }
}