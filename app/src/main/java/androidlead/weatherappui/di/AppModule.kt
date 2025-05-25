package androidlead.weatherappui.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


object AppModule {

    fun provideLocationManager(context: Context): AppLocationManager {
        return AppLocationManager(context)
    }

    fun provideWeatherRepository(): WeatherRepository {
        return WeatherRepository(NetworkModule.weatherApiService)
    }

    fun provideWeatherViewModelFactory(
        repository: WeatherRepository
    ): ViewModelProvider.Factory {
        return WeatherViewModelFactory(repository)
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Extension function for easier ViewModel creation
@androidx.compose.runtime.Composable
fun weatherViewModel(context: Context): WeatherViewModel {
    val repository = AppModule.provideWeatherRepository()
    val factory = AppModule.provideWeatherViewModelFactory(repository)
    return androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
}