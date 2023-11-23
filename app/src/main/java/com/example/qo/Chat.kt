package com.example.qo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Chat: Fragment() {
    data class Chat(val id: String, var lastMessage: String)
    class ChatAdapter(private val chatList: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
        class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameView: TextView = itemView.findViewById(R.id.name)
            val messageView: TextView = itemView.findViewById(R.id.message)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chatList[position]
            holder.nameView.text = chat.id
            holder.messageView.text = chat.lastMessage
            Log.d("ChatAdapter", "Binding view holder with chat: $chat")
            holder.itemView.setOnClickListener {
                // 创建一个Intent来启动新的Activity
               val intent = Intent(holder.itemView.context, Chatting::class.java)
                // 将聊天会话的名称作为额外数据传递给新的Activity
               intent.putExtra("id", chat.id)
                // 启动新的Activity
                holder.itemView.context.startActivity(intent)
            }
        }
        override fun getItemCount() = chatList.size
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: MutableList<Chat>
    private val handler = Handler(Looper.getMainLooper())
    lateinit var receiver: BroadcastReceiver
    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        chatList = mutableListOf()
        chatAdapter = ChatAdapter(chatList)
        recyclerView.adapter = chatAdapter
        refreshChatList()
        receiveNewMessage()
        return view
    }
    fun refreshChatList() {
        val db = DatabaseHelper(requireContext())
        val cursor =db.getLatestChats(MainActivity.id)
        if (cursor.moveToFirst()) {
            do {
                val chat = Chat(cursor.getString(1), cursor.getString(2))
                //如果读取的id是自己的id，就不加入chatlist
                if (chat.id == MainActivity.id) {
                    continue
                }
                chatList.add(chat)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
    fun searchChat(keyword: String) {
        // 根据关键字搜索会话
        // 更新chatList并通知adapter数据已改变
    }
    fun receiveNewMessage() {
        receiver = object : BroadcastReceiver() {
            //获取广播数据的id和message
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getStringExtra("id1")
                val message = intent.getStringExtra("message")
                // 找到对应的 Chat 对象
                val chat = chatList.find { it.id == id }
                if (chat != null) {
                    // 更新 lastMessage
                    chat.lastMessage = message!!
                } else {
                    // 如果没有找到对应的 Chat 对象，就创建一个新的
                    chatList.add(Chat(id!!, message!!))
                }
                chatAdapter.notifyDataSetChanged()
            }
        }
    }
    fun deleteChat(position: Int) {
        // 删除指定位置的会话
        chatList.removeAt(position)
        chatAdapter.notifyItemRemoved(position)
    }
    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(receiver)
    }
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo")
        requireActivity().registerReceiver(receiver, intentFilter)
    }

}

