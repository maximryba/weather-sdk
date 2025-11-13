package com.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Standardized weather data model used throughout the SDK.
 *
 * <p>This class represents the normalized weather data structure that is returned
 * to SDK users. It provides a consistent interface regardless of the underlying
 * data source and is designed for easy consumption by client applications.
 *
 * <p><b>Data Flow:</b>
 * <pre>
 * OpenWeatherResponse → WeatherDataMapper → WeatherData → SDK User
 * </pre>
 *
 * <p><b>Example Usage:</b>
 * <pre>
 * {@code
 * WeatherData data = weatherSDK.getWeather("London");
 * System.out.println("Temperature: " + data.getTemperature().getTemp());
 * System.out.println("Condition: " + data.getWeather().getMain());
 * }
 * </pre>
 *
 * @see WeatherDataMapper
 * @see OpenWeatherResponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {
    @JsonProperty("weather")
    private Weather weather;

    @JsonProperty("temperature")
    private Temperature temperature;

    @JsonProperty("visibility")
    private Integer visibility;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("datetime")
    private Long datetime;

    @JsonProperty("sys")
    private Sys sys;

    @JsonProperty("timezone")
    private Integer timezone;

    @JsonProperty("name")
    private String name;

    /**
     * Default constructor for JSON deserialization.
     */
    public WeatherData() {}

    /**
     * Constructs a complete WeatherData object with all fields.
     *
     * @param weather the weather condition information
     * @param temperature the temperature data
     * @param visibility the visibility in meters
     * @param wind the wind information
     * @param datetime the data timestamp as UNIX time
     * @param sys the system information (sunrise/sunset)
     * @param timezone the timezone offset in seconds
     * @param name the city name
     */
    public WeatherData(Weather weather, Temperature temperature, Integer visibility,
                       Wind wind, Long datetime, Sys sys, Integer timezone, String name) {
        this.weather = weather;
        this.temperature = temperature;
        this.visibility = visibility;
        this.wind = wind;
        this.datetime = datetime;
        this.sys = sys;
        this.timezone = timezone;
        this.name = name;
    }

    // Getters and Setters
    /**
     * Returns the weather condition information.
     *
     * @return the weather condition data, or null if not available
     */
    public Weather getWeather() {
        return weather;
    }

    /**
     * Sets the weather condition information.
     *
     * @param weather the weather condition data to set
     */
    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    /**
     * Returns the temperature data.
     *
     * @return the temperature information, or null if not available
     */
    public Temperature getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature data.
     *
     * @param temperature the temperature information to set
     */
    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns the visibility distance.
     *
     * @return the visibility in meters, or null if not available
     */
    public Integer getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility distance.
     *
     * @param visibility the visibility in meters to set
     */
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    /**
     * Returns the wind information.
     *
     * @return the wind data, or null if not available
     */
    public Wind getWind() {
        return wind;
    }

    /**
     * Sets the wind information.
     *
     * @param wind the wind data to set
     */
    public void setWind(Wind wind) {
        this.wind = wind;
    }

    /**
     * Returns the data timestamp.
     *
     * @return the UNIX timestamp when data was calculated, or null if not available
     */
    public Long getDatetime() {
        return datetime;
    }

    /**
     * Sets the data timestamp.
     *
     * @param datetime the UNIX timestamp when data was calculated
     */
    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    /**
     * Returns the system information.
     *
     * @return the system data (sunrise/sunset), or null if not available
     */
    public Sys getSys() {
        return sys;
    }

    /**
     * Sets the system information.
     *
     * @param sys the system data (sunrise/sunset) to set
     */
    public void setSys(Sys sys) {
        this.sys = sys;
    }

    /**
     * Returns the timezone offset.
     *
     * @return the timezone shift in seconds from UTC, or null if not available
     */
    public Integer getTimezone() {
        return timezone;
    }

    /**
     * Sets the timezone offset.
     *
     * @param timezone the timezone shift in seconds from UTC
     */
    public void setTimezone(Integer timezone) {
        this.timezone = timezone;
    }

    /**
     * Returns the city name.
     *
     * @return the name of the city, or null if not available
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the city name.
     *
     * @param name the name of the city
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherData that = (WeatherData) o;
        return Objects.equals(weather, that.weather) &&
                Objects.equals(temperature, that.temperature) &&
                Objects.equals(visibility, that.visibility) &&
                Objects.equals(wind, that.wind) &&
                Objects.equals(datetime, that.datetime) &&
                Objects.equals(sys, that.sys) &&
                Objects.equals(timezone, that.timezone) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weather, temperature, visibility, wind, datetime, sys, timezone, name);
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "weather=" + weather +
                ", temperature=" + temperature +
                ", visibility=" + visibility +
                ", wind=" + wind +
                ", datetime=" + datetime +
                ", sys=" + sys +
                ", timezone=" + timezone +
                ", name='" + name + '\'' +
                '}';
    }
}