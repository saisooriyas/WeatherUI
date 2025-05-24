package androidlead.weatherappui.data

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)