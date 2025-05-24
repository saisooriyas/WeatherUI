package androidlead.weatherappui.di

import retrofit2.http.GET
import retrofit2.http.Query
import androidlead.weatherappui.data.OpenMeteoResponse

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}