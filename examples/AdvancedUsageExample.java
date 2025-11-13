package com.weather.examples;

import com.weather.WeatherSDK;
import com.weather.config.SDKConfig;
import com.weather.exception.CityNotFoundException;
import com.weather.exception.WeatherApiException;
import com.weather.exception.WeatherSDKException;
import com.weather.model.WeatherData;

import java.util.Scanner;

/**
 * Example of advanced usage with error handling and interactive input
 */
public class AdvancedUsageExample {

    public static void main(String[] args) {
        String apiKey = "your-api-key-here";
        WeatherSDK sdk = WeatherSDK.getInstance(apiKey, SDKConfig.OperationMode.ON_DEMAND);
        Scanner scanner = new Scanner(System.in);

        System.out.println("🌍 Weather SDK Demo");
        System.out.println("Введите названия городов для получения погоды");
        System.out.println("Введите 'quit' для выхода");
        System.out.println();

        try {
            while (true) {
                System.out.print("Введите город: ");
                String input = scanner.nextLine().trim();

                if ("quit".equalsIgnoreCase(input)) {
                    break;
                }

                if (input.isEmpty()) {
                    continue;
                }

                try {
                    WeatherData weather = sdk.getWeather(input);
                    displayDetailedWeather(weather);

                } catch (CityNotFoundException e) {
                    System.err.println("Город '" + input + "' не найден");
                    System.out.println("Попробуйте уточнить название (например, 'London,UK')");

                } catch (WeatherApiException e) {
                    System.err.println("Ошибка API: " + e.getMessage());
                    if (e.getStatusCode() == 401) {
                        System.err.println("Проверьте правильность API ключа");
                    } else if (e.getStatusCode() == 429) {
                        System.err.println("Превышен лимит запросов. Попробуйте позже");
                    }

                } catch (WeatherSDKException e) {
                    System.err.println("Ошибка SDK: " + e.getMessage());
                }

                System.out.println();
            }

        } finally {
            scanner.close();
            WeatherSDK.removeInstance(apiKey);
            System.out.println("До свидания!");
        }
    }

    private static void displayDetailedWeather(WeatherData weather) {
        System.out.println();
        System.out.println("=================================");
        System.out.println(weather.getName());
        System.out.println("=================================");
        System.out.println(weather.getWeather().getMain() +
                " (" + weather.getWeather().getDescription() + ")");
        System.out.println("Температура: " +
                Math.round(weather.getTemperature().getTemp()) + " C");
        System.out.println("Ощущается как: " +
                Math.round(weather.getTemperature().getFeelsLike()) + " C");
        System.out.println("Ветер: " + weather.getWind().getSpeed() + " м/с");
        System.out.println("Видимость: " + (weather.getVisibility() / 1000) + " км");

        // Конвертируем timestamp в читаемое время
        java.time.Instant instant = java.time.Instant.ofEpochSecond(weather.getDatetime());
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
        System.out.println("Данные актуальны на: " + dateTime);
    }
}