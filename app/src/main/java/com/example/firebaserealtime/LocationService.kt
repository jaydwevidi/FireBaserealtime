package com.example.firebaserealtime

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {

    private lateinit var myRef: DatabaseReference
    private lateinit var fullName: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("LocationService", "longitude = ${locationResult.lastLocation.longitude}")
            sendToFirebase(locationResult.lastLocation)
            super.onLocationResult(locationResult)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendToFirebase(location: Location) {
        val date: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val cTime: String = SimpleDateFormat("HH:mm:ss").format(Date())

        myRef.child(date).child(cTime).apply {
            child("Latitude").setValue(location.latitude)
            child("Longitude").setValue(location.longitude)
        }
    }

    private fun startLocationUpdates() {
        val channelID = "location_notification_channel"
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            channelID
        )

        notificationBuilder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Location Service")
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setContentText("Running")
            setContentIntent(pendingIntent)
            setAutoCancel(false)
            priority = NotificationCompat.PRIORITY_MAX
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (notificationManager.getNotificationChannel(channelID) == null
            ) {

                val notificationChannel = NotificationChannel(
                    channelID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )

                notificationChannel.description = "channel for updating location in the background"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "No Permission Error", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startForeground(Constants.LOCATION_SERVICE_ID, notificationBuilder.build())

    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Toast.makeText(this, "service Stopped", Toast.LENGTH_LONG).show()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fullName = intent?.getStringExtra("FullName")!!
        myRef = Firebase.database.getReference(fullName)
        val action = intent.action

        if (action != null) {
            if (action == Constants.ACTION_START_LOCATION_SERVICE)
                startLocationUpdates()
            else if (action == Constants.ACTION_STOP_LOCATION_SERVICE)
                stopLocationUpdates()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? = null
}











