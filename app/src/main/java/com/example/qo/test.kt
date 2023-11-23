package com.example.qo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent= Intent(this,DataService::class.java)
        startService(intent)
    }
}