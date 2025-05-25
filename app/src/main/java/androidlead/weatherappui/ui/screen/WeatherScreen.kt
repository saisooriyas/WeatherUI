// WeatherScreen.kt - Fixed version with proper updates
package androidlead.weatherappui.ui.screen

import androidlead.weatherappui.di.AppLocationManager
import androidlead.weatherappui.di.ForecastUiState
import androidlead.weatherappui.di.NetworkModule
import androidlead.weatherappui.di.WeatherRepository
import androidlead.weatherappui.di.WeatherUiState
import androidlead.weatherappui.di.WeatherViewModel
import androidlead.weatherappui.di.getCurrentWeatherDescription
import androidlead.weatherappui.di.getCurrentWeatherTemperature
import androidlead.weatherappui.di.getDailyForecastData
import androidlead.weatherappui.di.mapWeatherToAirQuality
import androidlead.weatherappui.di.mapWeatherToWeeklyForecast
import androidlead.weatherappui.ui.screen.components.ActionBar
import androidlead.weatherappui.ui.screen.components.AirQuality
import androidlead.weatherappui.ui.screen.components.DailyForecast
import androidlead.weatherappui.ui.screen.components.WeeklyForecast
import androidlead.weatherappui.ui.screen.util.ForecastData
import androidlead.weatherappui.ui.theme.ColorBackground
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize dependencies
    val locationManager = remember { AppLocationManager(context) }
    val repository = remember { WeatherRepository(NetworkModule.weatherApiService) }
    val viewModel: WeatherViewModel = remember { WeatherViewModel(repository) }

    // Collect states
    val weatherState by viewModel.weatherState.collectAsState()
    val forecastState by viewModel.forecastState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Location state
    var locationPermissionRequested by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Initialize weather data on first launch
    LaunchedEffect(Unit) {
        initializeWeatherData(locationManager, viewModel)
    }

    // Swipe refresh state
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    val scope = rememberCoroutineScope()

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            scope.launch {
                refreshWeatherData(locationManager, viewModel)
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ColorBackground)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Action Bar with dynamic location
            ActionBarWithLocation(
                location = currentLocation,
                onLocationClick = {
                    scope.launch {
                        refreshWeatherData(locationManager, viewModel)
                    }
                }
            )

            // Daily Forecast Card
            when (weatherState) {
                is WeatherUiState.Loading -> {
                    DailyForecastLoading()
                }
                is WeatherUiState.Success -> {
                    val weatherData = (weatherState as WeatherUiState.Success).weather
                    val (date, temperature, description) = getDailyForecastData(weatherData)
                    DailyForecast(
                        forecast = description,
                        date = date,
                        temperature = temperature
                    )
                }
                is WeatherUiState.Error -> {
                    ErrorCard(
                        message = "Unable to load current weather",
                        onRetry = {
                            scope.launch {
                                refreshWeatherData(locationManager, viewModel)
                            }
                        }
                    )
                }
            }

            // Air Quality Section
            when (weatherState) {
                is WeatherUiState.Success -> {
                    val weatherData = (weatherState as WeatherUiState.Success).weather
                    val airQualityData = mapWeatherToAirQuality(weatherData)
                    AirQuality(
                        data = airQualityData,
                        onRefresh = {
                            scope.launch {
                                refreshWeatherData(locationManager, viewModel)
                            }
                        }
                    )
                }
                else -> {
                    AirQuality(
                        onRefresh = {
                            scope.launch {
                                refreshWeatherData(locationManager, viewModel)
                            }
                        }
                    )
                }
            }

            // Weekly Forecast Section
            when (forecastState) {
                is ForecastUiState.Loading -> {
                    WeeklyForecastLoading()
                }
                is ForecastUiState.Success -> {
                    val forecastData = (forecastState as ForecastUiState.Success).forecast
                    val weeklyData = mapWeatherToWeeklyForecast(forecastData)
                    WeeklyForecast(data = weeklyData)
                }
                is ForecastUiState.Error -> {
                    WeeklyForecast(data = ForecastData) // Fallback to mock data
                }
            }
        }
    }

    // Location permission dialog
    if (showLocationDialog) {
        LocationPermissionDialog(
            onPermissionGranted = {
                showLocationDialog = false
                scope.launch {
                    refreshWeatherData(locationManager, viewModel)
                }
            },
            onPermissionDenied = {
                showLocationDialog = false
                // Use default location
                val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
                scope.launch {
                    refreshWeatherData(locationManager, viewModel)
                }
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
private fun ActionBarWithLocation(
    location: String,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActionBar(
        modifier = modifier,
        location = location,
        isUpdating = false,
        onLocationClick = onLocationClick
    )
}

@Composable
private fun DailyForecastLoading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun WeeklyForecastLoading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission") },
        text = { Text("This app needs location permission to provide accurate weather information.") },
        confirmButton = {
            TextButton(onClick = onPermissionGranted) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onPermissionDenied) {
                Text("Use Default")
            }
        }
    )
}

// Helper functions
private suspend fun initializeWeatherData(
    locationManager: AppLocationManager,
    viewModel: WeatherViewModel
) {
    try {
        locationManager.getCurrentLocation()
            .onSuccess { locationData ->
                viewModel.refreshWeather(locationData)
            }
            .onFailure {
                val lastKnownLocation = locationManager.getLastKnownLocation()
                if (lastKnownLocation != null) {
                    viewModel.refreshWeather(lastKnownLocation)
                } else {
                    val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
                    viewModel.refreshWeather(defaultLocation)
                }
            }
    } catch (e: Exception) {
        val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
        viewModel.refreshWeather(defaultLocation)
    }
}

private suspend fun refreshWeatherData(
    locationManager: AppLocationManager,
    viewModel: WeatherViewModel
) {
    locationManager.getCurrentLocation()
        .onSuccess { locationData ->
            viewModel.refreshWeather(locationData)
        }
        .onFailure {
            val lastKnownLocation = locationManager.getLastKnownLocation()
            if (lastKnownLocation != null) {
                viewModel.refreshWeather(lastKnownLocation)
            } else {
                val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
                viewModel.refreshWeather(defaultLocation)
            }
        }
}

@Preview()
@Composable()
fun WeatherPreview(){
    WeatherScreen()
}