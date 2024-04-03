package com.example.travelwithus.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.travelwithus.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class GalleryFragment : Fragment() {
    private lateinit var root:View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var galleryViewModel: GalleryViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_gallery, container, false)

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val buttonGetWeather = root.findViewById<Button>(R.id.btGetWeather)
        buttonGetWeather.setOnClickListener {
            Toast.makeText(requireContext(), "Getting weather data", Toast.LENGTH_LONG).show()

            obtainLocation()
        }
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permission_granted = true
        for (i in 0 until permissions.size) {
            val grantResult = grantResults[i]
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                permission_granted = false
            }
        }
        if( permission_granted) {
            getWeatherInformation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeatherInformation() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(requireContext())

        // Request a string response  from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, galleryViewModel.WEATHER_URL,
            Response.Listener<String> { response ->
                showWeatherInformation(response, 0)
            },
            // In case of any error
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Could not get weather information", Toast.LENGTH_SHORT).show();
            })
        queue.add(stringReq)
        val stringReq2 = StringRequest(Request.Method.GET, galleryViewModel.WEATHER_URL2,
            Response.Listener<String> { response ->
                showWeatherInformation(response, 1)
            },
            // In case of any error
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Could not get weather information", Toast.LENGTH_SHORT).show();
            })
        queue.add(stringReq2)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun showWeatherInformation(jsonWeather: String, type: Int) {
        val obj = JSONObject(jsonWeather)
        val arr = obj.getJSONArray("data")  // weather info is in the array called data
        var objData = arr.getJSONObject(0)  // get position 0 of the array

        if (type==0){

            val temperature = root.findViewById<TextView>(R.id.info_temperature)
            temperature.text = objData.getString("temp") + "ºC"

            val humidity = root.findViewById<TextView>(R.id.info_humidity)
            humidity.text = objData.getString("rh") + "%"

            val wind = root.findViewById<TextView>(R.id.info_wind)
            wind.text = objData.getString("wind_spd") + "m/s"

            val city = root.findViewById<TextView>(R.id.info_loc)
            city.text = objData.getString("city_name")

            val objWeather = objData.getJSONObject("weather")
            val description = root.findViewById<TextView>(R.id.info_mood)
            description.text = objWeather.getString("description")

            val imageIconCode = objWeather.getString("icon")
            val drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon = root.findViewById<ImageView>(R.id.info_image)
            icon.setImageResource(drawableResourceId)

        }

        else{
            //DAY 0
            val min0 = root.findViewById<TextView>(R.id.info_min_temp_day0)
            min0.text = objData.getString("min_temp") + "º"
            val max0 = root.findViewById<TextView>(R.id.info_max_temp_day0)
            max0.text = objData.getString("max_temp") + "º"

            var objWeather = objData.getJSONObject("weather")
            var imageIconCode = objWeather.getString("icon")
            var drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon0 = root.findViewById<ImageView>(R.id.info_image_day0)
            icon0.setImageResource(drawableResourceId)

            //DAY 1
            objData = arr.getJSONObject(1)  // get position 0 of the array
            var datetime = objData.getString("datetime")
            var date = LocalDate.parse(datetime).dayOfWeek

            val min1 = root.findViewById<TextView>(R.id.info_min_temp_day1)
            min1.text = objData.getString("min_temp") + "º"
            val max1 = root.findViewById<TextView>(R.id.info_max_temp_day1)
            max1.text = objData.getString("max_temp") + "º"
            val disp1 = root.findViewById<TextView>(R.id.info_text_day1)
            disp1.text = date.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            objWeather = objData.getJSONObject("weather")
            imageIconCode = objWeather.getString("icon")
            drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon1 = root.findViewById<ImageView>(R.id.info_image_day1)
            icon1.setImageResource(drawableResourceId)

            //DAY 2
            objData = arr.getJSONObject(2)  // get position 0 of the array
            datetime = objData.getString("datetime")
            date = LocalDate.parse(datetime).dayOfWeek

            val min2 = root.findViewById<TextView>(R.id.info_min_temp_day2)
            min2.text = objData.getString("min_temp") + "º"
            val max2 = root.findViewById<TextView>(R.id.info_max_temp_day2)
            max2.text = objData.getString("max_temp") + "º"
            val disp2 = root.findViewById<TextView>(R.id.info_text_day2)
            disp2.text = date.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            objWeather = objData.getJSONObject("weather")
            imageIconCode = objWeather.getString("icon")
            drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon2 = root.findViewById<ImageView>(R.id.info_image_day2)
            icon2.setImageResource(drawableResourceId)

            //DAY 3
            objData = arr.getJSONObject(3)  // get position 0 of the array
            datetime = objData.getString("datetime")
            date = LocalDate.parse(datetime).dayOfWeek

            val min3 = root.findViewById<TextView>(R.id.info_min_temp_day3)
            min3.text = objData.getString("min_temp") + "º"
            val max3 = root.findViewById<TextView>(R.id.info_max_temp_day3)
            max3.text = objData.getString("max_temp") + "º"
            val disp3 = root.findViewById<TextView>(R.id.info_text_day3)
            disp3.text = date.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            objWeather = objData.getJSONObject("weather")
            imageIconCode = objWeather.getString("icon")
            drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon3 = root.findViewById<ImageView>(R.id.info_image_day3)
            icon3.setImageResource(drawableResourceId)

            //DAY 4
            objData = arr.getJSONObject(4)  // get position 0 of the array
            datetime = objData.getString("datetime")
            date = LocalDate.parse(datetime).dayOfWeek

            val min4 = root.findViewById<TextView>(R.id.info_min_temp_day4)
            min4.text = objData.getString("min_temp") + "º"
            val max4 = root.findViewById<TextView>(R.id.info_max_temp_day4)
            max4.text = objData.getString("max_temp") + "º"
            val disp4 = root.findViewById<TextView>(R.id.info_text_day4)
            disp4.text = date.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            objWeather = objData.getJSONObject("weather")
            imageIconCode = objWeather.getString("icon")
            drawableResourceId = resources.getIdentifier(imageIconCode,"drawable", requireContext().packageName)
            val icon4 = root.findViewById<ImageView>(R.id.info_image_day4)
            icon4.setImageResource(drawableResourceId)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtainLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        } else {
            val location_text = root.findViewById<EditText>(R.id.editTextLocation).text
            if (location_text.isEmpty()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    galleryViewModel.WEATHER_URL = "https://api.weatherbit.io/v2.0/current?lat=${location?.latitude}&lon=${location?.longitude}&key=${galleryViewModel.API_KEY}"
                    galleryViewModel.WEATHER_URL2 = "https://api.weatherbit.io/v2.0/forecast/daily?lat=${location?.latitude}&lon=${location?.longitude}&key=${galleryViewModel.API_KEY}"
                    getWeatherInformation()
                }
            }
            else{
                galleryViewModel.WEATHER_URL = "https://api.weatherbit.io/v2.0/current?city=${location_text}&key=${galleryViewModel.API_KEY}"
                galleryViewModel.WEATHER_URL2 = "https://api.weatherbit.io/v2.0/forecast/daily?city=${location_text}&key=${galleryViewModel.API_KEY}"
                getWeatherInformation()
            }
        }
    }
}