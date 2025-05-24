package androidlead.weatherappui.di

import androidlead.weatherappui.data.OpenMeteoResponse

class WeatherRepository(private val api: WeatherApiService) {
    suspend fun getWeather(latitude: Double, longitude: Double): OpenMeteoResponse {
        return api.getWeather(latitude, longitude)
    }
}