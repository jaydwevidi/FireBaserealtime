package com.example.firebaserealtime

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firebaserealtime.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    private lateinit var database : FirebaseDatabase
    private lateinit var fullName : String
    private lateinit var myRef : DatabaseReference


    private val locationCallback = object : LocationCallback(){

        override fun onLocationResult(locationResult: LocationResult) {
            for (i in locationResult.locations){

                //Log.d("TAG", "onLocationResult:  $i ")
                //val date = DateFormat.getDateInstance().format("MMMM d, yyyy ")

                val date: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
                val cTime: String = SimpleDateFormat("HH:mm:ss").format(Date())

                //myRef = myRef.child(date).child(cTime)

                myRef.child(date).child(cTime).child("Latitude").setValue(i.latitude)
                myRef.child(date).child(cTime).child("Longitude").setValue(i.longitude)

            }
            super.onLocationResult(locationResult)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationRequest = LocationRequest.create()
        locationRequest.apply {
            setInterval(2000)
            setFastestInterval(500)
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }

        getLocationFineOnce()

        fullName = intent.getStringExtra("MFullName")!!

        myRef = Firebase.database.getReference(fullName)

        binding.apply {
            startSyncButton.setOnClickListener { checkSettingsThenStart() }
            stopSyncButton.setOnClickListener { stopLocationUpdates() }
        }
        checkSettingsThenStart()
    }

    private fun startLocationUpdates(){

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Updates Location no permission", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Location syncing to Firebase", Toast.LENGTH_SHORT).show()
        fusedLocationClient.requestLocationUpdates(locationRequest , locationCallback , Looper.getMainLooper())
    }

    private fun getLocationFineOnce(){
        //Toast.makeText(this, "get Fine called", Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this , arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION //, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) , 101)
            //finish()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val task = fusedLocationClient.lastLocation

        task.addOnSuccessListener {
            if(it!=null) {
                //Toast.makeText(this, "${it.latitude} , ${it.longitude}", Toast.LENGTH_SHORT).show()
                //mapActivity()

                val sydney = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(sydney).title("Your Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
            }
            else
                Toast.makeText(this, "Error , it = null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSettingsThenStart(){ // Continuous Location
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val client = LocationServices.getSettingsClient(this);
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(request)

        val successListener = object : OnSuccessListener<LocationSettingsResponse> {
            override fun onSuccess(p0: LocationSettingsResponse?) {
                startLocationUpdates()
            }
        }

        val failureListener = object : OnFailureListener {
            override fun onFailure(p0: Exception) {
                Log.e("Failed", "onFailureListener: " )
            }
        }

        task.addOnSuccessListener (successListener)
        task.addOnFailureListener (failureListener)
    }

    private fun stopLocationUpdates(){
        Toast.makeText(this, "Location syncing Stopped", Toast.LENGTH_SHORT).show()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}