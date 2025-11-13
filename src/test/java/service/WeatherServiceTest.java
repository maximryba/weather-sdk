package service;

import com.weather.cache.WeatherCache;
import com.weather.config.SDKConfig;
import com.weather.exception.CityNotFoundException;
import com.weather.exception.WeatherSDKException;
import com.weather.http.WeatherHttpClient;
import com.weather.model.OpenWeatherResponse;
import com.weather.model.WeatherData;
import com.weather.model.WeatherDataMapper;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WeatherService class
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherHttpClient mockHttpClient;

    @Mock
    private SDKConfig mockConfig;

    private WeatherCache cache;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        // Mock configuration
        lenient().when(mockConfig.getCacheTtlMinutes()).thenReturn(10L);
        lenient().when(mockConfig.getMaxCities()).thenReturn(10);

        cache = new WeatherCache(10L, 10);

        weatherService = WeatherService.builder()
                .withConfig(mockConfig)
                .withCache(cache)
                .withHttpClient(mockHttpClient)
                .build();
    }

    @AfterEach
    void tearDown() {
        weatherService.cleanup();
    }

    @Test
    void getWeatherByCity_WithValidCity_ReturnsWeatherData() {
        // Arrange
        String cityName = "London";
        OpenWeatherResponse mockResponse = createMockOpenWeatherResponse(cityName);
        when(mockHttpClient.getWeatherByCity(cityName)).thenReturn(mockResponse);

        // Act
        WeatherData result = weatherService.getWeatherByCity(cityName);

        // Assert
        assertNotNull(result);
        assertEquals(cityName, result.getName());
        assertEquals("Clouds", result.getWeather().getMain());
        assertEquals(15.5, result.getTemperature().getTemp());
        verify(mockHttpClient).getWeatherByCity(cityName);
    }

    @Test
    void getWeatherByCity_WithCachedData_ReturnsCachedData() {
        // Arrange
        String cityName = "Paris";
        WeatherData cachedData = createTestWeatherData(cityName);
        cache.put(cityName, cachedData);

        // Act
        WeatherData result = weatherService.getWeatherByCity(cityName);

        // Assert
        assertNotNull(result);
        assertEquals(cityName, result.getName());
        // Verify that HTTP client was NOT called (data came from cache)
        verify(mockHttpClient, never()).getWeatherByCity(anyString());
    }

    @Test
    void getWeatherByCity_WithNullCity_ThrowsException() {
        // Act & Assert
        WeatherSDKException exception = assertThrows(WeatherSDKException.class,
                () -> weatherService.getWeatherByCity(null));
        assertEquals("City name cannot be null or empty", exception.getMessage());
    }

    @Test
    void getWeatherByCity_WithEmptyCity_ThrowsException() {
        // Act & Assert
        WeatherSDKException exception = assertThrows(WeatherSDKException.class,
                () -> weatherService.getWeatherByCity("   "));
        assertEquals("City name cannot be null or empty", exception.getMessage());
    }

    @Test
    void getWeatherByCity_WhenApiReturnsNull_ThrowsCityNotFoundException() {
        // Arrange
        String cityName = "NonExistentCity";
        when(mockHttpClient.getWeatherByCity(cityName)).thenReturn(null);

        // Act & Assert
        CityNotFoundException exception = assertThrows(CityNotFoundException.class,
                () -> weatherService.getWeatherByCity(cityName));
        assertTrue(exception.getMessage().contains(cityName));
    }

    @Test
    void getWeatherByCity_DataStoredInCache_AfterApiCall() {
        // Arrange
        String cityName = "Berlin";
        OpenWeatherResponse mockResponse = createMockOpenWeatherResponse(cityName);
        when(mockHttpClient.getWeatherByCity(cityName)).thenReturn(mockResponse);

        // Act
        WeatherData result = weatherService.getWeatherByCity(cityName);

        // Assert
        assertNotNull(result);
        // Verify data is cached
        WeatherData cachedData = cache.get(cityName);
        assertNotNull(cachedData);
        assertEquals(cityName, cachedData.getName());
    }

    @Test
    void getWeatherByCity_CacheRespectsMaxCitiesLimit() {
        // Arrange
        when(mockHttpClient.getWeatherByCity(anyString())).thenReturn(createMockOpenWeatherResponse("City"));

        // Act - Add more cities than cache limit
        for (int i = 1; i <= 15; i++) {
            String cityName = "City" + i;
            weatherService.getWeatherByCity(cityName);
        }

        // Assert - Cache should not exceed max size
        assertTrue(cache.size() <= 10);
    }

    @Test
    void refreshCache_ClearsAllCachedData() {
        // Arrange
        cache.put("London", createTestWeatherData("London"));
        cache.put("Paris", createTestWeatherData("Paris"));
        assertEquals(2, cache.size());

        // Act
        weatherService.refreshCache();

        // Assert
        assertEquals(0, cache.size());
    }

    @Test
    void getWeatherByCity_CityNameIsNormalized() {
        // Arrange
        String cityName = "  New York  ";
        String normalizedCityName = "New York";
        OpenWeatherResponse mockResponse = createMockOpenWeatherResponse(normalizedCityName);
        when(mockHttpClient.getWeatherByCity(normalizedCityName)).thenReturn(mockResponse);

        // Act
        WeatherData result = weatherService.getWeatherByCity(cityName);

        // Assert
        assertNotNull(result);
        assertEquals(normalizedCityName, result.getName());
        verify(mockHttpClient).getWeatherByCity(normalizedCityName);
    }

    @Test
    void getWeatherByCity_ConcurrentAccess_DifferentCities() throws InterruptedException {
        // Arrange
        String[] cities = {"London", "Paris", "Berlin", "Madrid", "Rome"};

        for (String city : cities) {
            when(mockHttpClient.getWeatherByCity(city)).thenReturn(createMockOpenWeatherResponse(city));
        }

        int threadCount = cities.length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final String city = cities[i];
            executor.submit(() -> {
                try {
                    startLatch.await();
                    WeatherData result = weatherService.getWeatherByCity(city);
                    assertNotNull(result);
                    assertEquals(city, result.getName());
                } catch (Exception e) {
                    fail("Exception should not occur during concurrent access: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completed = endLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertTrue(completed, "All threads should complete within timeout");

        for (String city : cities) {
            verify(mockHttpClient, times(1)).getWeatherByCity(city);
        }

        for (String city : cities) {
            WeatherData cachedData = cache.get(city);
            assertNotNull(cachedData, "Data should be cached for " + city);
            assertEquals(city, cachedData.getName());
        }
    }

    @Test
    void getWeatherByCity_SequentialAccess_SameCity_UsesCache() {
        // Arrange
        String cityName = "London";
        OpenWeatherResponse mockResponse = createMockOpenWeatherResponse(cityName);
        when(mockHttpClient.getWeatherByCity(cityName)).thenReturn(mockResponse);

        // Act
        WeatherData result1 = weatherService.getWeatherByCity(cityName);
        WeatherData result2 = weatherService.getWeatherByCity(cityName);
        WeatherData result3 = weatherService.getWeatherByCity(cityName);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        verify(mockHttpClient, times(1)).getWeatherByCity(cityName);

        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }

    @Test
    void getWeatherByCity_CacheExpiration() {
        // Arrange
        WeatherCache fastExpiringCache = new WeatherCache(1L, 10); // 1 minute TTL
        WeatherService fastExpiringService = WeatherService.builder()
                .withConfig(mockConfig)
                .withCache(fastExpiringCache)
                .withHttpClient(mockHttpClient)
                .build();

        String cityName = "Madrid";
        OpenWeatherResponse mockResponse = createMockOpenWeatherResponse(cityName);
        when(mockHttpClient.getWeatherByCity(cityName)).thenReturn(mockResponse);

        // Act
        WeatherData result1 = fastExpiringService.getWeatherByCity(cityName);
        assertNotNull(result1);

        fastExpiringCache.clear();

        // Act
        WeatherData result2 = fastExpiringService.getWeatherByCity(cityName);

        // Assert
        verify(mockHttpClient, times(2)).getWeatherByCity(cityName);
    }

    @Test
    void getWeatherByCity_ExceptionPropagation() {
        // Arrange
        String cityName = "ErrorCity";
        when(mockHttpClient.getWeatherByCity(cityName))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> weatherService.getWeatherByCity(cityName));
        assertEquals("Network error", exception.getMessage());
    }

    @Test
    void getWeatherByCity_MixedAccessPattern() {
        // Arrange
        String city1 = "London";
        String city2 = "Paris";

        OpenWeatherResponse response1 = createMockOpenWeatherResponse(city1);
        OpenWeatherResponse response2 = createMockOpenWeatherResponse(city2);

        when(mockHttpClient.getWeatherByCity(city1)).thenReturn(response1);
        when(mockHttpClient.getWeatherByCity(city2)).thenReturn(response2);

        // Act
        WeatherData result1a = weatherService.getWeatherByCity(city1); // API call
        WeatherData result2a = weatherService.getWeatherByCity(city2); // API call
        WeatherData result1b = weatherService.getWeatherByCity(city1); // Cache
        WeatherData result2b = weatherService.getWeatherByCity(city2); // Cache
        WeatherData result1c = weatherService.getWeatherByCity(city1); // Cache

        // Assert
        assertNotNull(result1a);
        assertNotNull(result2a);
        assertNotNull(result1b);
        assertNotNull(result2b);
        assertNotNull(result1c);

        assertEquals(city1, result1a.getName());
        assertEquals(city2, result2a.getName());

        assertEquals(result1a, result1b);
        assertEquals(result1a, result1c);
        assertEquals(result2a, result2b);

        verify(mockHttpClient, times(1)).getWeatherByCity(city1);
        verify(mockHttpClient, times(1)).getWeatherByCity(city2);
    }

    private OpenWeatherResponse createMockOpenWeatherResponse(String cityName) {
        OpenWeatherResponse response = new OpenWeatherResponse();

        OpenWeatherResponse.WeatherInfo weatherInfo = new OpenWeatherResponse.WeatherInfo();
        weatherInfo.setMain("Clouds");
        weatherInfo.setDescription("scattered clouds");

        OpenWeatherResponse.MainInfo mainInfo = new OpenWeatherResponse.MainInfo();
        mainInfo.setTemp(15.5);
        mainInfo.setFeelsLike(14.2);

        OpenWeatherResponse.WindInfo windInfo = new OpenWeatherResponse.WindInfo();
        windInfo.setSpeed(3.5);

        OpenWeatherResponse.SysInfo sysInfo = new OpenWeatherResponse.SysInfo();
        sysInfo.setSunrise(1675751262L);
        sysInfo.setSunset(1675787560L);

        response.setWeather(List.of(weatherInfo));
        response.setMain(mainInfo);
        response.setWind(windInfo);
        response.setSys(sysInfo);
        response.setVisibility(10000);
        response.setDatetime(1675744800L);
        response.setTimezone(3600);
        response.setName(cityName);

        return response;
    }

    private WeatherData createTestWeatherData(String cityName) {
        return WeatherDataMapper.fromOpenWeatherResponse(createMockOpenWeatherResponse(cityName));
    }
}