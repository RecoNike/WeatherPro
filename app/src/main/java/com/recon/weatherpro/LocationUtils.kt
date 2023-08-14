package com.recon.weatherpro

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

object LocationUtils {

    fun getCurrentLocation(
        activity: FragmentActivity,
        onLocationReceived: (Float, Float) -> Unit,
        onLocationError: () -> Unit,
        forceUpdate: Boolean = false // Параметр по умолчанию установлен в false
    ) {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity)

        if (checkPermission(activity)) {
            if (forceUpdate || isLocationEnabled(activity)) { // Проверяем параметр forceUpdate
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(activity) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        onLocationError.invoke()
                    } else {
                        val lat = location.latitude.toFloat()
                        val lon = location.longitude.toFloat()
                        onLocationReceived.invoke(lat, lon)
                    }
                }
            } else {
                onLocationError.invoke()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        } else {
            requestPermission(activity, PERMISSION_REQUEST_ACCESS_LOCATION)
        }
    }


    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestPermission(activity: FragmentActivity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestCode
        )
    }

    fun checkPermission(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    var PERMISSION_REQUEST_ACCESS_LOCATION = (100..120).random()
}
