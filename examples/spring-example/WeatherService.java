package com.weather.examples;

import com.weather.WeatherSDK;
import com.weather.config.SDKConfig;
import com.weather.model.WeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Example of integration SDK with Spring Boot app
 */
@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.sdk.mode:ON_DEMAND}")
    private String sdkMode;

    private WeatherSDK weatherSDK;

    @PostConstruct
    public void init() {
        SDKConfig.OperationMode mode = SDKConfig.OperationMode.valueOf(sdkMode);
        this.weatherSDK = WeatherSDK.getInstance(apiKey, mode);
    }

    public WeatherData getCityWeather(String cityName) {
        return weatherSDK.getWeather(cityName);
    }

    public void refreshWeatherCache() {
        weatherSDK.refreshCache();
    }

    @PreDestroy
    public void cleanup() {
        WeatherSDK.removeInstance(apiKey);
    }
}