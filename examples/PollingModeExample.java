package com.weather.examples;

import com.weather.WeatherSDK;
import com.weather.config.SDKConfig;
import com.weather.model.WeatherData;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Пример использования SDK в polling режиме с многопоточными запросами
 */
public class PollingModeExample {

    public static void main(String[] args) throws InterruptedException {
        String apiKey = "your-api-key-here";

        // Создаем SDK в polling режиме для мгновенного доступа к данным
        WeatherSDK sdk = WeatherSDK.getInstance(apiKey, SDKConfig.OperationMode.POLLING);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<String> europeanCapitals = Arrays.asList(
                "London", "Paris", "Berlin", "Rome", "Madrid",
                "Amsterdam", "Vienna", "Prague", "Warsaw", "Brussels"
        );

        try {
            System.out.println("🚀 Запуск многопоточных запросов погоды...");

            // Запускаем запросы в разных потоках
            for (String capital : europeanCapitals) {
                executor.submit(() -> {
                    try {
                        // В polling режиме эти запросы будут очень быстрыми
                        // так как данные уже обновляются в фоне
                        WeatherData weather = sdk.getWeather(capital);
                        System.out.println(Thread.currentThread().getName() +
                                " - " + capital + ": " +
                                Math.round(weather.getTemperature().getTemp()) + " C");
                    } catch (Exception e) {
                        System.err.println(Thread.currentThread().getName() +
                                " - Ошибка для " + capital + ": " + e.getMessage());
                    }
                });
            }

            // Ждем завершения всех задач
            executor.shutdown();
            boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

            if (finished) {
                System.out.println("Все запросы завершены");
            } else {
                System.out.println("Некоторые запросы не завершились вовремя");
            }

        } finally {
            // Очистка ресурсов
            WeatherSDK.removeInstance(apiKey);
        }
    }
}