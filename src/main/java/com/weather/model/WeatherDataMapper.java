package com.weather.model;

/**
 * Utility class for mapping between OpenWeatherMap API response and SDK weather data model.
 *
 * <p>This mapper converts the detailed OpenWeatherResponse structure to the simplified
 * WeatherData structure used throughout the SDK. It handles null safety and provides
 * a clean separation between API-specific structures and the public SDK interface.
 *
 * <p><b>Mapping Logic:</b>
 * <ul>
 *   <li>Selects first weather condition from the list (if available)</li>
 *   <li>Converts nested API structures to flat domain models</li>
 *   <li>Handles null values gracefully</li>
 *   <li>Preserves all essential weather information</li>
 * </ul>
 *
 * @see OpenWeatherResponse
 * @see WeatherData
 */
public class WeatherDataMapper {

    /**
     * Converts an OpenWeatherMap API response to the SDK's WeatherData model.
     *
     * <p>This method performs the following transformations:
     * <ul>
     *   <li>Extracts the first weather condition from the list</li>
     *   <li>Maps main temperature data to Temperature object</li>
     *   <li>Converts wind information to Wind object</li>
     *   <li>Transforms system data to Sys object</li>
     *   <li>Copies direct fields (visibility, datetime, timezone, name)</li>
     * </ul>
     *
     * @param response the OpenWeatherMap API response, can be null
     * @return a WeatherData object, or null if the input response is null
     */
    public static WeatherData fromOpenWeatherResponse(OpenWeatherResponse response) {
        if (response == null) {
            return null;
        }

        Weather weather = null;
        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            OpenWeatherResponse.WeatherInfo weatherInfo = response.getWeather().get(0);
            weather = new Weather(weatherInfo.getMain(), weatherInfo.getDescription());
        }

        Temperature temperature = null;
        if (response.getMain() != null) {
            temperature = new Temperature(response.getMain().getTemp(), response.getMain().getFeelsLike());
        }

        Wind wind = null;
        if (response.getWind() != null) {
            wind = new Wind(response.getWind().getSpeed());
        }

        Sys sys = null;
        if (response.getSys() != null) {
            sys = new Sys(response.getSys().getSunrise(), response.getSys().getSunset());
        }

        return new WeatherData(
                weather,
                temperature,
                response.getVisibility(),
                wind,
                response.getDatetime(),
                sys,
                response.getTimezone(),
                response.getName()
        );
    }
}