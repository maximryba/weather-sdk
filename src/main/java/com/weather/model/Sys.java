package com.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents system information including sunrise and sunset times.
 *
 * <p>Sunrise and sunset times are represented as UNIX timestamps (seconds since epoch).
 *
 * @see WeatherData
 */
public class Sys {
    @JsonProperty("sunrise")
    private Long sunrise;

    @JsonProperty("sunset")
    private Long sunset;

    /**
     * Default constructor for JSON deserialization.
     */
    public Sys() {}

    /**
     * Constructs a Sys object with specified sunrise and sunset times.
     *
     * @param sunrise the sunrise time as UNIX timestamp
     * @param sunset the sunset time as UNIX timestamp
     */
    public Sys(Long sunrise, Long sunset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    /**
     * Returns the sunrise time.
     *
     * @return the sunrise time as UNIX timestamp
     */
    public Long getSunrise() {
        return sunrise;
    }

    /**
     * Sets the sunrise time.
     *
     * @param sunrise the sunrise time as UNIX timestamp to set
     */
    public void setSunrise(Long sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * Returns the sunset time.
     *
     * @return the sunset time as UNIX timestamp
     */
    public Long getSunset() {
        return sunset;
    }

    /**
     * Sets the sunset time.
     *
     * @param sunset the sunset time as UNIX timestamp to set
     */
    public void setSunset(Long sunset) {
        this.sunset = sunset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sys sys = (Sys) o;
        return Objects.equals(sunrise, sys.sunrise) &&
                Objects.equals(sunset, sys.sunset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sunrise, sunset);
    }

    @Override
    public String toString() {
        return "Sys{" +
                "sunrise=" + sunrise +
                ", sunset=" + sunset +
                '}';
    }
}