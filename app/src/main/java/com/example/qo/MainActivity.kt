package com.example.qo


import kotlinx.coroutines.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var idEditText: EditText
    lateinit var pwd: EditText
    companion object {
        var id: String = ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        idEditText = findViewById(R.id.id)
        pwd = findViewById(R.id.pwd)

        // Check SharedPreferences for id and password
        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        idEditText.setText(sharedPreferences.getString("id", ""))
        pwd.setText(sharedPreferences.getString("pwd", ""))
        id = idEditText.text.toString()
    }
    fun signup(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivityForResult(intent, Activity.RESULT_OK)
    }
    fun login(view: View) {
        id = idEditText.text.toString()  // Get the current value of the EditText field
        val pwd = pwd.text.toString()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val ip = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
        var logininfo="login:"+id+' '+pwd+' '+ip
        val job=GlobalScope.launch(Dispatchers.IO) {//发送数据
            val app = application as socket
            val socket = app.getSocketInstance()
            val outputStream= socket?.getOutputStream()
            outputStream!!.write(logininfo.toByteArray())
            outputStream.flush()
            val inputStream=socket.getInputStream()
            val buffer=ByteArray(1024)
            val len=inputStream.read(buffer)
            val str=String(buffer,0,len)
            withContext(Dispatchers.Main) {
                if("true" in str){
                    Toast.makeText(application,"登录成功",Toast.LENGTH_SHORT).show()
                    //保存用户名和密码为sharedpreference
                    val sharedPreferences=getSharedPreferences("user", Context.MODE_PRIVATE)
                    val editor=sharedPreferences.edit()
                    editor.putString("id",id)
                    editor.putString("pwd",pwd)
                    editor.apply()
                    val intent1 = Intent(this@MainActivity,DataService::class.java)
                    startService(intent1)
                    val intent = Intent(this@MainActivity,MainPage::class.java)
                    startActivityForResult(intent, Activity.RESULT_OK)
                }
                else{
                    Toast.makeText(application,"用户名或密码错误",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}