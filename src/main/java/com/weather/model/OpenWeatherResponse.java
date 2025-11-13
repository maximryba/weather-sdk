package com.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Represents the complete response structure from the OpenWeatherMap API.
 *
 * <p>This class maps the JSON response from OpenWeatherMap API to Java objects.
 * It uses Jackson annotations for JSON deserialization and includes all major
 * weather data fields provided by the API.
 *
 * <p><b>JSON Structure Example:</b>
 * <pre>
 * {
 *   "weather": [{"main": "Clouds", "description": "scattered clouds"}],
 *   "main": {"temp": 15.5, "feels_like": 14.2},
 *   "visibility": 10000,
 *   "wind": {"speed": 3.2},
 *   "dt": 1675744800,
 *   "sys": {"sunrise": 1675751262, "sunset": 1675787560},
 *   "timezone": 3600,
 *   "name": "London"
 * }
 * </pre>
 *
 * <p><b>Usage:</b>
 * <pre>
 * {@code
 * ObjectMapper mapper = new ObjectMapper();
 * OpenWeatherResponse response = mapper.readValue(json, OpenWeatherResponse.class);
 * }
 * </pre>
 *
 * @see JsonIgnoreProperties
 * @see JsonProperty
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    @JsonProperty("weather")
    private List<WeatherInfo> weather;

    @JsonProperty("main")
    private MainInfo main;

    @JsonProperty("visibility")
    private Integer visibility;

    @JsonProperty("wind")
    private WindInfo wind;

    @JsonProperty("dt")
    private Long datetime;

    @JsonProperty("sys")
    private SysInfo sys;

    @JsonProperty("timezone")
    private Integer timezone;

    @JsonProperty("name")
    private String name;

    /**
     * Represents weather condition information from OpenWeatherMap API.
     * Contains the main weather category and detailed description.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherInfo {
        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        /** @return the main weather category (e.g., "Clouds", "Rain") */
        public String getMain() { return main; }

        /** @param main the main weather category to set */
        public void setMain(String main) { this.main = main; }

        /** @return the detailed weather description */
        public String getDescription() { return description; }

        /** @param description the detailed weather description to set */
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Represents main weather parameters including temperature data.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainInfo {
        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feelsLike;

        /** @return the current temperature in configured units */
        public Double getTemp() { return temp; }

        /** @param temp the current temperature to set */
        public void setTemp(Double temp) { this.temp = temp; }

        /** @return the "feels like" temperature in configured units */
        public Double getFeelsLike() { return feelsLike; }

        /** @param feelsLike the "feels like" temperature to set */
        public void setFeelsLike(Double feelsLike) { this.feelsLike = feelsLike; }
    }

    /**
     * Represents wind information from the weather API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WindInfo {
        @JsonProperty("speed")
        private Double speed;

        /** @return the wind speed in configured units */
        public Double getSpeed() { return speed; }

        /** @param speed the wind speed to set */
        public void setSpeed(Double speed) { this.speed = speed; }
    }

    /**
     * Represents system information including sunrise and sunset times.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SysInfo {
        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;

        /** @return the sunrise time as UNIX timestamp */
        public Long getSunrise() { return sunrise; }

        /** @param sunrise the sunrise time as UNIX timestamp to set */
        public void setSunrise(Long sunrise) { this.sunrise = sunrise; }

        /** @return the sunset time as UNIX timestamp */
        public Long getSunset() { return sunset; }

        /** @param sunset the sunset time as UNIX timestamp to set */
        public void setSunset(Long sunset) { this.sunset = sunset; }
    }

    // Getters and Setters
    /** @return the list of weather condition information */
    public List<WeatherInfo> getWeather() { return weather; }

    /** @param weather the list of weather condition information to set */
    public void setWeather(List<WeatherInfo> weather) { this.weather = weather; }

    /** @return the main weather parameters */
    public MainInfo getMain() { return main; }

    /** @param main the main weather parameters to set */
    public void setMain(MainInfo main) { this.main = main; }

    /** @return the visibility in meters, or null if not available */
    public Integer getVisibility() { return visibility; }

    /** @param visibility the visibility in meters to set */
    public void setVisibility(Integer visibility) { this.visibility = visibility; }

    /** @return the wind information */
    public WindInfo getWind() { return wind; }

    /** @param wind the wind information to set */
    public void setWind(WindInfo wind) { this.wind = wind; }

    /** @return the data calculation time as UNIX timestamp */
    public Long getDatetime() { return datetime; }

    /** @param datetime the data calculation time as UNIX timestamp to set */
    public void setDatetime(Long datetime) { this.datetime = datetime; }

    /** @return the system information */
    public SysInfo getSys() { return sys; }

    /** @param sys the system information to set */
    public void setSys(SysInfo sys) { this.sys = sys; }

    /** @return the timezone shift in seconds from UTC */
    public Integer getTimezone() { return timezone; }

    /** @param timezone the timezone shift in seconds from UTC to set */
    public void setTimezone(Integer timezone) { this.timezone = timezone; }

    /** @return the city name */
    public String getName() { return name; }

    /** @param name the city name to set */
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenWeatherResponse that = (OpenWeatherResponse) o;
        return Objects.equals(weather, that.weather) &&
                Objects.equals(main, that.main) &&
                Objects.equals(visibility, that.visibility) &&
                Objects.equals(wind, that.wind) &&
                Objects.equals(datetime, that.datetime) &&
                Objects.equals(sys, that.sys) &&
                Objects.equals(timezone, that.timezone) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weather, main, visibility, wind, datetime, sys, timezone, name);
    }
}