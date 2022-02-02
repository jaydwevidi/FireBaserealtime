package com.example.firebaserealtime

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaCodec.MetricsConstants.MODE
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fullName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //fullName = intent.getStringExtra("MFullName")!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)





    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val rand : Int = (Math.random() *  1000).toInt()
        val re = Regex("[^A-Za-z0-9 ]")
        fullName = intent.getStringExtra("MFullName")!!
        fullName = fullName + "  " + android.os.Build.MODEL + " " + rand

        fullName = re.replace(fullName, "-")


        val viewModel = ViewModelProvider(this )[MainActivityViewModel::class.java]
        val serviceVM = ViewModelProvider(this)[ServiceVM::class.java]
        serviceVM.setup(fullName)
        viewModel.setup(fullName)

        viewModel.getLocationOnce(this ,this )

        viewModel.location.observe(this) {
            val sydney = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Your Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }


        binding.apply {
            startSyncButton.setOnClickListener {
                //checkSettingsThenStart()
                serviceVM.startLocationService(this@MapsActivity , this@MapsActivity)
            }
            stopSyncButton.setOnClickListener {
                //stopLocationUpdates()
                serviceVM.stopLocationService(this@MapsActivity , this@MapsActivity)
            }
            startLocalSync.setOnClickListener {
                viewModel.checkSettingsThenStart(this@MapsActivity)
            }

            stopLocalSync.setOnClickListener {
                viewModel.stopLocationUpdates(this@MapsActivity)
            }
        }
    }
}