package com.example.travelwithus

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class forum_1 : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUser: String? = null
    private var photoReference: String? = null
    private lateinit var viewImage: ImageView
    private val GALLERY_REQUEST_CODE = 13
    private val CAMERA_REQUEST_CODE = 200

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum2)
        findViewById<ImageView>(R.id.current).setOnClickListener{
            getDeviceLocation()
        }

        viewImage = findViewById(R.id.imagePopInd)
        findViewById<ImageView>(R.id.imagePopInd).setOnClickListener {
            requestPermission(this, CAMERA_REQUEST_CODE)
        }

        findViewById<Button>(R.id.btn_yes).setOnClickListener {
            val texto = findViewById<EditText>(R.id.tvpost).text.toString()
            val country = findViewById<EditText>(R.id.tvBody).text.toString()
            val Lat = findViewById<EditText>(R.id.lat).text.toString()
            val Long = findViewById<EditText>(R.id.log).text.toString()

            val cityName = findViewById<EditText>(R.id.tvTitle).text.toString()
            if (cityName.isNotEmpty() && country.isNotEmpty() && texto.isNotEmpty()) {
                lifecycleScope.launch {
                    currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                    val db = Firebase.firestore
                    val usersRef =
                        Firebase.firestore.collection("users").whereEqualTo("id", currentUser)
                            .get().await()
                    photoReference = UUID.randomUUID().toString()

                    val name = usersRef.documents[0].get("firstName").toString()
                    val lastname = usersRef.documents[0].get("lastName").toString()
                    val post = hashMapOf(
                        "idUserPosted" to currentUser,
                        "city" to cityName,
                        "country" to country,
                        "name" to name,
                        "lastname" to lastname,
                        "latitude" to Lat,
                        "longitude" to Long,
                        "message" to texto,
                        "photoRef" to photoReference,
                        "createdAt" to Timestamp.from(Instant.now())
                    )
                    val newPost = Firebase.firestore.collection("Forum").add(post)
                    newPost.addOnCompleteListener {
                        Toast.makeText(this@forum_1, "Post Added!", Toast.LENGTH_LONG).show()
                        uploadImage(photoReference + ".jpg")
                        finish()
                    }
                }
            } else {
                Toast.makeText(this@forum_1, "You need to fill Country, City and POST text",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImage(fileName: String) {

        val imageBitMap =  viewImage.drawToBitmap()
        val imageData = getFileDataFromDrawable(imageBitMap)
        val postURL = "http://simfctan.atwebpages.com/upload_file.php"
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                println("response is: $it")
            },
            Response.ErrorListener {
                println("error is: $it")
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["file"] = FileDataPart(fileName, imageData!!, "jpeg")
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    fun requestPermission(activity: Activity?, requestCode: Int) {
        Log.e("anv","Entrei no request")
        if (requestCode == GALLERY_REQUEST_CODE) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode
            )
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.CAMERA),
                requestCode
            )
        }
    }

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if the app has the necessary permissions to access the device's location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the necessary permissions if they are not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)

            return
        }

        // Get the last known location of the device
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Location was successfully retrieved
                if (location != null) {
                    // You can use the location object to get the latitude and longitude
                    val latitude = location.latitude
                    val longitude = location.longitude

                    currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                    var country: String?
                    var cityName: String?
                    val geoCoder = Geocoder(this, Locale.getDefault())
                    val address = geoCoder.getFromLocation(latitude,longitude,1)

                    country = address!!.get(0)?.countryName
                    cityName = address!!.get(0)?.locality
                    findViewById<EditText>(R.id.log).setText(latitude.toString())
                    findViewById<EditText>(R.id.lat).setText(longitude.toString())
                    findViewById<EditText>(R.id.tvTitle).setText(cityName)
                    findViewById<EditText>(R.id.tvBody).setText(country)

                }
            }
            .addOnFailureListener { exception: Exception ->
                // Failed to get the location
                // You can handle the exception here
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var granted = true
        if (grantResults.isNotEmpty()) {
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                }
            }
        }
        if (!granted) {
            Toast.makeText(this,  "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        when ( requestCode ) {
            CAMERA_REQUEST_CODE ->{
                capturePhotoFromCamera()
                return
            }
            GALLERY_REQUEST_CODE -> {
                chooseImageFromGallery()
                return
            }
        }
    }
    private fun chooseImageFromGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent,GALLERY_REQUEST_CODE)
    }

    private fun capturePhotoFromCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("onActivityResult","requestCode: ${requestCode} resultCode: ${resultCode}")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE && data != null){
            viewImage.setImageBitmap(data.extras?.get("data") as Bitmap)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE && data != null){
            val uri: Uri? = data.data
            viewImage.setImageURI(uri)
        }
    }
}
