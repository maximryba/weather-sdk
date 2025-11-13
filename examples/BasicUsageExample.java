package com.weather.examples;

import com.weather.WeatherSDK;
import com.weather.config.SDKConfig;
import com.weather.model.WeatherData;

/**
 * Базовый пример использования Weather SDK
 */
public class BasicUsageExample {

    public static void main(String[] args) {
        // Замените на ваш реальный API ключ
        String apiKey = "your-actual-api-key-here";

        // Создаем SDK в on-demand режим
        WeatherSDK weatherSDK = WeatherSDK.getInstance(apiKey, SDKConfig.OperationMode.ON_DEMAND);

        try {
            // Получаем погоду для нескольких городов
            String[] cities = {"London", "Paris", "Berlin", "Madrid"};

            for (String city : cities) {
                try {
                    WeatherData weather = weatherSDK.getWeather(city);
                    displayWeather(weather);
                    System.out.println("---");

                    // Небольшая пауза между запросами
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.err.println("Ошибка для города " + city + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Очищаем ресурсы
            WeatherSDK.removeInstance(apiKey);
        }
    }

    private static void displayWeather(WeatherData weather) {
        System.out.println("   Погода в " + weather.getName());
        System.out.println("   Состояние: " + weather.getWeather().getDescription());
        System.out.println("   Температура: " + Math.round(weather.getTemperature().getTemp()) + " C");
        System.out.println("   Ощущается как: " + Math.round(weather.getTemperature().getFeelsLike()) + " C");
        System.out.println("   Ветер: " + weather.getWind().getSpeed() + " м/с");
        System.out.println("   Видимость: " + (weather.getVisibility() / 1000) + " км");
    }
}