package com.example.qo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [contacts.newInstance] factory method to
 * create an instance of this fragment.
 */
class Contact : Fragment() {
    var Contacts = emptyList<Contacts>()
    class ContactAdapter(private val contacts: List<Contacts>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

        class ContactViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            val contact = contacts[position]
            val info = holder.view.findViewById<TextView>(R.id.info)
            info.text = contact.name+"("+contact.id+")"
            info.setOnClickListener(){
                val intent = Intent(holder.itemView.context, Chatting::class.java)
                intent.putExtra("id",contact.id)
                intent.putExtra("name",contact.name)
                holder.itemView.context.startActivity(intent)
            }
        }

        override fun getItemCount() = contacts.size
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var statu = "false"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    //接收广播
    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val intent = intent
            statu = intent.getStringExtra("status") ?: "true"
            if (statu == "true") {
                refreshcontact()
                statu = "false"
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)
        val nf=view.findViewById<TextView>(R.id.newFriend)
        nf.setOnClickListener {
            sendmessage(requireContext()).sendmessage("getcontacts:"+MainActivity.id)
            val intent = Intent(activity,NewFriend::class.java)
            startActivity(intent)
        }
        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.qo.Contacts")
        activity?.registerReceiver(receiver, intentFilter)
        return view
    }
    fun refreshcontact() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(activity) // Add this

        //从db库中读取数据
        val db=DatabaseHelper(requireContext())
        Contacts =db.readcontactsData("好友")
        db.close()

        // Set the adapter after fetching data from database
        recyclerView?.adapter = ContactAdapter(Contacts)
        recyclerView?.adapter?.notifyDataSetChanged()
    }
    override fun onResume() {
        super.onResume()
        refreshcontact()
    }
    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }
}