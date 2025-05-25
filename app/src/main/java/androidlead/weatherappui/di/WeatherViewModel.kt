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

    private fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            _isRefreshing.value = true

            //locationName?.let { _currentLocation.value = it }

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

    private fun fetchForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            // _isRefreshing might also be managed here if calls are independent
            repository.getWeatherForecast(latitude, longitude)
                .onSuccess { forecast ->
                    _forecastState.value = ForecastUiState.Success(forecast)
                }
                .onFailure { exception ->
                    _forecastState.value = ForecastUiState.Error(
                        exception.message ?: "Failed to load forecast"
                    )
                }
            // _isRefreshing.value = false; // Set after forecast is also done if part of a single refresh op
        }
    }

    fun refreshWeather(locationData: LocationData) { // Changed parameter to LocationData
        viewModelScope.launch { // Use a single viewModelScope for the refresh operation
            _isRefreshing.value = true
            _currentLocation.value = locationData.cityName // Update location name

            // Fetch weather and forecast concurrently
            launch {
                fetchWeather(locationData.latitude, locationData.longitude)
            }
            launch {
                fetchForecast(locationData.latitude, locationData.longitude)
            }
            // Add a join or await if you need to ensure both complete before setting _isRefreshing to false
            // For simplicity, we'll assume they manage their own loading states for now,
            // and _isRefreshing is for the overall pull-to-refresh action.
            // If fetchWeather/fetchForecast don't set _isRefreshing, set it here.
            // Consider when _isRefreshing should become false.
            // If fetchWeather and fetchForecast manage their own parts of loading,
            // _isRefreshing might be set to false sooner, or after both complete.
            // For now, let's assume it's for the overall swipe action:
            // kotlinx.coroutines.joinAll(job1, job2) // If you assign the launch blocks to jobs
            _isRefreshing.value = false // Set this after both operations are initiated or completed
        }
    }
}