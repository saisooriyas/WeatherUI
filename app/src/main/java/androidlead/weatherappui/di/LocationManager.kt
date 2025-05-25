// app/src/main/java/androidlead/weatherappui/location/LocationManager.kt
package androidlead.weatherappui.di

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.withContext // Ensure this is imported

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "Unknown Location"
)

class AppLocationManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getCurrentLocation(): Result<LocationData> = suspendCancellableCoroutine { continuation ->
        Log.d("AppLocationManager", "----------------------------------------------------") // Separator
        Log.d("AppLocationManager", "Enter getCurrentLocation()")

        if (!hasLocationPermission()) {
            Log.w("AppLocationManager", "getCurrentLocation: Location permission NOT granted. Failing early.")
            if (continuation.isActive) {
                continuation.resume(Result.failure(SecurityException("Location permission not granted")))
            }
            return@suspendCancellableCoroutine
        }
        Log.d("AppLocationManager", "getCurrentLocation: Location permission IS granted.")

        if (!isLocationEnabled()) {
            Log.w("AppLocationManager", "getCurrentLocation: Location services are DISABLED. Failing early.")
            if (continuation.isActive) {
                continuation.resume(Result.failure(Exception("Location services are disabled")))
            }
            return@suspendCancellableCoroutine
        }
        Log.d("AppLocationManager", "getCurrentLocation: Location services ARE enabled.")

        Log.d("AppLocationManager", "getCurrentLocation: Proceeding to request location updates.")

        try {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this) // Crucial: unregister

