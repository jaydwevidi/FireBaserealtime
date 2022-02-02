package com.example.firebaserealtime

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

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
            finish()
        }

    }









}