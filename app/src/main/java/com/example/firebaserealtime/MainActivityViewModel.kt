package com.example.firebaserealtime

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MainActivityViewModel : ViewModel() {

    val location = MutableLiveData<Location>()
    private lateinit var myRef : DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest

    fun setup(fullName : String){
        myRef = Firebase.database.getReference(fullName)

        locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun stopLocationUpdates(context: Context){
        Toast.makeText(context, "Location syncing Stopped", Toast.LENGTH_SHORT).show()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            for (i in locationResult.locations){
                sendToFirebase(i)
                Log.d("MyLocal", "onLocationResult: $i")
            }
            super.onLocationResult(locationResult)
        }
    }

    fun getLocationOnce(context : Context , activity: MapsActivity){
        // and add marker on map
        Log.d("jay", "getLocationOnce: ")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity , arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION //, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) , 101)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val task = fusedLocationClient.lastLocation

        task.addOnSuccessListener {
            if(it!=null) {
                Log.d("jay", "viewModel: ${it.latitude}")
                location.value = it
                //Toast.makeText(this, "${it.latitude}", Toast.LENGTH_SHORT).show()
            }
            else {
                Log.d("jay", "viewModel: error ")
                Toast.makeText(context, "Error , it = null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendToFirebase(location : Location){
        val date: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val cTime: String = SimpleDateFormat("HH:mm:ss").format(Date())

        myRef.child(date).child(cTime).apply {
            child("Latitude").setValue(location.latitude)
            child("Longitude").setValue(location.longitude)
        }

    }

    fun checkSettingsThenStart(context: Context){ // Continuous Location
        val request = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        val client = LocationServices.getSettingsClient(context)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(request)

        val successListener = OnSuccessListener<LocationSettingsResponse> {
            startLocationUpdates(context)
        }

        val failureListener = OnFailureListener {
            Log.e("Failed", "onFailureListener: " )
        }

        task.addOnSuccessListener (successListener)
        task.addOnFailureListener (failureListener)
    }

    private fun startLocationUpdates(context: Context){

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(context, "Updates Location no permission", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "syncing to Firebase MyLocal", Toast.LENGTH_SHORT).show()
        fusedLocationClient.requestLocationUpdates(locationRequest , locationCallback , Looper.getMainLooper())
    }

}