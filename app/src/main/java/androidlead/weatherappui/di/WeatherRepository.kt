// app/src/main/java/androidlead/weatherappui/repository/WeatherRepository.kt
package androidlead.weatherappui.di

import androidlead.weatherappui.data.OpenMeteoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val apiService: WeatherApiService) {

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<OpenMeteoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeather(latitude, longitude)
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        Result.success(weatherData)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWeatherForecast(latitude: Double, longitude: Double): Result<OpenMeteoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWeatherForecast(latitude, longitude)
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        Result.success(weatherData)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getAirQuality(latitude: Double, longitude: Double): Result<OpenMeteoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAirQuality(latitude, longitude)
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        Result.success(weatherData)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}