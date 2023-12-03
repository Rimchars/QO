package com.example.qo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GptHelper.newInstance] factory method to
 * create an instance of this fragment.
 */
class GptHelper :Fragment(){
    private lateinit var adapter: GptAdapter
    var flag=true
    class data{
        var role=""
        var content=""
    }
    private val msgs = mutableListOf<data>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_gpt_helper, container, false)
        val db=DatabaseHelper(requireContext())
        val dataList =  db.readGptHelperData()
        Log.i("data", dataList.toString())
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView) // Assuming you have a RecyclerView with id recyclerView
        val filter = IntentFilter("com.example.qo.MESSAGE")
        requireActivity().registerReceiver(receiver, filter)

        dataList.forEach { jsonObject ->
            val message = data()
            Log.i("data", jsonObject.toString())
            message.role = jsonObject.get("role").asString
            message.content = jsonObject.get("content").asString
            msgs.add(message)
        }

        adapter = GptAdapter(msgs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        return view
    }
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            println("完整的回复: $message")
            data().apply {
                role="assistant"
                content= message!!
            }.also { msgs.add(it) }
            val db=DatabaseHelper(context)
            db.insertGptHelperData("assistant",message!!)
            db.close()
            flag=true
            adapter.notifyDataSetChanged()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val send = view.findViewById<Button>(R.id.sendButton)
        if (flag==false) {}
        else{
        send.setOnClickListener {
            val message =view.findViewById<TextView>(R.id.editText)
            val messageText = message.text.toString()
            val helper=helper()
            helper.sendmessage(requireContext(),messageText)
            data().apply {
                role="user"
                content=messageText
            }.also { msgs.add(it) }
            val db=DatabaseHelper(requireContext())
            db.insertGptHelperData("user",messageText)
            Log.i("data", "inserted")
            flag=false
            adapter.notifyDataSetChanged()
        }
        }
    }

    class GptAdapter(private val messages: MutableList<data>) : RecyclerView.Adapter<GptAdapter.GptViewHolder>() {
        class GptViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GptViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
            return GptViewHolder(view)
        }
        override fun onBindViewHolder(holder: GptViewHolder, position: Int) {
            val message = messages[position]
            val info = holder.view.findViewById<TextView>(R.id.messageTextView)
            info.text = message.content
            val itemlinearLayout = holder.view.findViewById<LinearLayout>(R.id.msg)
            //如果是自己发的消息，就靠右显示
            val time=holder.view.findViewById<TextView>(R.id.timeTextView)
            if (message.role == "user") {
                itemlinearLayout.gravity = Gravity.END
            } else {
                itemlinearLayout.gravity = Gravity.START
            }
        }

        override fun getItemCount() = messages.size

    }

}
