package cache;

import com.weather.cache.WeatherCache;
import com.weather.model.WeatherData;
import com.weather.model.Weather;
import com.weather.model.Temperature;
import com.weather.model.Wind;
import com.weather.model.Sys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherCache class
 * Tests cache functionality including TTL, LRU eviction, and thread safety
 */
@ExtendWith(MockitoExtension.class)
class WeatherCacheTest {

    private static final long TTL_MINUTES = 10L;
    private static final int MAX_SIZE = 10;
    private WeatherCache weatherCache;

    private WeatherData createTestWeatherData(String cityName) {
        // Create nested objects to build a complete WeatherData object
        Weather weather = new Weather();
        weather.setMain("Clouds");
        weather.setDescription("Scattered clouds");

        Temperature temperature = new Temperature();
        temperature.setTemp(269.6);
        temperature.setFeelsLike(267.57);

        Wind wind = new Wind();
        wind.setSpeed(1.38);

        Sys sys = new Sys();
        sys.setSunrise(1675751262L);
        sys.setSunset(1675787560L);

        return new WeatherData(
                weather,
                temperature,
                10000,
                wind,
                Instant.now().getEpochSecond(),
                sys,
                3600,
                cityName
        );
    }

    @BeforeEach
    void setUp() {
        weatherCache = new WeatherCache(TTL_MINUTES, MAX_SIZE);
    }

    @AfterEach
    void tearDown() {
        weatherCache.clear();
    }

    @Test
    void put_WhenValidData_StoresInCache() {
        // Arrange
        String cityName = "London";
        WeatherData weatherData = createTestWeatherData(cityName);

        // Act
        weatherCache.put(cityName, weatherData);

        // Assert
        WeatherData cachedData = weatherCache.get(cityName);
        assertNotNull(cachedData);
        assertEquals(cityName, cachedData.getName());
    }

    @Test
    void put_WhenCityNameInMixedCase_StoresInLowerCase() {
        // Arrange
        String cityNameMixedCase = "NeW YoRk";
        String cityNameLower = "new york";
        WeatherData weatherData = createTestWeatherData(cityNameMixedCase);

        // Act
        weatherCache.put(cityNameMixedCase, weatherData);

        // Assert - Should be retrievable with any case
        WeatherData cachedData1 = weatherCache.get(cityNameMixedCase);
        WeatherData cachedData2 = weatherCache.get(cityNameLower);

        assertNotNull(cachedData1);
        assertNotNull(cachedData2);
        assertEquals(cachedData1.getName(), cachedData2.getName());
    }

    @Test
    void get_WhenDataNotCached_ReturnsNull() {
        // Arrange
        String cityName = "Paris";

        // Act
        WeatherData result = weatherCache.get(cityName);

        // Assert
        assertNull(result);
    }

    @Test
    void get_WhenDataExistsAndNotExpired_ReturnsCachedData() {
        // Arrange
        String cityName = "Tokyo";
        WeatherData weatherData = createTestWeatherData(cityName);
        weatherCache.put(cityName, weatherData);

        // Act
        WeatherData result = weatherCache.get(cityName);

        // Assert
        assertNotNull(result);
        assertEquals(weatherData.getName(), result.getName());
        assertEquals(weatherData.getTemperature(), result.getTemperature());
        assertEquals(weatherData.getWeather(), result.getWeather());
    }

    @Test
    void remove_WhenCityExists_RemovesFromCache() {
        // Arrange
        String cityName = "Moscow";
        WeatherData weatherData = createTestWeatherData(cityName);
        weatherCache.put(cityName, weatherData);
        assertNotNull(weatherCache.get(cityName));

        // Act
        weatherCache.remove(cityName);

        // Assert
        assertNull(weatherCache.get(cityName));
    }

