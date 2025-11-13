package com.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents wind speed information.
 *
 * <p>Wind speed is typically in meters per second when using metric units
 * with the OpenWeatherMap API.
 *
 * @see WeatherData
 */
public class Wind {
    @JsonProperty("speed")
    private Double speed;

    /**
     * Default constructor for JSON deserialization.
     */
    public Wind() {}

    /**
     * Constructs a Wind object with specified speed.
     *
     * @param speed the wind speed in configured units
     */
    public Wind(Double speed) {
        this.speed = speed;
    }

    /**
     * Returns the wind speed.
     *
     * @return the wind speed in configured units
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * Sets the wind speed.
     *
     * @param speed the wind speed to set
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wind wind = (Wind) o;
        return Objects.equals(speed, wind.speed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(speed);
    }

    @Override
    public String toString() {
        return "Wind{" +
                "speed=" + speed +
                '}';
    }
}