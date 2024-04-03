package com.example.travelwithus

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.travelwithus.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var lastSelectedMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyAEEkBSytj5dRRmIdAH5XwcAshCrxYcFTs");
        }
        binding = ActivityMapsBinding.inflate(inflater, container, false)
        val icon = BitmapFactory.decodeResource(resources, R.drawable.image)
        val newWidth = 50
        val newHeight = 50
        val scaledIcon = Bitmap.createScaledBitmap(icon, newWidth, newHeight, false)
        val db = Firebase.firestore
        lifecycleScope.launch {
            val people = mutableListOf<Pair<String, String>>()
            val cities = db.collection("Forum")
                .get().await()
            cities.forEach {
                people.add(Pair(it.get("city").toString(), it.get("name").toString()))
            }
            people.distinctBy { it.first }
            val geocoder = Geocoder(requireContext())
            for (i in people.indices) {
                val address = geocoder.getFromLocationName(people[i].first, 1)?.first()
                val latLng1 = LatLng(address?.latitude ?: 0.0, address?.longitude ?: 0.0)
                val marker1 =
                    mMap.addMarker(MarkerOptions().position(latLng1).title(people[i].second))?.setIcon(
                        BitmapDescriptorFactory.fromBitmap(scaledIcon))
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

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
            //getWeatherInformation()
            getDeviceLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        getDeviceLocation()
    }

    @SuppressLint("MissingPermission")
    private  fun getDeviceLocation() {
        val placesClient : PlacesClient = Places.createClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(requireActivity(), permissions, 0)
            return
        } else {
            val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES))
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location!!.latitude, location.longitude)))
                mMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10F))
                val currentLocation = LatLng(location.latitude, location.longitude)
                val autocompleteFragment = childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                    override fun onPlaceSelected(place: Place) {
                        // Do something with the selected place
                        val latLng = place.latLng
                        if (lastSelectedMarker != null) {
                            lastSelectedMarker?.remove()
                        }
                        val marker = mMap?.addMarker(MarkerOptions().position(latLng!!).title(place.name))
                        lastSelectedMarker = marker
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                    }

                    override fun onError(status: Status) {
                        // Handle the error
                        Log.i("PlaceSelectionError", status.statusMessage.toString())
                    }
                })
            }
            placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                val nearbyHotels = response.placeLikelihoods.filter {
                    it.place.types?.contains(Place.Type.RESTAURANT) == true || it.place.types?.contains(Place.Type.LODGING) == true || it.place.types?.contains(Place.Type.GAS_STATION) == true
                }
                val icon = BitmapFactory.decodeResource(resources, R.drawable.image1)
                val newWidth = 70
                val newHeight = 70
                val scaledIcon = Bitmap.createScaledBitmap(icon, newWidth, newHeight, false)
                //por alguma razão está a retornar todos os nearby places portanto o turista ficará a saber onde se encontra o pingo doce
                nearbyHotels.forEach {
                    val hotelName = it.place.name
                    val hotelAddress = it.place.address
                    val hotelLatLng = it.place.latLng
                    // Add the hotel to the map using the hotelLatLng and other information
                    mMap.addMarker(MarkerOptions().position(hotelLatLng!!).title("Restaurant").icon(BitmapDescriptorFactory.fromBitmap(scaledIcon)))
                }
            }.addOnFailureListener { exception ->

            }
        }
    }
}