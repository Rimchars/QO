package com.example.qo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.*
import java.net.Socket

class Register : AppCompatActivity() {
    lateinit var name: EditText
    lateinit var pwd: EditText
    lateinit var email: EditText
    lateinit var id: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        name = findViewById<EditText>(R.id.name)
        pwd = findViewById<EditText>(R.id.pwd)
        email = findViewById<EditText>(R.id.email)
        id = findViewById<EditText>(R.id.id)
    }

    fun signup1(view: View) {
        if (name.text.toString().isEmpty() || pwd.text.toString().isEmpty() || email.text.toString().isEmpty()) {
            val toast = Toast.makeText(this, "请完善信息", Toast.LENGTH_SHORT)
            toast.show()
        } else {
            setResult(Activity.RESULT_OK, intent)
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            val ip = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            var signupinfo =
                "register:" + id.text.toString() + ' ' + pwd.text.toString() + ' ' + name.text.toString() + ' ' + email.text.toString() +' '+ ip
            val job = GlobalScope.launch(Dispatchers.IO) {//发送数据
                val app = application as socket
                val socket = app.getSocketInstance()
                val outputStream = socket!!.getOutputStream()
                outputStream.write(signupinfo.toByteArray())
                outputStream.flush()
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val len = inputStream.read(buffer)
                val str = String(buffer, 0, len)
                if ("true" in str) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "注册成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "用户名已存在", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }
        }
    }
}