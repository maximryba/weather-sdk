package com.weather.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.exception.WeatherApiException;
import com.weather.exception.WeatherSDKException;
import com.weather.model.OpenWeatherResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP client for communicating with the OpenWeatherMap API.
 *
 * <p>This client handles the following responsibilities:
 * <ul>
 *   <li>Constructing properly formatted API requests</li>
 *   <li>Handling HTTP communication and error responses</li>
 *   <li>Parsing JSON responses into domain objects</li>
 *   <li>Converting API-specific errors to SDK exceptions</li>
 *   <li>Managing HTTP connection lifecycle</li>
 * </ul>
 *
 * <p><b>Supported HTTP Status Codes:</b>
 * <ul>
 *   <li>200 OK - Returns parsed weather data</li>
 *   <li>401 Unauthorized - Throws WeatherApiException for invalid API key</li>
 *   <li>404 Not Found - Throws WeatherApiException for unknown city</li>
 *   <li>429 Too Many Requests - Throws WeatherApiException for rate limiting</li>
 *   <li>Other errors - Throws WeatherApiException with response body</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * WeatherHttpClient client = new WeatherHttpClient("https://api.openweathermap.org/data/2.5", "your-api-key");
 * OpenWeatherResponse response = client.getWeatherByCity("London");
 * client.close();
 * }
 * </pre>
 *
 * @see OpenWeatherResponse
 * @see WeatherApiException
 * @see WeatherSDKException
 */
public class WeatherHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(WeatherHttpClient.class);
    private static final String PARAM = "q";
    private static final String APP_ID = "appid";
    private static final String UNITS = "units";

    private final String baseUrl;
    private final String apiKey;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new WeatherHttpClient with the specified base URL and API key.
     *
     * @param baseUrl the base URL of the OpenWeatherMap API (e.g., "https://api.openweathermap.org/data/2.5")
     * @param apiKey the API key for authenticating with the OpenWeatherMap service
     * @throws NullPointerException if baseUrl or apiKey is null
     * @throws IllegalArgumentException if baseUrl is empty or malformed
     */
    public WeatherHttpClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves current weather data for the specified city from the OpenWeatherMap API.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Constructs the API request URL with query parameters</li>
     *   <li>Sends an HTTP GET request to the weather API</li>
     *   <li>Parses the JSON response into an OpenWeatherResponse object</li>
     *   <li>Handles error responses by throwing appropriate exceptions</li>
     * </ol>
     *
     * @param cityName the name of the city to get weather data for
     * @return OpenWeatherResponse containing the parsed weather data
     * @throws WeatherApiException if the API returns an error response (e.g., city not found, invalid API key)
     * @throws WeatherSDKException if there's a network error, URI syntax issue, or JSON parsing error
     * @throws NullPointerException if cityName is null
     * @throws IllegalArgumentException if cityName is empty
     *
     * @see OpenWeatherResponse
     * @see WeatherApiException
     * @see WeatherSDKException
     */
    public OpenWeatherResponse getWeatherByCity(String cityName) {
        logger.debug("Fetching weather data for city: {}", cityName);

        try {
            URI uri = new URIBuilder(baseUrl + "/weather")
                    .addParameter(PARAM, cityName)
                    .addParameter(APP_ID, apiKey)
                    .addParameter(UNITS, "metric")
                    .build();

            HttpGet request = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());

                logger.debug("API Response status: {}, body: {}", statusCode, responseBody);

                if (statusCode == HttpStatus.SC_OK) {
                    return objectMapper.readValue(responseBody, OpenWeatherResponse.class);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    throw new WeatherApiException("City not found: " + cityName, statusCode);
                } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    throw new WeatherApiException("Invalid API key", statusCode);
                } else if (statusCode == HttpStatus.SC_TOO_MANY_REQUESTS) {
                    throw new WeatherApiException("API rate limit exceeded", statusCode);
                } else {
                    throw new WeatherApiException("Weather API error: " + responseBody, statusCode);
                }
            }

        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax", e);
            throw new WeatherSDKException("Invalid API configuration", e);
        } catch (IOException e) {
            logger.error("IO error during API call", e);
            throw new WeatherSDKException("Failed to communicate with weather API", e);
        }
    }

    /**
     * Closes the underlying HTTP client and releases associated resources.
     * This method should be called when the client is no longer needed to prevent
     * resource leaks. Once closed, the client cannot be reused.
     *
     * <p><b>Note:</b> It's recommended to use this client in a try-with-resources
     * pattern or call this method explicitly when done.
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}