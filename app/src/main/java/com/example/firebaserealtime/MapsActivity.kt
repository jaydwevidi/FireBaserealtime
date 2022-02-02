package com.example.firebaserealtime

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.firebaserealtime.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var viewModel : VMLocalLocationUpdate
    private lateinit var serviceVM: ServiceVM
    private lateinit var fullName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setName()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupViewModels()
        addMarkerOnMap()
        addListeners()
    }

    private fun setName(){
        val rand : Int = (Math.random() *  1000).toInt()
        val re = Regex("[^A-Za-z0-9 ]")
        fullName = intent.getStringExtra("MFullName")!!
        fullName = fullName + "  " + android.os.Build.MODEL + " " + rand
        fullName = re.replace(fullName, "-")
    }

    private fun setupViewModels(){
        viewModel = ViewModelProvider(this )[VMLocalLocationUpdate::class.java]
        serviceVM = ViewModelProvider(this)[ServiceVM::class.java]

        serviceVM.setup(fullName)
        viewModel.setup(fullName)
    }

    private fun addMarkerOnMap(){
        viewModel.getLocationOnce(this ,this )

        viewModel.location.observe(this) {
            val sydney = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Your Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    private fun addListeners(){

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