package com.example.qo

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Runnable

class NewFriend : AppCompatActivity() {
    class ContactAdapter(private val contacts: List<Contacts>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
        private val mutableContacts = contacts.toMutableList()
        class ContactViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.nf_item, parent, false)
            return ContactViewHolder(view)
        }
        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            val contact = mutableContacts[position]
            val info = holder.view.findViewById<TextView>(R.id.info)
            val button=holder.view.findViewById<Button>(R.id.button)
            button.setOnClickListener {
                val str =("accept:"+MainActivity.id+" "+contact.id)
                sendmessage(holder.itemView.context).sendmessage(str)
                val db=DatabaseHelper(holder.view.context)
                db.updataContact(contact.id!!,"等待","好友")
                db.close()
                mutableContacts.removeAt(position)
                notifyItemRemoved(position)
                notifyDataSetChanged()
            }
            info.text = contact.name+"("+contact.id+")"
        }
        override fun getItemCount() = mutableContacts.size
    }

    var status="false"
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            val status = intent.getStringExtra("status") ?: "true"
            println("status:$status")
            if(status=="true"){
                RefreshRequest()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_friend)
        RefreshRequest()
    }
    fun RefreshRequest() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView) ?: return
        val db=DatabaseHelper(this)
        val contact=db.readcontactsData("等待")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ContactAdapter(contact)
        recyclerView.adapter?.notifyDataSetChanged()
    }
    override fun onResume() {
        super.onResume()
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo.NewFriend")
        registerReceiver(receiver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}