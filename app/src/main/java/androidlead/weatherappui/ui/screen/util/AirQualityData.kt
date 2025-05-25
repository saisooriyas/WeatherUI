package androidlead.weatherappui.ui.screen.util

import androidlead.weatherappui.R
import androidx.annotation.DrawableRes

data class AirQualityItem(
    @DrawableRes val icon: Int,
    val title: String,
    val value: String
)

val AirQualityData = listOf(
    AirQualityItem(
        title = "Real Feel",
        value = "--",
        icon = R.drawable.ic_real_feel
    ),
    AirQualityItem(
        title = "Wind",
        value = "--km/h",
        icon = R.drawable.ic_wind_qality,
    ),
    AirQualityItem(
        title = "SO2",
        value = "--",
        icon = R.drawable.ic_so2
    ),
    AirQualityItem(
        title = "Rain",
        value = "--%",
        icon = R.drawable.ic_rain_chance
    ),
    AirQualityItem(
        title = "UV Index",
        value = "--",
        icon = R.drawable.ic_uv_index
    ),
    AirQualityItem(
        title = "OЗ",
        value = "--",
        icon = R.drawable.ic_o3
    )
)