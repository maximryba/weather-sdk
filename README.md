Weather SDK

Java SDK for accessing the OpenWeatherMap API with built-in caching, polling mode, and comprehensive error handling.
Table of Contents

    Features

    Requirements

    Installation

    API Reference

Features

    Easy Weather Data Access - Simple API for retrieving current weather data

    Dual Operation Modes - Support for both on-demand and polling modes

    Smart Caching - Automatic caching with configurable TTL and LRU eviction

    Comprehensive Error Handling - Detailed exceptions for different error scenarios

    Fully Tested - Comprehensive unit test coverage

    Configurable - Flexible configuration for different use cases

    Thread-Safe - Safe for use in concurrent environments

    No Dependencies - Minimal external dependencies

Requirements

    Java 17 or higher

    Maven 3.6+ or Gradle 7+

    OpenWeatherMap API key (Get one here)

Installation
Maven
Write in a terminal with sdk-project: mvn clean install
Add the following dependency to your pom.xml:

<dependency>
    <groupId>com.weather</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>

Gradle

Add the following dependency to your build.gradle:

implementation 'com.weather:weather-sdk:1.0.0'

Manual Installation

    Download the latest JAR file from releases

    Add the JAR to your classpath


API Reference

Common Exceptions
Exception	When Thrown	Example
CityNotFoundException	City not found by API	getWeather("NonExistentCity")
WeatherApiException	API returns error	Invalid API key, rate limit
WeatherSDKException	SDK configuration error	Null API key, network issues
API Reference
WeatherSDK

Main entry point for the SDK.
Method	Description
getInstance(String apiKey, OperationMode mode)	Get or create SDK instance
removeInstance(String apiKey)	Remove and cleanup specific instance
shutdownAll()	Shutdown all SDK instances
getWeather(String cityName)	Get weather data for city
refreshCache()	Clear cache and force fresh data
WeatherData

Building from Source
Prerequisites

    Java 17+

    Maven 3.6+

Build Steps

# Clone the repository
git clone https://github.com/maximryba/weather-sdk.git
cd weather-sdk

# Build the project
mvn clean compile

# Run tests
mvn test

# Package the library
mvn package

# Install to local Maven repository
mvn install

Performance Considerations
Caching Strategy

    TTL-based expiration: Data expires after 10 minutes (configurable)

    LRU eviction: Least recently used cities evicted when cache full

    Case-insensitive: City names normalized to lowercase

Memory Usage

    Default cache size: 10 cities

    Configurable via SDKConfig

    Each cached entry: ~1-2KB

Best Practices

    Use ON_DEMAND mode for applications with infrequent requests

    Use POLLING mode for real-time applications

    Set appropriate cache TTL based on data freshness requirements

    Monitor cache hit rates for performance tuning

    Always call cleanup to release resources
