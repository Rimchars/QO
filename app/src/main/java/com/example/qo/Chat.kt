package com.example.qo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class Chat: Fragment() {
    companion object {
        var drawerOpen = false
    }
    class Chat(val id: String, val messages: MutableList<String>, val name: String)
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
            holder.nameView.text = chat.name+"("+chat.id+")"
            holder.messageView.text = chat.messages.last()
            holder.itemView.setOnClickListener {
                // 创建一个Intent来启动新的Activity
                val intent = Intent(holder.itemView.context, Chatting::class.java)
                // 将聊天会话的名称作为额外数据传递给新的Activity
                intent.putExtra("id", chat.id)
                intent.putExtra("name",chat.name)
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
        val view = inflater.inflate(R.layout.fragment_chat, container, false) // replace "your_layout" with your actual layout resource
        chatList = mutableListOf()
        chatAdapter = ChatAdapter(chatList)
        rcv(view)
        refreshChatList()
        receiveNewMessage()
        return view
    }
    fun rcv(view: View){
        recyclerView = view.findViewById(R.id.recyclerView) // find recyclerView in the inflated view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.adapter = chatAdapter
        val drawer = view.findViewById<View>(R.id.drawer)
        var initialTouchY = 0f
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val touchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val diffY = event.rawY - initialTouchY
                    //设置fragment的大小随着手指的滑动而变化
                    drawer.translationY = Math.min(0f, Math.max(-screenHeight.toFloat(), drawer.translationY + diffY))
                    initialTouchY = event.rawY
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val itemCount = recyclerView.adapter?.itemCount ?: 0
                    if (diffY < 0 && drawerOpen) {
                        // Close the drawer immediately when swiping up
                        drawerOpen = false
                        val animator = ObjectAnimator.ofFloat(drawer, "translationY", -screenHeight.toFloat())
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        animator.duration = 500 // Increase duration to slow down the animation
                        animator.start()
                        true
                    } else if (diffY > 0 && !drawerOpen && firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1)||itemCount==0) {
                        drawer.translationY = Math.min(0f, Math.max(-screenHeight.toFloat(), drawer.translationY + diffY))
                        initialTouchY = event.rawY
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (drawer.translationY < -screenHeight / 2) {
                        // Close the drawer
                        drawerOpen = false
                        val animator = ObjectAnimator.ofFloat(drawer, "translationY", -screenHeight.toFloat())
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        animator.duration = 500 // Increase duration to slow down the animation
                        animator.start()
                    } else {
                        drawerOpen = true
                        refreshChatList()
                        receiveNewMessage()
                        val animator = ObjectAnimator.ofFloat(drawer, "translationY", 0f)
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        animator.duration = 500 // Increase duration to slow down the animation
                        animator.start()

                        // Add your fragment here
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, GptHelper())
                        transaction.commit()
                    }
                    true
                }
                else -> false
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerOpen) {
                    // Close the drawer
                    drawerOpen = false
                    val animator = ObjectAnimator.ofFloat(drawer, "translationY", -screenHeight.toFloat())
                    animator.interpolator = AccelerateDecelerateInterpolator()
                    animator.duration = 500 // Increase duration to slow down the animation
                    animator.start()

                    // Return to the main page
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, Chat())
                    transaction.commit()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
        recyclerView.setOnTouchListener(touchListener)
        drawer.translationY = -screenHeight.toFloat()
    }
    fun refreshChatList() {
        val db = DatabaseHelper(requireContext())
        val cursor = db.getLatestChats(MainActivity.id)
        var name = ""
        var str = ""
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(1) != MainActivity.id) {
                        str = cursor.getString(1)
                        name = db.getname(str)
                    } else {
                        str = cursor.getString(3)
                        name = db.getname(str)
                    }
                    if (chatList.find { it.id == str }==null) {
                        chatList.add(Chat(str, mutableListOf(cursor.getString(4)), name))
                    } else {
                        chatList.find { it.id == str }?.messages?.add(cursor.getString(4))
                    }

                } while (cursor.moveToNext())
            }
        }
        if (cursor != null) {
            cursor.close()
        }
        chatAdapter.notifyDataSetChanged()
    }
    fun receiveNewMessage() {
        receiver = object : BroadcastReceiver() {
            //获取广播数据的id和message
            override fun onReceive(context: Context, intent: Intent) {
                val db = DatabaseHelper(requireContext())
                val id1 = intent.getStringExtra("id1")
                val id2 = intent.getStringExtra("id2")
                val message = intent.getStringExtra("message")
                val id = if (id1 == MainActivity.id) id2 else id1
                val name= id?.let { db.getname(it) }
                db.close()
                if (id != null && message != null && name != null) {
                    // 找到对应的 Chat 对象
                    val chat = chatList.find { it.id == id }
                    if (chat != null) {
                        // 添加新的消息到 messages
                        chat.messages.add(message)
                    } else {
                        if (id != MainActivity.id) {
                            // 如果找不到对应的 Chat 对象，就创建一个新的 Chat 对象
                            chatList.add(Chat(id, mutableListOf(message), name))
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                }
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendmessage(requireContext()).sendmessage("getmessage:"+MainActivity.id)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo")
        requireActivity().registerReceiver(receiver, intentFilter)
    }

}

