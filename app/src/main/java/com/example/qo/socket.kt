package com.example.qo

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Socket

class socket : Application() {
    var socket: Socket? = null

    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch(Dispatchers.IO) {
            socket = Socket("10.160.67.226", 8080)
        }
    }
    fun getSocketInstance(): Socket? {
        return socket
    }
}