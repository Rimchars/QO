package com.example.qo


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class sendmessage(private val context: Context) {

    fun sendmessage(message:String){
        val job = GlobalScope.launch(Dispatchers.IO) {
            val app = context.applicationContext as socket
            val socket = app.getSocketInstance()
            val outputStream = socket?.getOutputStream()
            outputStream?.write(message.toByteArray())
        }
    }
}