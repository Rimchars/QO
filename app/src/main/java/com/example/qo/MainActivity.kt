package com.example.qo


import kotlinx.coroutines.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var idEditText: EditText
    lateinit var pwd: EditText
    lateinit var ip: String
    companion object {
        var id: String = ""
        var islogin="false"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ip=socket().getip()
        idEditText = findViewById(R.id.id)
        pwd = findViewById(R.id.pwd)
        // Check SharedPreferences for id and password
        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        id = sharedPreferences.getString("id", "") ?: ""
        val password = sharedPreferences.getString("pwd", "") ?: ""
        idEditText.setText(id)
        pwd.setText(password)
        // Check SharedPreferences for login
        val sharedPreferences1 = getSharedPreferences("login", Context.MODE_PRIVATE)
        islogin = sharedPreferences1.getString("islogin", "") ?: ""
        Log.d("islogin", islogin)
        if (id != "" && password != "") {
            if (islogin == "true") {
                val logininfo = "login:$id $password $ip"
                sendmessage(this).sendmessage(logininfo)
                DatabaseHelper.messages_table=  "t" + id + "messages_tablee"
                DatabaseHelper.contacts_table = "t" + id + "contacts_table"
                DatabaseHelper.GptHelper_table = "t" + id + "GptHelper_table"
                val intent = Intent(this@MainActivity, MainPage::class.java)
                startActivityForResult(intent, Activity.RESULT_OK)
                finish()
            }
        }
    }
    fun signup(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivityForResult(intent, Activity.RESULT_OK)
    }
    fun login(view: View) {
        id = idEditText.text.toString()  // Get the current value of the EditText field
        val pwd = pwd.text.toString()
        var logininfo="login:"+id+' '+pwd+' '+ip
        val job=GlobalScope.launch(Dispatchers.IO) {//发送数据
            val app = application as socket
            val socket = app.getSocketInstance()
            val outputStream= socket?.getOutputStream()
            outputStream?.write(logininfo.toByteArray())
            outputStream?.flush()
            val inputStream=socket?.getInputStream()
            val buffer=ByteArray(1024)
            val len=inputStream?.read(buffer)
            val str= len?.let { String(buffer,0, it) }
            withContext(Dispatchers.Main) {
                if (str != null) {
                    if("true" in str){
                        DatabaseHelper.messages_table=  "t" + id + "messages_tablee"
                        DatabaseHelper.contacts_table = "t" + id + "contacts_table"
                        DatabaseHelper.GptHelper_table = "t" + id + "GptHelper_table"
                        Toast.makeText(application,"登录成功",Toast.LENGTH_SHORT).show()
                        //保存用户名和密码为sharedpreference
                        val sharedPreferences=getSharedPreferences("user", Context.MODE_PRIVATE)
                        val editor=sharedPreferences.edit()
                        editor.putString("id",id)
                        editor.putString("pwd",pwd)
                        editor.apply()
                        islogin="true"
                        val sharedPreferences1=getSharedPreferences("login", Context.MODE_PRIVATE)
                        val editor1=sharedPreferences1.edit()
                        editor1.putString("islogin", "true")
                        editor1.apply()
                        val intent = Intent(this@MainActivity, MainPage::class.java)
                        startActivityForResult(intent, Activity.RESULT_OK)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                    else{
                        Toast.makeText(application,"登录失败",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}