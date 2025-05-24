// app/src/main/java/androidlead/weatherappui/data/OpenMeteoResponse.kt
package androidlead.weatherappui.data

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("generationtime_ms")
    val generationTimeMs: Double,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int,
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    val elevation: Double,
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather,
    @SerializedName("daily")
    val daily: DailyWeather? = null,
    @SerializedName("hourly")
    val hourly: HourlyWeather? = null
)

data class CurrentWeather(
    val temperature: Double,
    @SerializedName("windspeed")
    val windSpeed: Double,
    @SerializedName("winddirection")
    val windDirection: Double,
    @SerializedName("weathercode")
    val weatherCode: Int,
    @SerializedName("is_day")
    val isDay: Int,
    val time: String
)

data class DailyWeather(
    val time: List<String>,
    @SerializedName("weathercode")
    val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerializedName("apparent_temperature_max")
    val apparentTemperatureMax: List<Double>,
    @SerializedName("apparent_temperature_min")
    val apparentTemperatureMin: List<Double>,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerializedName("rain_sum")
    val rainSum: List<Double>,
    @SerializedName("showers_sum")
    val showersSum: List<Double>,
    @SerializedName("snowfall_sum")
    val snowfallSum: List<Double>,
    @SerializedName("precipitation_hours")
    val precipitationHours: List<Double>,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>,
    @SerializedName("windspeed_10m_max")
    val windSpeedMax: List<Double>,
    @SerializedName("windgusts_10m_max")
    val windGustsMax: List<Double>,
    @SerializedName("winddirection_10m_dominant")
    val windDirectionDominant: List<Double>,
    @SerializedName("shortwave_radiation_sum")
    val shortwaveRadiationSum: List<Double>,
    @SerializedName("et0_fao_evapotranspiration")
    val evapotranspiration: List<Double>
)

data class HourlyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("relativehumidity_2m")
    val relativeHumidity: List<Int>,
    @SerializedName("apparent_temperature")
    val apparentTemperature: List<Double>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>,
    @SerializedName("precipitation")
    val precipitation: List<Double>,
    @SerializedName("rain")
    val rain: List<Double>,
    @SerializedName("showers")
    val showers: List<Double>,
    @SerializedName("snowfall")
    val snowfall: List<Double>,
    @SerializedName("weathercode")
    val weatherCode: List<Int>,
    @SerializedName("pressure_msl")
    val pressureMsl: List<Double>,
    @SerializedName("surface_pressure")
    val surfacePressure: List<Double>,
    @SerializedName("cloudcover")
    val cloudCover: List<Int>,
    @SerializedName("visibility")
    val visibility: List<Double>,
    @SerializedName("evapotranspiration")
    val evapotranspiration: List<Double>,
    @SerializedName("windspeed_10m")
    val windSpeed: List<Double>,
    @SerializedName("winddirection_10m")
    val windDirection: List<Double>,
    @SerializedName("windgusts_10m")
    val windGusts: List<Double>,
    @SerializedName("uv_index")
    val uvIndex: List<Double>
)

// Weather code mappings based on WMO Weather interpretation codes
data class WeatherInfo(
    val description: String,
    val icon: String,
    val isDay: Boolean
)

object WeatherCodeMapper {
    fun getWeatherInfo(code: Int, isDay: Boolean): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo("Clear sky", if (isDay) "☀️" else "🌙", isDay)
            1, 2, 3 -> WeatherInfo("Partly cloudy", if (isDay) "⛅" else "☁️", isDay)
            45, 48 -> WeatherInfo("Fog", "🌫️", isDay)
            51, 53, 55 -> WeatherInfo("Drizzle", "🌦️", isDay)
            56, 57 -> WeatherInfo("Freezing drizzle", "🌦️", isDay)
            61, 63, 65 -> WeatherInfo("Rain", "🌧️", isDay)
            66, 67 -> WeatherInfo("Freezing rain", "🌧️", isDay)
            71, 73, 75 -> WeatherInfo("Snow fall", "❄️", isDay)
            77 -> WeatherInfo("Snow grains", "❄️", isDay)
            80, 81, 82 -> WeatherInfo("Rain showers", "🌦️", isDay)
            85, 86 -> WeatherInfo("Snow showers", "🌨️", isDay)
            95 -> WeatherInfo("Thunderstorm", "⛈️", isDay)
            96, 99 -> WeatherInfo("Thunderstorm with hail", "⛈️", isDay)
            else -> WeatherInfo("Unknown", "❓", isDay)
        }
    }
}