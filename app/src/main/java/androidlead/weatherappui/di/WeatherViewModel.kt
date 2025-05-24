package androidlead.weatherappui.di

import androidlead.weatherappui.data.OpenMeteoResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weather = MutableLiveData<OpenMeteoResponse>()
    val weather: LiveData<OpenMeteoResponse> = _weather

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weather.value = repository.getWeather(latitude, longitude)
        }
    }
}