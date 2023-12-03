package com.example.qo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.DropBoxManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SerchPerson : AppCompatActivity() {
    data class Contact(val name: String,val id: String,val ship:String)
    private var contacts = emptyList<Contact>()
    private var lastQuery: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serch_person)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                lastQuery = query
                val msg= "serch:$query"
                sendmessage(this@SerchPerson).sendmessage(msg)
                contacts = emptyList<Contact>() // 清空列表
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.adapter = ContactAdapter(contacts) // 更新适配器
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }
    var reciver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val name=intent.getStringExtra("name")
            val id=intent.getStringExtra("id")
            val ship=DatabaseHelper(this@SerchPerson).getship(id!!)
            Log.d("serch", "onReceive: $name $id $ship")
            val contact=Contact(name!!,id!!,ship!!)
            if (!contacts.any { it.id == contact.id }) {
                contacts=contacts+contact
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.adapter = ContactAdapter(contacts)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    class ContactAdapter(private val contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

        class ContactViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.serch_item, parent, false)
            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            val contact = contacts[position]
            val info = holder.view.findViewById<TextView>(R.id.info)
            val button=holder.view.findViewById<Button>(R.id.button)
            if (contact.ship=="好友"){
                button.text="已添加"
                button.isEnabled=false
            }
            button.setOnClickListener {
                //socket发送数据
                val job = GlobalScope.launch(Dispatchers.IO) {
                    val app = holder.itemView.context.applicationContext as socket
                    val socket = app.getSocketInstance()
                    val writer = socket?.getOutputStream()
                    val msg = "add:${MainActivity.id} ${contact.id}"
                    writer?.write(msg.toByteArray())
                    writer?.flush()
                }
            }
            info.text = contact.name+"("+contact.id+")"
        }
        override fun getItemCount() = contacts.size
    }

    override fun onResume() {
        super.onResume()
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo.SerchPerson")
        registerReceiver(reciver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(reciver)
    }
}