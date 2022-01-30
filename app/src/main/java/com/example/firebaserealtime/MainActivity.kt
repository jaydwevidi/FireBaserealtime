package com.example.firebaserealtime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this , arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION //, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) , 102)

        findViewById<Button>(R.id.button1).setOnClickListener {
            var name : String = findViewById<EditText>(R.id.etFullName).text.toString()
            val re = Regex("[^A-Za-z0-9 ]")
            name = re.replace(name, "") // works

            val intent = Intent(this , MapsActivity::class.java)
            intent.putExtra("MFullName" , name)
            startActivity(intent)
        }

    }









}