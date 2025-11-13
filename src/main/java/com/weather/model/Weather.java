package com.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents weather condition information with main category and description.
 *
 * <p>This class encapsulates the qualitative description of weather conditions,
 * such as "Clouds" with description "scattered clouds".
 *
 * @see WeatherData
 */
public class Weather {
    @JsonProperty("main")
    private String main;

    @JsonProperty("description")
    private String description;

    /**
     * Default constructor for JSON deserialization.
     */
    public Weather() {}

    /**
     * Constructs a Weather object with specified values.
     *
     * @param main the main weather category (e.g., "Rain", "Snow", "Clouds")
     * @param description the detailed weather description
     */
    public Weather(String main, String description) {
        this.main = main;
        this.description = description;
    }

    /**
     * Returns the main weather category.
     *
     * @return the main weather category (e.g., "Rain", "Snow", "Clouds")
     */
    public String getMain() {
        return main;
    }

    /**
     * Sets the main weather category.
     *
     * @param main the main weather category to set
     */
    public void setMain(String main) {
        this.main = main;
    }

    /**
     * Returns the detailed weather description.
     *
     * @return the detailed weather description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the detailed weather description.
     *
     * @param description the detailed weather description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weather weather = (Weather) o;
        return Objects.equals(main, weather.main) &&
                Objects.equals(description, weather.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(main, description);
    }

    @Override
    public String toString() {
        return "Weather{" +
                "main='" + main + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}