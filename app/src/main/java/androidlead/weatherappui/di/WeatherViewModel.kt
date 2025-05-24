// app/src/main/java/androidlead/weatherappui/viewmodel/WeatherViewModel.kt
package androidlead.weatherappui.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidlead.weatherappui.data.OpenMeteoResponse

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: OpenMeteoResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class ForecastUiState {
    object Loading : ForecastUiState()
    data class Success(val forecast: OpenMeteoResponse) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState: StateFlow<ForecastUiState> = _forecastState.asStateFlow()

    private val _currentLocation = MutableStateFlow("Loading...")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun fetchWeather(latitude: Double, longitude: Double, locationName: String? = null) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            _isRefreshing.value = true

            locationName?.let { _currentLocation.value = it }

            repository.getCurrentWeather(latitude, longitude)
                .onSuccess { weather ->
                    _weatherState.value = WeatherUiState.Success(weather)
                }
                .onFailure { exception ->
                    _weatherState.value = WeatherUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }

            _isRefreshing.value = false
        }
    }

    fun fetchForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading

            repository.getWeatherForecast(latitude, longitude)
                .onSuccess { forecast ->
                    _forecastState.value = ForecastUiState.Success(forecast)
                }
                .onFailure { exception ->
                    _forecastState.value = ForecastUiState.Error(
                        exception.message ?: "Failed to load forecast"
                    )
                }
        }
    }

    fun refreshWeather(latitude: Double, longitude: Double, locationName: String? = null) {
        fetchWeather(latitude, longitude, locationName)
        fetchForecast(latitude, longitude)
    }
}