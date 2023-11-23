package com.example.qo

import android.os.Bundle
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
import org.json.JSONArray

class SerchPerson : AppCompatActivity() {
    data class Contact(val name: String,val id: String)
    private var contacts = emptyList<Contact>()
    private var lastQuery: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serch_person)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val searchView = findViewById<SearchView>(R.id.searchView)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != lastQuery) {
                        lastQuery = query
                        // socket发送数据
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            val app = application as socket
                            val socket = app.getSocketInstance()
                            val outputStream = socket?.getOutputStream()
                            outputStream!!.write("serch:".toByteArray()+query!!.toByteArray())
                            //接收数据
                            val inputStream = socket?.getInputStream()
                            val buffer = ByteArray(1024)
                            val len = inputStream?.read(buffer)
                            if (len == -1) {
                                return@launch
                            }
                            val str = String(buffer, 0, len!!)
                            //解析json数据
                            val jsonArray = JSONArray(str)
                            val newContacts = mutableListOf<Contact>()
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val type = jsonObject.getString("type")
                                val id = jsonObject.getInt("id")
                                val name = jsonObject.getString("name")
                                println("type: $type, id: $id, name: $name")
                                newContacts.add(Contact(name, id.toString()))
                            }
                            outputStream.flush() // 将 flush() 方法移动到这里
                            contacts = newContacts
                            runOnUiThread {
                                recyclerView.adapter = ContactAdapter(contacts)
                            }
                        }
                    }
                    return false
                }
            override fun onQueryTextChange(newText: String?): Boolean {
                // Update the search results when the search query changes
                val filteredContacts = contacts.filter { it.name.contains(newText ?: "", ignoreCase = true) }
                recyclerView.adapter = ContactAdapter(filteredContacts)
                return false
            }
        })
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
            button.setOnClickListener {
                //socket发送数据
                val job = GlobalScope.launch(Dispatchers.IO) {
                    val app = holder.view.context.applicationContext as socket
                    val socket = app.getSocketInstance()
                    val outputStream = socket?.getOutputStream()
                    outputStream!!.write(("add:"+MainActivity.id+" "+contact.id).toByteArray())
                    outputStream.flush()
                }
            }
            info.text = contact.name+"("+contact.id+")"
        }

        override fun getItemCount() = contacts.size
    }
}