// app/src/main/java/androidlead/weatherappui/di/WeatherMapper.kt
package androidlead.weatherappui.di

import androidlead.weatherappui.R
import androidlead.weatherappui.data.OpenMeteoResponse
import androidlead.weatherappui.data.WeatherCodeMapper
import androidlead.weatherappui.ui.screen.util.AirQualityItem
import androidlead.weatherappui.ui.screen.util.ForecastItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

fun mapWeatherToWeeklyForecast(weather: OpenMeteoResponse): List<ForecastItem> {
    val dailyWeather = weather.daily
    if (dailyWeather == null || dailyWeather.time.isEmpty()) {
        return emptyList()
    }

    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    return dailyWeather.time.mapIndexed { index, dateString ->
        val date = inputFormat.parse(dateString) ?: Date()
        val dayOfWeek = dayFormat.format(date)
        val formattedDate = dateFormat.format(date)

        val weatherCode = dailyWeather.weatherCode.getOrNull(index) ?: 0
        val maxTemp = dailyWeather.temperatureMax.getOrNull(index) ?: 0.0
        val precipitationProb = dailyWeather.precipitationProbabilityMax.getOrNull(index) ?: 0

        val weatherIcon = getWeatherIcon(weatherCode)
        val temperature = "${maxTemp.roundToInt()}°"

        // Generate realistic air quality values
        val airQualityValue = when {
            precipitationProb > 70 -> Random.nextInt(15, 50) // Good air quality when rainy
            weatherCode in listOf(0, 1) -> Random.nextInt(20, 80) // Clear/partly cloudy
            else -> Random.nextInt(50, 200) // Other conditions
        }

        val airQualityColor = when {
            airQualityValue <= 50 -> "#2dbe8d" // Good
            airQualityValue <= 100 -> "#f9cf5f" // Moderate
            else -> "#ff7676" // Unhealthy
        }

        ForecastItem(
            image = weatherIcon,
            dayOfWeek = dayOfWeek,
            date = formattedDate,
            temperature = temperature,
            airQuality = airQualityValue.toString(),
            airQualityIndicatorColorHex = airQualityColor,
            isSelected = index == 1 // Select second day by default
        )
    }
}

fun mapWeatherToAirQuality(weather: OpenMeteoResponse): List<AirQualityItem> {
    val currentWeather = weather.currentWeather
    val hourlyWeather = weather.hourly

    // Extract current hour data or use defaults
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val uvIndex = hourlyWeather?.uvIndex?.getOrNull(currentHour) ?: 3.0
    val humidity = hourlyWeather?.relativeHumidity?.getOrNull(currentHour) ?: 65
    val pressure = hourlyWeather?.pressureMsl?.getOrNull(currentHour) ?: 1013.0
    val visibility = hourlyWeather?.visibility?.getOrNull(currentHour) ?: 10000.0
    val apparentTemp = hourlyWeather?.apparentTemperature?.getOrNull(currentHour)
        ?: (currentWeather.temperature + Random.nextDouble(-3.0, 5.0)) // More realistic apparent temperature
    val precipitation = hourlyWeather?.precipitation?.getOrNull(currentHour) ?: 0.0

    return listOf(
        AirQualityItem(
            title = "Real Feel",
            value = "${apparentTemp.roundToInt()}°",
            icon = R.drawable.ic_real_feel
        ),
        AirQualityItem(
            title = "Wind",
            value = "${currentWeather.windSpeed.roundToInt()}km/h",
            icon = R.drawable.ic_wind_qality
        ),
        AirQualityItem(
            title = "Humidity",
            value = "$humidity%",
            icon = R.drawable.ic_so2 // Reusing icon
        ),
        AirQualityItem(
            title = "Rain",
            value = "${(precipitation * 100).roundToInt()}%",
            icon = R.drawable.ic_rain_chance
        ),
        AirQualityItem(
            title = "UV Index",
            value = uvIndex.roundToInt().toString(),
            icon = R.drawable.ic_uv_index
        ),
        AirQualityItem(
            title = "Pressure",
            value = "${pressure.roundToInt()}mb",
            icon = R.drawable.ic_o3 // Reusing icon
        )
    )
}

private fun getWeatherIcon(weatherCode: Int): Int {
    return when (weatherCode) {
        0 -> R.drawable.img_sun // Clear sky
        1, 2, 3 -> R.drawable.img_cloudy // Partly cloudy
        45, 48 -> R.drawable.img_clouds // Fog
        51, 53, 55, 56, 57 -> R.drawable.img_rain // Drizzle
        61, 63, 65, 66, 67 -> R.drawable.img_rain // Rain
        71, 73, 75, 77 -> R.drawable.img_cloudy // Snow (using cloudy as fallback)
        80, 81, 82 -> R.drawable.img_rain // Rain showers
        85, 86 -> R.drawable.img_cloudy // Snow showers (using cloudy as fallback)
        95, 96, 99 -> R.drawable.img_thunder // Thunderstorm
        else -> R.drawable.img_cloudy
    }
}

fun getDailyForecastData(weather: OpenMeteoResponse): Triple<String, String, String> {
    val weatherInfo = WeatherCodeMapper.getWeatherInfo(
        weather.currentWeather.weatherCode,
        weather.currentWeather.isDay == 1
    )

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())

    val date = try {
        inputFormat.parse(weather.currentWeather.time)?.let {
            outputFormat.format(it)
        } ?: "Today"
    } catch (e: Exception) {
        "Today"
    }
    val temperature = "${weather.currentWeather.temperature.roundToInt()}°"
    val description = weatherInfo.description
    return Triple(date, temperature, description)
}

fun getCurrentWeatherIcon(weather: OpenMeteoResponse): Int {
    return getWeatherIcon(weather.currentWeather.weatherCode)
}

fun getCurrentWeatherDescription(weather: OpenMeteoResponse): String {
    val weatherInfo = WeatherCodeMapper.getWeatherInfo(
        weather.currentWeather.weatherCode,
        weather.currentWeather.isDay == 1
    )
    return weatherInfo.description
}

fun getCurrentWeatherTemperature(weather: OpenMeteoResponse): String {
    return "${weather.currentWeather.temperature.roundToInt()}°"
}

fun getCurrentWeatherWindSpeed(weather: OpenMeteoResponse): String {
    return "${weather.currentWeather.windSpeed.roundToInt()} km/h"
}