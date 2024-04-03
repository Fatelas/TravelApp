package com.example.travelwithus.ui.slideshow

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelwithus.R
import com.example.travelwithus.databinding.FragmentSlideshowBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class UserPosts(val firstName:String, val lastName:String,val city:String, val date: Date?, val mensagem:String,val ref:String){
    override fun toString(): String{
        return "${firstName} ${lastName},${lastName},${date}, ${mensagem},${ref} "
    }
}

class SlideshowFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUser: String? = null
    val db = Firebase.firestore
    private var _binding: FragmentSlideshowBinding? = null

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        currentUser = FirebaseAuth.getInstance().currentUser.toString()
        binding.button3.setOnClickListener{
            getDeviceLocation()
        }

        listRecentPosts()

        binding.trash.setOnClickListener{
            binding.cidade.text.clear()
            binding.pais.text.clear()
        }

        binding.search.setOnClickListener {
            var city =binding.cidade.text.toString()
            var country =binding.pais.text.toString()
            if(city.isEmpty() || country.isEmpty()){
                Toast.makeText(requireContext(), "Country or City field is empty, you need to fill both fields", Toast.LENGTH_LONG).show()
                listRecentPosts()
            }
            else{
                lifecycleScope.launch() {
                    val queryUser = db.collection("Forum").whereEqualTo("city", city).whereEqualTo("country", country).get().await()
                    var listItems = ArrayList<UserPosts>()
                    queryUser.forEach {
                        val firstname = it.get("name").toString()
                        val lastname = it.get("lastname").toString()
                        val dateOR = it.getTimestamp("createdAt")?.toDate()
                        val city = it.get("city").toString()
                        val mensagem = it.get("message").toString()
                        val ref = it.get("photoRef").toString()
                        listItems.add(UserPosts(firstname,lastname, city,dateOR ,mensagem,ref))
                    }
                    val listView = binding.listaPost
                    val adapter= ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,listItems)
                    listView.adapter = adapter

                    listView.setOnItemClickListener {adapterView, view, position,id ->
                        val element = adapterView.getItemAtPosition(position) as UserPosts
                        val inflater = LayoutInflater.from(requireContext())
                        var popupView = inflater.inflate(R.layout.forum_individual_post, null)
                        var textView = popupView.findViewById<TextView>(R.id.textoPopup)
                        var textView2 = popupView.findViewById<TextView>(R.id.textoPopup1)
                        var textView4 = popupView.findViewById<TextView>(R.id.textoPopup2)
                        var textView3 = popupView.findViewById<ImageView>(R.id.imagem)
                        textView.setText(element.firstName)
                        textView2.setText(element.mensagem)
                        if(element.ref !== "") {
                            Thread {
                                val mImage: Bitmap?
                                val mWebPath = "http://simfctan.atwebpages.com/download_file.php?id=${element.ref}"
                                mImage = mLoad(mWebPath)
                                lifecycleScope.launch {
                                    textView3.setImageBitmap(mImage)
                                }
                            }.start()
                        }
                        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                        val focusable = true
                        var popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
                    }
                }
            }
        }
        return root
    }

    fun listRecentPosts(){
        lifecycleScope.launch() {
            val queryUser = db.collection("Forum").orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            var listItems = ArrayList<UserPosts>()
            queryUser.forEach {
                val firstname = it.get("name").toString()
                val lastname = it.get("lastname").toString()
                val city = it.get("city").toString()
                val dateOR = it.getTimestamp("createdAt")?.toDate()
                val ref = it.get("photoRef").toString()
                val mensagem = it.get("message").toString()
                listItems.add(UserPosts(firstname,lastname,city, dateOR,mensagem,ref))
            }
            val listView = binding.listaPost
            val adapter= ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,listItems)
            listView.adapter = adapter

            listView.setOnItemClickListener {adapterView, view, position,id ->
                val element = adapterView.getItemAtPosition(position) as UserPosts
                val inflater = LayoutInflater.from(requireContext())
                var popupView = inflater.inflate(R.layout.forum_individual_post, null)
                var textView = popupView.findViewById<TextView>(R.id.textoPopup)
                var textView2 = popupView.findViewById<TextView>(R.id.textoPopup1)
                var textView4 = popupView.findViewById<TextView>(R.id.textoPopup2)
                var textView3 = popupView.findViewById<ImageView>(R.id.imagem)
                textView.setText(element.firstName)
                textView2.setText(element.mensagem)
                textView4.setText(element.city)
                if(element.ref !== "") {
                    Thread {
                        val mImage: Bitmap?
                        val mWebPath = "http://simfctan.atwebpages.com/download_file.php?id=${element.ref}"
                        mImage = mLoad(mWebPath)
                        val inflater = LayoutInflater.from(requireContext())
                        var popupView = inflater.inflate(R.layout.forum_individual_post, null)
                        lifecycleScope.launch {
                            textView3.setImageBitmap(mImage)
                        }
                    }.start()
                }
                val width = 1000
                val height = 1500
                val focusable = true
                var popupWindow = PopupWindow(popupView, width, height, focusable)
                popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
            }
        }
    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        listRecentPosts()//onStart Called
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(requireActivity(), permissions, 0)
            return
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // You can use the location object to get the latitude and longitude
                    var latitude = location!!.latitude
                    var longitude = location!!.longitude
                    currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                    var country: String?
                    var cityName: String?
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    val address = geoCoder.getFromLocation(latitude,longitude,1)
                    country = address!!.get(0)?.countryName
                    cityName = address!!.get(0)?.locality
                    binding.cidade.setText(cityName)
                    binding.pais.setText(country)
                }
            }
        }
    }
}