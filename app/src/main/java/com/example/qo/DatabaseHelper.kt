package com.example.qo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "messages.db"
        private var messages_table= "t" + MainActivity.id + "messages_table"
        private var contacts_table = "t" + MainActivity.id + "contacts_table"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE_QUERY = "CREATE TABLE $messages_table (mid String,id1 String,messages TEXT,id2 String,time DATETIME)"
        val CREATE_TABLE_QUERY2 = "CREATE TABLE $contacts_table (name String,id String,ship String)"
        db.execSQL(CREATE_TABLE_QUERY2)
        db.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $messages_table")
        db.execSQL("DROP TABLE IF EXISTS $contacts_table")
        onCreate(db)
    }
    fun insertcontactsData(name: String, id: String,ship:String){
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("id", id)
        contentValues.put("ship", ship)
        db.insert(contacts_table, null, contentValues)
    }
    fun insertmessageData(mid:String,id1: String,messages: String,id2:String,time:String){
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("mid", mid)
        contentValues.put("id1", id1)
        contentValues.put("messages", messages)
        contentValues.put("id2", id2)
        contentValues.put("time", time)
        db.insert(messages_table, null, contentValues)
    }
    fun readcontactsData(ship:String): MutableList<Contacts> {
        val list: MutableList<Contacts> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $contacts_table where ship = ?"
        val result = db.rawQuery(query, arrayOf(ship))
        if (result.moveToFirst()) {
            do {
                val contacts = Contacts(result.getString(0), result.getString(1),result.getString(2))
                list.add(contacts)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }
    fun deleteData(id: Int) {
        val db = this.writableDatabase
        db.delete(messages_table, "id = ?", arrayOf(id.toString()))
    }
    fun readmessageData(id1:String,id2:String): MutableList<Messages> {
        val list: MutableList<Messages> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $messages_table where id1 = ? and id2 = ? or id1 = ? and id2 = ?"
        val result = db.rawQuery(query, arrayOf(id1,id2,id2,id1))
        if (result.moveToFirst()) {
            do {
                val messages = Messages(result.getString(0), result.getString(1),result.getString(2),result.getString(3),result.getString(4))
                list.add(messages)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }
    fun readData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM messages_table", null)
    }
    fun getLatestChats(currentUserId: String): Cursor {
        val db = this.readableDatabase
        val query = """
        SELECT * FROM $messages_table 
        WHERE (id1,time) IN (
            SELECT id1, MAX(time) 
            FROM $messages_table 
            WHERE id1 = ? OR id2 = ?
            GROUP BY id1
        ) 
        ORDER BY time DESC
    """
        return db.rawQuery(query, arrayOf(currentUserId, currentUserId))
    }
    fun deleteContact(id: String,ship:String) {
        val db = this.writableDatabase
        db.delete(contacts_table, "id1 = ? or id2= ? and ship = ?", arrayOf(id,id,ship))
    }
    fun updataContact(id: String,sship:String,eship:String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("ship",eship )
        db.update(contacts_table, contentValues, "id = ? and ship = ?", arrayOf(id,sship))
    }
}