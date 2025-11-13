package com.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents temperature data including actual and "feels like" temperatures.
 *
 * <p>Temperature values are typically in Celsius when using metric units
 * with the OpenWeatherMap API.
 *
 * @see WeatherData
 */
public class Temperature {
    @JsonProperty("temp")
    private Double temp;

    @JsonProperty("feels_like")
    private Double feelsLike;

    /**
     * Default constructor for JSON deserialization.
     */
    public Temperature() {}

    /**
     * Constructs a Temperature object with specified values.
     *
     * @param temp the actual temperature
     * @param feelsLike the "feels like" temperature (accounting for humidity, wind, etc.)
     */
    public Temperature(Double temp, Double feelsLike) {
        this.temp = temp;
        this.feelsLike = feelsLike;
    }

    /**
     * Returns the actual temperature.
     *
     * @return the actual temperature in configured units
     */
    public Double getTemp() {
        return temp;
    }

    /**
     * Sets the actual temperature.
     *
     * @param temp the actual temperature to set
     */
    public void setTemp(Double temp) {
        this.temp = temp;
    }

    /**
     * Returns the "feels like" temperature.
     *
     * @return the perceived temperature accounting for weather factors
     */
    public Double getFeelsLike() {
        return feelsLike;
    }

    /**
     * Sets the "feels like" temperature.
     *
     * @param feelsLike the perceived temperature to set
     */
    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Temperature that = (Temperature) o;
        return Objects.equals(temp, that.temp) &&
                Objects.equals(feelsLike, that.feelsLike);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temp, feelsLike);
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "temp=" + temp +
                ", feelsLike=" + feelsLike +
                '}';
    }
}