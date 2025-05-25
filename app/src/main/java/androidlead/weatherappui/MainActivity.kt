package androidlead.weatherappui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidlead.weatherappui.ui.screen.WeatherScreen
import androidlead.weatherappui.ui.theme.WeatherAppUiTheme
// Add these imports to the top of your MainActivity.kt file:

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {

    private var hasLocationPermission by mutableStateOf(false)
    private var showPermissionRationale by mutableStateOf(false)
    private var permissionPermanentlyDenied by mutableStateOf(false)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        hasLocationPermission = fineLocationGranted || coarseLocationGranted

        if (!hasLocationPermission) {
            val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (!shouldShowRationale) {
                // Permission permanently denied
                permissionPermanentlyDenied = true
            } else {
                showPermissionRationale = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLocationPermission()

        setContent {
            WeatherAppUiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        hasLocationPermission -> {
                            WeatherScreen()
                        }
                        permissionPermanentlyDenied -> {
                            PermissionPermanentlyDeniedScreen(
                                onOpenSettings = ::openAppSettings
                            )
                        }
                        showPermissionRationale -> {
                            PermissionRationaleScreen(
                                onGrantPermission = ::requestLocationPermission,
                                onDeny = { showPermissionRationale = false }
                            )
                        }
                        else -> {
                            PermissionRequestScreen(
                                onRequestPermission = ::requestLocationPermission
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkLocationPermission() {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        showPermissionRationale = false
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (permissionPermanentlyDenied) {
            // Check if user granted permission from settings
            checkLocationPermission()
            if (hasLocationPermission) {
                permissionPermanentlyDenied = false
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This app needs location access to provide accurate weather information for your current location.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun PermissionRationaleScreen(
    onGrantPermission: () -> Unit,
    onDeny: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Why We Need Location",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location permission allows us to:\n\n" +
                    "• Show weather for your current location\n" +
                    "• Provide accurate local forecasts\n" +
                    "• Send location-based weather alerts\n\n" +
                    "Your location data is only used for weather services and is not shared with third parties.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGrantPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onDeny,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue Without Location")
        }
    }
}

@Composable
fun PermissionPermanentlyDeniedScreen(
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location permission has been permanently denied. To use this app, please enable location permission in your device settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Settings")
        }
    }
}