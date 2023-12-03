package com.example.qo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

class Chatting : AppCompatActivity() {
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Messages>()
    private lateinit var receiver: BroadcastReceiver
    lateinit var id1: String
    lateinit var id2: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)
        id1 = MainActivity.id
        id2 = intent.getStringExtra("id")!!
        val name = intent.getStringExtra("name")
        val title = findViewById<TextView>(R.id.title)
        title.text = name
        adapter = MessageAdapter(messages)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Add this line
        recyclerView.adapter = adapter
        //从db库中读取数据
        val db=DatabaseHelper(this)
        val cursor=db.readData()
        if(cursor.count>0){
            while(cursor.moveToNext()){
                val message = Messages(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4))
                println("mid:${cursor.getString(0)} id1:${cursor.getString(1)} message:${cursor.getString(2)} id2:${cursor.getString(3)} time:${cursor.getString(4)}")
                //判断是不是当前聊天的人
                if((message.id1==id1&&message.id2==id2)||(message.id1==id2&&message.id2==id1))
                    messages.add(message)
            }
            adapter.notifyDataSetChanged()
            // Scroll to the last message
            recyclerView.scrollToPosition(messages.size - 1)
        }
        cursor.close()
        db.close()
        receiveMessageFromSocket()
    }
    fun receiveMessageFromSocket() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val mid = intent.getStringExtra("mid")
                val id1 = intent.getStringExtra("id1")
                val message = intent.getStringExtra("message")
                val id2 = intent.getStringExtra("id2")
                val time = intent.getStringExtra("time")
                messages.add(Messages(mid!!,id1!!,message!!,id2!!,time!!))
                adapter.notifyDataSetChanged()
                // Scroll to the last message
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }
    fun send(view: View) {
        val messageText = findViewById<EditText>(R.id.editText).text
        val datetime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
        val db=DatabaseHelper(this)
        if (messageText.isEmpty()) {
            return
        }
        val message = Messages("send",id1,messageText.toString(),id2,datetime)
        messages.add(message)
        adapter.notifyDataSetChanged()
        // 获取datetime格式的时间
        db.insertmessageData("send",id1,messageText.toString(),id2,datetime)
        // Send message through socket
        sendmessage(this).sendmessage("send:$id1 $messageText $id2")
        // Scroll to the last message
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.scrollToPosition(messages.size - 1)
        // Clear the input
        findViewById<EditText>(R.id.editText).text.clear()
        db.close()
    }

    class MessageAdapter(private val messages: MutableList<Messages>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
        fun date(date:String):String{
            //如果是今天则取时分,如果是昨天则取昨天,如果是昨天之前则取月日
            val today=java.text.SimpleDateFormat("yyyy-MM-dd").format(Date())
            val yesterday=java.text.SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()-24*60*60*1000))
            val date1=date.substring(0,10)
            val date2=date.substring(11,16)
            if(date1==today){
                return date2
            }
            else if(date1==yesterday){
                return "昨天"
            }
            else{
                return date1.substring(5,10)
            }
        }

        class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            val messageTextView = holder.view.findViewById<TextView>(R.id.messageTextView)
            messageTextView.text = message.messages
            val itemlinearLayout = holder.view.findViewById<LinearLayout>(R.id.msg)
            val time=holder.view.findViewById<TextView>(R.id.timeTextView)
            time.setText(message.time?.let { date(it) })
            if (message.mid == "send") {
                itemlinearLayout.gravity = Gravity.END
            } else {
                itemlinearLayout.gravity = Gravity.START
            }

        }
        override fun getItemCount() = messages.size
    }

    override fun onResume() {
        super.onResume()
        // 注册广播接收器
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo")
        registerReceiver(receiver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        // 注销广播接收器
        unregisterReceiver(receiver)
    }
}

