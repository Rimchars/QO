package com.example.qo

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
class helper()
{
    val id = MainActivity.id
    val client = OkHttpClient()
    fun sendmessage(context: Context, message: String) {
        val wsParam = WsParam(context) // Move this line here
        val request = Request.Builder().url(wsParam.create_url()).build()
        val db = DatabaseHelper(context)
        // Create a new JsonObject and add role and content to it
        val jsonObject = JsonObject()
        Log.i("message", message)
        jsonObject.addProperty("role", "user")
        jsonObject.addProperty("content", message)
        Log.i("message", jsonObject.toString())
        // Convert the JsonObject to a string
        // Save the JSON string to the database
        val js=db.readGptHelperData()
        js.add(jsonObject)
        db.insertGptHelperData("user",message)
        wsParam.getparm(id, js)
        val webSocket = client.newWebSocket(request, wsParam.listener)
        println(wsParam.getparm(id, js))
        webSocket.send(wsParam.getparm(id, js))
    }
}