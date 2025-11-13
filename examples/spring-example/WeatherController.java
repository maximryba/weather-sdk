package com.weather.examples;

import com.weather.model.WeatherData;
import org.springframework.web.bind.annotation.*;

/**
 * Example of integration SDK with Spring Boot app
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{city}")
    public WeatherData getWeather(@PathVariable String city) {
        return weatherService.getCityWeather(city);
    }

    @PostMapping("/cache/refresh")
    public String refreshCache() {
        weatherService.refreshWeatherCache();
        return "Cache refreshed successfully";
    }
}