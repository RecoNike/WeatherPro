package com.recon.weatherpro

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import coil.load
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text

const val API_KEY = "2392c107c09f422d8f8141306231108"

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    lateinit var tempInfo : TextView
    lateinit var cityInfo : TextView
    lateinit var updateInfo : TextView
    lateinit var windInfo : TextView
    lateinit var pogodaInfo : TextView
    lateinit var day1 : TextView
    lateinit var day2 : TextView
    lateinit var day3 : TextView
    lateinit var image : ImageView
    lateinit var imageD1 : ImageView
    lateinit var imageD2 : ImageView
    lateinit var imageD3 : ImageView

    var lat : Float = 0.0f
    var lon : Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getButton : ImageView = findViewById(R.id.buttonGet)
        tempInfo  = findViewById(R.id.tvTemp)
        cityInfo =findViewById(R.id.tvCity)
        updateInfo = findViewById(R.id.tvUpdated)
        windInfo = findViewById(R.id.tvWind)
        pogodaInfo = findViewById(R.id.tvPogoda)

        day1 = findViewById(R.id.tvDay1)
        imageD1 = findViewById(R.id.imgDay1)

        day2 = findViewById(R.id.tvDay2)
        imageD2 = findViewById(R.id.imgDay2)

        day3 = findViewById(R.id.tvDay3)
        imageD3 = findViewById(R.id.imgDay3)

        image = findViewById(R.id.currentImage)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        getCurrentLocation()
        Log.i("MyLog", "coordssss ${lat.toString()} ${lon.toString()}")


        getButton.setOnClickListener{
            getResult(lat.toString(), lon.toString())
        }
    }

    private fun getResult(lati : String, lont : String){
        var url = "https://api.weatherapi.com/v1/forecast.json" +
                "?key=$API_KEY" +
                "&q=${lati},${lont}" +
                "&days=3&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET,
        url,
            {
                response ->
                Log.d("MyLog", "Received JSON: $response")
                val obj = JSONObject(response)
                var locationObj = obj.getJSONObject("location")
                var currentObj = obj.getJSONObject("current")
                val imageUrl = "https:" + currentObj.getJSONObject("condition").getString("icon")
                val dayTextViews = arrayOf(day1, day2, day3)
                val dayImgViews = arrayOf(imageD1, imageD2, imageD3)
                tempInfo.text = currentObj.getString("temp_c") + " C°"
                updateInfo.text = currentObj.getString("last_updated")
                cityInfo.text = locationObj.getString("name")
                windInfo.text = "Wind : " + currentObj.getString("wind_mph") +
                               " " + currentObj.getString("wind_dir")
                pogodaInfo.text = currentObj.getJSONObject("condition").getString("text")
                // Используйте Coil для загрузки изображения и отображения его в ImageView
                image.load(imageUrl) {
                    crossfade(true) // Добавление плавного перехода при загрузке изображения
                    placeholder(R.drawable.placeholder) // Установка плейсхолдера, если изображение еще не загружено
                    error(R.drawable.error) // Установка изображения ошибки, если загрузка не удалась
                }

try{
                val forecastArray = obj.getJSONObject("forecast").getJSONArray("forecastday")

                for (i in 0 until forecastArray.length()) {
                    val forecastDayObj = forecastArray.getJSONObject(i)
                    val date = forecastDayObj.getString("date")

                    val dayObj = forecastDayObj.getJSONObject("day")
                    val maxTempC = dayObj.getDouble("maxtemp_c")
                    val minTempC = dayObj.getDouble("mintemp_c")
                    val conditionText = dayObj.getJSONObject("condition").getString("text")

                    // Теперь у вас есть доступ к данным для текущего дня
                    Log.d("MyLog","Date: $date")
                    Log.d("MyLog","icon: ${forecastDayObj.getJSONObject("day")
                                                    .getJSONObject("condition")
                                                    .getString("icon")}")
                    Log.d("MyLog","Max Temp C: $maxTempC")
                    Log.d("MyLog","Min Temp C: $minTempC")
                    Log.d("MyLog","Condition Text: $conditionText \n\n\n")

                    val imgUrl = "https:" + dayObj.getJSONObject("condition").getString("icon")
                    val dayTextView = dayTextViews[i]
                    val dayImg = dayImgViews[i]
                    dayImg.load(imgUrl) {
                        crossfade(true) // Добавление плавного перехода при загрузке изображения
                        placeholder(R.drawable.placeholder) // Установка плейсхолдера, если изображение еще не загружено
                        error(R.drawable.error) // Установка изображения ошибки, если загрузка не удалась
                    }
                    dayTextView.text = "Date: $date\nMax Temp: $maxTempC°C\nMin Temp: $minTempC°C\nCondition: $conditionText"
                }
            } catch (e: JSONException) {
            e.printStackTrace()
        }




            },
            {
                Log.d("MyLog","We get this: $it")
            })
        queue.add(stringRequest)
    }

    private fun getCurrentLocation(){
        if(checkPermission())
        {
            if(isLocationEnabled()){

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task ->
                    val location : Location? = task.result
                    if(location == null){
                        Toast.makeText(applicationContext,"Huinia Happens",LENGTH_SHORT).show()
                    } else {
                        lat = location.latitude.toFloat()
                        lon = location.longitude.toFloat()
                        Log.d("MyLog"," Coords : ${location.latitude} , ${location.longitude}")
                    }
                }

            } else {
                Toast.makeText(applicationContext,"Turn on GPS",LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPrmission()
        }
    }


    private fun isLocationEnabled() : Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE)
                                                as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPrmission() {
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION),
        PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermission() : Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(applicationContext,"Done",LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext,"UNDone",LENGTH_SHORT).show()
            }
        }
    }

}