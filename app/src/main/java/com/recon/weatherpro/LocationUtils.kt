package com.recon.weatherpro

import android.content.Context
import android.location.LocationListener
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

object LocationUtils {

    private const val TAG = "LocationUtils"

    fun getCurrentLocation(
        activity: FragmentActivity,
        onLocationReceived: (Float, Float) -> Unit,
        onLocationError: () -> Unit,
        forceUpdate: Boolean = true // Параметр по умолчанию установлен в false
    ) {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (checkPermission(activity)) {
            if (forceUpdate || isLocationEnabled(activity)) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            val lat = location.latitude.toFloat()
                            val lon = location.longitude.toFloat()
                            onLocationReceived.invoke(lat, lon)
                            locationManager.removeUpdates(this)
                        }

                        override fun onProviderDisabled(provider: String) {
                            onLocationError.invoke()
                        }

                        override fun onProviderEnabled(provider: String) {}

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    })
                } else {
                    onLocationError.invoke()
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

    val PERMISSION_REQUEST_ACCESS_LOCATION = (100)
}
