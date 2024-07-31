package com.diary.digitaldiary.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import java.io.IOException
import java.util.*

class LocationHelper(private val context: Context) {

    private var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var locationListener: LocationListener? = null

    fun startLocationUpdates(listener: (Location) -> Unit) {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                listener(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (checkLocationPermission()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener as LocationListener
            )
        }
    }

    fun stopLocationUpdates() {
        locationListener?.let { locationManager.removeUpdates(it) }
    }

    private fun checkLocationPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(context as AppCompatActivity, arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE)
            return false
        }
        // Permission is already granted
        return true
    }


    fun fetchTownName(latitude: Double, longitude: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val townName = addresses[0].locality
                    callback(townName)
                } else {
                    callback(null)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callback(null)
        }
    }
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
