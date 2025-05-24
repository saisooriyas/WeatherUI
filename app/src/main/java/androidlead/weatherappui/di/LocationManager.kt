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
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "Unknown Location"
)

class AppLocationManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getCurrentLocation(): Result<LocationData> = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(Result.failure(SecurityException("Location permission not granted")))
            return@suspendCancellableCoroutine
        }

        if (!isLocationEnabled()) {
            continuation.resume(Result.failure(Exception("Location services are disabled")))
            return@suspendCancellableCoroutine
        }

        try {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)

                    val cityName = getCityName(location.latitude, location.longitude)
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        cityName = cityName
                    )

                    if (continuation.isActive) {
                        continuation.resume(Result.success(locationData))
                    }
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception("Location provider disabled")))
                    }
                }
            }

            // Try GPS first, then Network
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var requestSent = false

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        0L,
                        0f,
                        locationListener
                    )
                    requestSent = true
                    break
                }
            }

            if (!requestSent) {
                continuation.resume(Result.failure(Exception("No location providers available")))
            }

            continuation.invokeOnCancellation {
                locationManager.removeUpdates(locationListener)
            }

        } catch (e: SecurityException) {
            continuation.resume(Result.failure(e))
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    fun getLastKnownLocation(): LocationData? {
        if (!hasLocationPermission()) return null

        try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                location?.let {
                    val cityName = getCityName(it.latitude, it.longitude)
                    return LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        cityName = cityName
                    )
                }
            }
        } catch (e: SecurityException) {
            // Handle permission error
        }

        return null
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Location"
            } ?: "Unknown Location"
        } catch (e: Exception) {
            "Unknown Location"
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
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