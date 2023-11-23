package com.example.qo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.example.qo.DatabaseHelper
import com.example.qo.socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class DataService : Service() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
        //启动socket
        val job=GlobalScope.launch {
            val app = application as socket
            val socket = app.getSocketInstance()
            val reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
            //打印接收到的数据
            while (true) {
                try {
                    val stringBuilder = StringBuilder()
                    var char = reader.read().toChar()
                    while (char != '}') {
                        stringBuilder.append(char)
                        char = reader.read().toChar()
                    }
                    stringBuilder.append('}')
                    val line = stringBuilder.toString()
                    println("接收到的数据为：$line")
                    if (line.startsWith("{")) {
                        val jsonObject = JSONObject(line)
                        val type = jsonObject.getString("type")
                        if (type == "send") {
                            val mid = "rev"
                            val id1 = jsonObject.getString("id1")
                            val message = jsonObject.getString("message")
                            val id2 = jsonObject.getString("id2")
                            val time = jsonObject.getString("time")
                            saveMessageData(mid,id1,message, id2,time)
                            //发送广播
                            val intent = Intent()
                            intent.action = "com.example.qo"
                            intent.putExtra("mid", "rev")
                            intent.putExtra("id1", id1)
                            intent.putExtra("message", message)
                            intent.putExtra("time", time)
                            intent.putExtra("id2", id2)
                            sendBroadcast(intent)
                        }
                    }
                    if (line.startsWith("{")) {
                        val jsonObject = JSONObject(line)
                        val type = jsonObject.getString("type")
                        if (type == "contacts") {
                            val name = jsonObject.getString("name")
                            val id = jsonObject.getString("id")
                            val ship = jsonObject.getString("ship")
                            //打印
                            println("name:$name id:$id ship:$ship")
                            saveContactData(name, id, ship)
                            val intent = Intent()
                            if (ship == "好友")
                                intent.action = "com.example.qo.Contacts"
                            else
                                intent.action = "com.example.qo.NewFriend"
                            intent.putExtra("status",true )
                            sendBroadcast(intent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle your service start here
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    fun saveContactData(name: String, id: String, ship: String) {
        databaseHelper.insertcontactsData(name, id, ship)
    }

    fun saveMessageData(mid: String,id1: String, messages: String, id2: String, time: String) {
        databaseHelper.insertmessageData(mid,id1, messages, id2, time)
    }
}