    @Test
    void remove_WhenCityNotExists_DoesNothing() {
        // Arrange
        String cityName = "Rome";

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> weatherCache.remove(cityName));
    }

    @Test
    void clear_WhenCacheHasData_RemovesAllEntries() {
        // Arrange
        weatherCache.put("London", createTestWeatherData("London"));
        weatherCache.put("Paris", createTestWeatherData("Paris"));
        assertEquals(2, weatherCache.size());

        // Act
        weatherCache.clear();

        // Assert
        assertEquals(0, weatherCache.size());
        assertNull(weatherCache.get("London"));
        assertNull(weatherCache.get("Paris"));
    }

    @Test
    void size_ReturnsCorrectNumberOfEntries() {
        // Arrange & Act
        assertEquals(0, weatherCache.size());

        weatherCache.put("City1", createTestWeatherData("City1"));
        assertEquals(1, weatherCache.size());

        weatherCache.put("City2", createTestWeatherData("City2"));
        assertEquals(2, weatherCache.size());

        weatherCache.remove("City1");
        assertEquals(1, weatherCache.size());
    }

    @Test
    void cache_WhenMaxSizeExceeded_EvictsOldestEntry() {
        // Arrange
        WeatherCache smallCache = new WeatherCache(TTL_MINUTES, 2); // Max 2 entries

        // Act - Add 3 entries
        smallCache.put("City1", createTestWeatherData("City1"));
        smallCache.put("City2", createTestWeatherData("City2"));
        smallCache.put("City3", createTestWeatherData("City3"));

        // Assert - Oldest entry should be evicted
        assertEquals(2, smallCache.size());
        assertNull(smallCache.get("City1")); // First entry should be evicted
        assertNotNull(smallCache.get("City2"));
        assertNotNull(smallCache.get("City3"));
    }

    @Test
    void cache_WhenAccessingEntry_MakesItRecentlyUsed() {
        // Arrange
        WeatherCache smallCache = new WeatherCache(TTL_MINUTES, 2);
        smallCache.put("City1", createTestWeatherData("City1"));
        smallCache.put("City2", createTestWeatherData("City2"));

        // Access City1 to make it recently used
        smallCache.get("City1");

        // Act - Add third city, should evict City2 (least recently used)
        smallCache.put("City3", createTestWeatherData("City3"));

        // Assert
        assertNotNull(smallCache.get("City1")); // Should still be there (recently accessed)
        assertNull(smallCache.get("City2")); // Should be evicted
        assertNotNull(smallCache.get("City3")); // New entry should be there
    }

    @Test
    void cache_ThreadSafety_ConcurrentAccess() throws InterruptedException {
        // Arrange
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // Act - Multiple threads accessing cache concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                String cityName = "City" + threadId;
                WeatherData data = createTestWeatherData(cityName);

                weatherCache.put(cityName, data);
                WeatherData retrieved = weatherCache.get(cityName);

                assertNotNull(retrieved);
                assertEquals(cityName, retrieved.getName());
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(1000); // Timeout to prevent hanging
        }

        // Assert - All operations should complete without concurrency issues
        assertEquals(threadCount, weatherCache.size());
    }

    @Test
    void cache_MaxSizeLimit_ExactlyTenCities() {
        // Arrange & Act - Add exactly MAX_SIZE cities
        for (int i = 0; i < MAX_SIZE; i++) {
            String cityName = "City" + i;
            weatherCache.put(cityName, createTestWeatherData(cityName));
        }

        // Assert
        assertEquals(MAX_SIZE, weatherCache.size());

        // Add one more city
        weatherCache.put("ExtraCity", createTestWeatherData("ExtraCity"));

        // Should still be MAX_SIZE due to LRU eviction
        assertEquals(MAX_SIZE, weatherCache.size());
    }

    @Test
    void cache_CaseInsensitiveOperations_WorkCorrectly() {
        // Arrange
        String cityNameMixed = "Los Angeles";
        String cityNameLower = "los angeles";
        String cityNameUpper = "LOS ANGELES";
        WeatherData weatherData = createTestWeatherData(cityNameMixed);

        // Act
        weatherCache.put(cityNameMixed, weatherData);

        // Assert - All case variations should work
        assertNotNull(weatherCache.get(cityNameMixed));
        assertNotNull(weatherCache.get(cityNameLower));
        assertNotNull(weatherCache.get(cityNameUpper));

        // Remove with different case
        weatherCache.remove(cityNameUpper);
        assertNull(weatherCache.get(cityNameMixed));
    }

    @Test
    void cache_Performance_MultipleOperations() {
        // Arrange
        int operationCount = 100;
        long startTime = System.currentTimeMillis();

        // Act - Perform multiple operations
        for (int i = 0; i < operationCount; i++) {
            String cityName = "City" + i;
            WeatherData data = createTestWeatherData(cityName);
            weatherCache.put(cityName, data);

            if (i % 10 == 0) {
                weatherCache.get(cityName);
            }

            if (i % 20 == 0) {
                weatherCache.remove(cityName);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert - Operations should complete in reasonable time
        assertTrue(duration < 1000, "Operations should complete quickly, took: " + duration + "ms");
    }

    @Test
    void cache_DataIntegrity_StoredDataMatchesOriginal() {
        // Arrange
        String cityName = "Berlin";
        WeatherData originalData = createTestWeatherData(cityName);

        // Act
        weatherCache.put(cityName, originalData);
        WeatherData retrievedData = weatherCache.get(cityName);

        // Assert
        assertNotNull(retrievedData);
        assertEquals(originalData.getName(), retrievedData.getName());
        assertEquals(originalData.getTemperature(), retrievedData.getTemperature());
        assertEquals(originalData.getWeather(), retrievedData.getWeather());
        assertEquals(originalData.getVisibility(), retrievedData.getVisibility());
        assertEquals(originalData.getWind(), retrievedData.getWind());
        assertEquals(originalData.getDatetime(), retrievedData.getDatetime());
        assertEquals(originalData.getSys(), retrievedData.getSys());
        assertEquals(originalData.getTimezone(), retrievedData.getTimezone());
    }

    @Test
    void cache_EmptyCache_OperationsWorkCorrectly() {
        // Assert initial state
        assertEquals(0, weatherCache.size());
        assertNull(weatherCache.get("NonExistentCity"));

        // Act & Assert - Operations on empty cache should not throw exceptions
        assertDoesNotThrow(() -> weatherCache.remove("NonExistentCity"));
        assertDoesNotThrow(() -> weatherCache.clear());
    }
}