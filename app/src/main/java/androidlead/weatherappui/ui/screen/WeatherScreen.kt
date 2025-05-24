// WeatherScreen.kt - Main screen integrating all components
package androidlead.weatherappui.ui.screen

import androidlead.weatherappui.di.AppLocationManager
import androidlead.weatherappui.di.ForecastUiState
import androidlead.weatherappui.di.NetworkModule
import androidlead.weatherappui.di.WeatherRepository
import androidlead.weatherappui.di.WeatherUiState
import androidlead.weatherappui.di.WeatherViewModel
import androidlead.weatherappui.di.getCurrentWeatherDescription
import androidlead.weatherappui.di.getDailyForecastData
import androidlead.weatherappui.di.mapWeatherToAirQuality
import androidlead.weatherappui.di.mapWeatherToWeeklyForecast
import androidlead.weatherappui.ui.screen.components.ActionBar
import androidlead.weatherappui.ui.screen.components.AirQuality
import androidlead.weatherappui.ui.screen.components.DailyForecast
import androidlead.weatherappui.ui.screen.components.WeeklyForecast
import androidlead.weatherappui.ui.screen.util.ForecastData
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
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

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            refreshWeatherData(locationManager, viewModel)
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Action Bar with dynamic location
            ActionBarWithLocation(
                location = currentLocation,
                onLocationClick = {
                    refreshWeatherData(locationManager, viewModel)
                }
            )

            // Daily Forecast Card
            when (weatherState) {
                is WeatherUiState.Loading -> {
                    DailyForecastLoading()
                }
                is WeatherUiState.Success -> {
                    val (date, temperature) = getDailyForecastData((weatherState as WeatherUiState.Success).weather)
                    DailyForecast(
                        forecast = getCurrentWeatherDescription((weatherState as WeatherUiState.Success).weather),
                        date = date
                    )
                }
                is WeatherUiState.Error -> {
                    ErrorCard(
                        message = "Unable to load current weather",
                        onRetry = { refreshWeatherData(locationManager, viewModel) }
                    )
                }
            }

            // Air Quality Section
            when (weatherState) {
                is WeatherUiState.Success -> {
                    val airQualityData = mapWeatherToAirQuality((weatherState as WeatherUiState.Success).weather)
                    AirQuality(data = airQualityData)
                }
                else -> {
                    AirQuality() // Uses default data
                }
            }

            // Weekly Forecast Section
            when (forecastState) {
                is ForecastUiState.Loading -> {
                    WeeklyForecastLoading()
                }
                is ForecastUiState.Success -> {
                    val weeklyData = mapWeatherToWeeklyForecast((forecastState as ForecastUiState.Success).forecast)
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
                refreshWeatherData(locationManager, viewModel)
            },
            onPermissionDenied = {
                showLocationDialog = false
                // Use default location
                val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
                viewModel.refreshWeather(
                    defaultLocation.latitude,
                    defaultLocation.longitude,
                    defaultLocation.cityName
                )
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
    // Modified ActionBar component to accept dynamic location
    ActionBar(modifier = modifier)
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
        // Try to get current location
        locationManager.getCurrentLocation()
            .onSuccess { locationData ->
                viewModel.refreshWeather(
                    locationData.latitude,
                    locationData.longitude,
                    locationData.cityName
                )
            }
            .onFailure {
                // Fall back to last known location
                val lastKnownLocation = locationManager.getLastKnownLocation()
                if (lastKnownLocation != null) {
                    viewModel.refreshWeather(
                        lastKnownLocation.latitude,
                        lastKnownLocation.longitude,
                        lastKnownLocation.cityName
                    )
                } else {
                    // Use default location
                    val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
                    viewModel.refreshWeather(
                        defaultLocation.latitude,
                        defaultLocation.longitude,
                        defaultLocation.cityName
                    )
                }
            }
    } catch (e: Exception) {
        // Use default location as final fallback
        val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
        viewModel.refreshWeather(
            defaultLocation.latitude,
            defaultLocation.longitude,
            defaultLocation.cityName
        )
    }
}

private fun refreshWeatherData(
    locationManager: AppLocationManager,
    viewModel: WeatherViewModel
) {
    // Try to get fresh location, or use last known
    val lastKnownLocation = locationManager.getLastKnownLocation()
    if (lastKnownLocation != null) {
        viewModel.refreshWeather(
            lastKnownLocation.latitude,
            lastKnownLocation.longitude,
            lastKnownLocation.cityName
        )
    } else {
        // Use default location
        val defaultLocation = AppLocationManager.DEFAULT_LOCATIONS.first()
        viewModel.refreshWeather(
            defaultLocation.latitude,
            defaultLocation.longitude,
            defaultLocation.cityName
        )
    }
}