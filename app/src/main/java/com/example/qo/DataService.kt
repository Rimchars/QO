package com.example.qo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class DataService : Service() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
        databaseHelper.creattable()
        //启动socket
        val job = GlobalScope.launch(Dispatchers.IO) {
            val app = application as socket
            val socket = app.getSocketInstance()
            val reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
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
                    Log.d("socket", "onCreate: $line")
                    if (line.startsWith("{")) {
                        val jsonObject = JSONObject(line)
                        val type = jsonObject.getString("type")
                        if (type == "send") {
                            val id1 = jsonObject.getString("id1")
                            val message = jsonObject.getString("message")
                            val id2 = jsonObject.getString("id2")
                            val time = jsonObject.getString("time")
                            var mid = ""
                            //如果是本人的id则mid=rev
                            if (id1 != MainActivity.id) {
                                mid = "rev"
                            } else mid = "send"
                            saveMessageData(mid, id1, message, id2, time)
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
                            val id1 = jsonObject.getString("id1")
                            val id2 = jsonObject.getString("id2")
                            val ship = jsonObject.getString("ship")
                            //打印
                            var id = ""
                            if (id1==MainActivity.id)
                            {
                                id=id2
                            }
                            else
                            {
                                id=id1
                            }
                            if (ship=="等待"&&id1==MainActivity.id)
                            {
                                continue
                            }
                            val cursor = databaseHelper.getContactData(id)
                            if (cursor.moveToFirst()) {
                                val oldName = cursor.getString(with(cursor) { getColumnIndex("name") })
                                val oldShip = cursor.getString(with(cursor) { getColumnIndex("ship") })
                                if (oldName != name || oldShip != ship) {
                                    saveContactData(name, id, ship)
                                }
                            } else {
                                saveContactData(name, id, ship)
                            }
                            val intent = Intent()
                            if (ship == "好友")
                                intent.action = "com.example.qo.Contacts"
                            else
                                intent.action = "com.example.qo.NewFriend"
                            intent.putExtra("status", true)
                            sendBroadcast(intent)
                        }
                    }
                    if (line.startsWith("{")) {
                        val jsonObject = JSONObject(line)
                        val type = jsonObject.getString("type")
                        if (type == "serch") {
                            val name = jsonObject.getString("name")
                            val id = jsonObject.getString("id")
                            if (id != MainActivity.id) {
                                val intent = Intent()
                                intent.action = "com.example.qo.SerchPerson"
                                intent.putExtra("name", name)
                                intent.putExtra("id", id)
                                sendBroadcast(intent)
                            }
                        }
                    }
                }
                    catch(e: Exception) {
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