                    // Launch a coroutine to handle suspend function call for geocoding
                    // Use the continuation's context to stay within the lifecycle of this operation
                    kotlinx.coroutines.CoroutineScope(continuation.context + Dispatchers.Main).launch {
                        Log.d("AppLocationManager", "Location changed: ${location.latitude}, ${location.longitude}. Fetching city name...")
                        val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)
                        Log.d("AppLocationManager", "City name fetched: $cityName")
                        val locationData = LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            cityName = cityName
                        )
                        if (continuation.isActive) { // Check again before resuming
                            continuation.resume(Result.success(locationData))
                        }
                    }
                }

                override fun onProviderEnabled(provider: String) {
                    Log.d("AppLocationManager", "Provider enabled: $provider")
                }

                override fun onProviderDisabled(provider: String) {
                    Log.w("AppLocationManager", "Provider disabled: $provider")
                }
            }

            // ... (rest of the provider trying logic) ...

            val providers = locationManager.getProviders(true) // Get enabled providers
            if (providers.isNullOrEmpty()) {
                Log.w("AppLocationManager", "No location providers available/enabled.")
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception("No location providers available")))
                }
                return@suspendCancellableCoroutine
            }

            var locationRequested = false
            for (provider in providers) {
                Log.d("AppLocationManager", "Attempting to request updates from provider: $provider")
                try {
                    // Check if the provider is one you want to use (e.g., GPS or Network)
                    if (provider == LocationManager.GPS_PROVIDER || provider == LocationManager.NETWORK_PROVIDER) {
                        // Request updates: minTimeMs = 0 to get updates as fast as possible (adjust as needed)
                        // minDistanceM = 0 to get updates regardless of distance change (adjust as needed)
                        locationManager.requestLocationUpdates(provider, 0L, 0f, locationListener, Looper.getMainLooper())
                        Log.d("AppLocationManager", "Successfully requested updates from $provider")
                        locationRequested = true
                        // Typically, you only need updates from one good provider.
                        // You might break here or have logic to prefer GPS then Network.
                        // For simplicity, let's say we request from all suitable ones and the first one to respond wins
                        // (as your current listener removes updates on first change).
                    }
                } catch (ex: SecurityException) {
                    Log.e("AppLocationManager", "SecurityException requesting updates from $provider", ex)
                    // Potentially resume with failure or try next provider
                } catch (ex: Exception) {
                    Log.e("AppLocationManager", "Exception requesting updates from $provider", ex)
                    // Potentially resume with failure or try next provider
                }
            }
            if (!locationRequested) {
                Log.w("AppLocationManager", "No suitable provider found or failed to request updates from any provider.")
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception("Failed to request location updates from any provider")))
                }
                return@suspendCancellableCoroutine
            }

            // Make sure to remove the listener if the coroutine is cancelled
            continuation.invokeOnCancellation {
                Log.d("AppLocationManager", "getCurrentLocation coroutine cancelled, removing updates.")
                locationManager.removeUpdates(locationListener)
            }

        } catch (e: SecurityException) {
            Log.e("AppLocationManager", "Outer SecurityException in getCurrentLocation", e)
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        } catch (e: Exception) {
            Log.e("AppLocationManager", "Outer Exception in getCurrentLocation", e)
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }


    suspend fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) {
            Log.w("AppLocationManager", "getLastKnownLocation: No location permission.")
            return null
        }

        try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

            for (provider in providers) {
                // Important: getLastKnownLocation can return null if no location is cached for this provider
                @Suppress("MissingPermission") // We check hasLocationPermission() above
                val location: Location? = locationManager.getLastKnownLocation(provider)

                if (location != null) {
                    Log.d(
                        "AppLocationManager",
                        "Found last known location with $provider: ${location.latitude}, ${location.longitude}"
                    )
                    // Since getCityNameFromCoordinates is a suspend function, call it directly.
                    // It's good practice to ensure it runs on an appropriate dispatcher (e.g., Dispatchers.IO for Geocoder)
                    // Your getCityNameFromCoordinates already uses withContext(Dispatchers.IO) which is good.
                    val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)
                    Log.d("AppLocationManager", "City name for last known location: $cityName")

                    return LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        cityName = cityName
                    )
                } else {
                    Log.d("AppLocationManager", "No last known location found with $provider.")
                }
            }
        } catch (e: SecurityException) {
            Log.e("AppLocationManager", "SecurityException in getLastKnownLocation", e)
            // Depending on your app's logic, you might want to inform the user or try a default.
        } catch (e: Exception) {
            // Catching a general Exception can hide specific issues.
            // Consider more specific catches if needed (e.g., IOException for geocoder).
            Log.e("AppLocationManager", "Exception in getLastKnownLocation", e)
        }

        Log.d("AppLocationManager", "No last known location found from any provider.")
        return null // Return null if no location was found from any provider or an error occurred
    }

    // In AppLocationManager.kt

    // 1. Make getCityName suspend and handle Tiramisu Geocoder properly
    private suspend fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String =
        withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            Log.w("AppLocationManager", "Geocoder service not available.")
            return@withContext "Unknown (No Geocoder)"
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val cityName = addresses.firstOrNull()?.let { addr ->
                            addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown (T+)"
                        } ?: "Unknown (T+ no addr)"
                        Log.d("AppLocationManager", "T+ Geocoded: $cityName for $latitude, $longitude")
                        if (continuation.isActive) continuation.resume(cityName)
                    }
                    continuation.invokeOnCancellation {
                        Log.d("AppLocationManager", "T+ Geocoding cancelled for $latitude, $longitude")
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                val cityName = addresses?.firstOrNull()?.let { addr ->
                    addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown (Pre-T)"
                } ?: "Unknown (Pre-T no addr)"
                Log.d("AppLocationManager", "Pre-T Geocoded: $cityName for $latitude, $longitude")
                cityName
            }
        } catch (e: Exception) {
            Log.e("AppLocationManager", "Error geocoding $latitude, $longitude", e)
            "Unknown (Error)"
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    companion object {

        const val LOCATION_PERMISSION_REQUEST_CODE = 100

        // Default locations for fallback
        val DEFAULT_LOCATIONS = listOf(
            LocationData(51.5074, -0.1278, "London"),
            LocationData(40.7128, -74.0060, "New York"),
            LocationData(35.6762, 139.6503, "Tokyo"),
            LocationData(48.8566, 2.3522, "Paris"),
            LocationData(-33.8688, 151.2093, "Sydney")
        )
    }
}