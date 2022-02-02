package com.example.firebaserealtime

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel

class ServiceVM: ViewModel() {

    private lateinit var fullName : String

    fun setup(fullname : String){
        fullName = fullname
    }

    fun startLocationService(activity: MapsActivity , context:Context){
        val intent = Intent(context , LocationService::class.java)
        intent.putExtra("FullName" , fullName)
        intent.action = Constants.ACTION_START_LOCATION_SERVICE
        activity.startService(intent)
        Toast.makeText(context, "Service Starting", Toast.LENGTH_SHORT).show()
    }

    fun stopLocationService(activity: MapsActivity , context: Context){
        val intent = Intent(context , LocationService::class.java)
        intent.putExtra("FullName" , fullName)
        intent.action = Constants.ACTION_STOP_LOCATION_SERVICE
        activity.startService(intent)
        Toast.makeText(context, "Service Stopping", Toast.LENGTH_SHORT).show()
    }
}