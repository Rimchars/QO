package com.example.qo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "messages.db"
        var messages_table=""
        var contacts_table=""
        var GptHelper_table=""
    }

    override fun onCreate(db: SQLiteDatabase) {
    }
    fun creattable()
    {
        val db = this.writableDatabase
        val CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS $messages_table (mid String,id1 String,messages TEXT,id2 String,time DATETIME)"
        val CREATE_TABLE_QUERY2 = "CREATE TABLE IF NOT EXISTS $contacts_table (name String,id String,ship String)"
        val CREATE_TABLE_QUERY3 = "CREATE TABLE IF NOT EXISTS $GptHelper_table (role String,messages TEXT,time DATETIME)"
        db.execSQL(CREATE_TABLE_QUERY3)
        db.execSQL(CREATE_TABLE_QUERY2)
        db.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $messages_table")
        db.execSQL("DROP TABLE IF EXISTS $contacts_table")
        db.execSQL("DROP TABLE IF EXISTS $GptHelper_table")
        onCreate(db)
    }
    fun insertcontactsData(name: String, id: String,ship:String){
        val db = this.writableDatabase
        val cursor = db.query(contacts_table, null, "id = ?", arrayOf(id), null, null, null)
        if (cursor.moveToFirst()) {
            // The data already exists, update or ignore
            val contentValues = ContentValues()
            contentValues.put("name", name)
            contentValues.put("ship", ship)
            db.update(contacts_table, contentValues, "id = ?", arrayOf(id))
        } else {
            // The data does not exist, insert
            val contentValues = ContentValues()
            contentValues.put("name", name)
            contentValues.put("id", id)
            contentValues.put("ship", ship)
            db.insert(contacts_table, null, contentValues)
        }
        cursor.close()
    }

    fun insertmessageData(mid:String,id1: String,messages: String,id2:String,time:String){
        val db = this.writableDatabase
        val cursor = db.query(messages_table, null, "mid = ? and id1 = ? and messages = ? and id2 = ? and time = ?", arrayOf(mid,id1,messages,id2,time), null, null, null)
        if (cursor.moveToFirst()) {
            // The data already exists, update or ignore
            val contentValues = ContentValues()
            contentValues.put("id1", id1)
            contentValues.put("messages", messages)
            contentValues.put("id2", id2)
            contentValues.put("time", time)
            db.update(messages_table, contentValues, "mid = ?", arrayOf(mid))
        } else {
            // The data does not exist, insert
            val contentValues = ContentValues()
            contentValues.put("mid", mid)
            contentValues.put("id1", id1)
            contentValues.put("messages", messages)
            contentValues.put("id2", id2)
            contentValues.put("time", time)
            db.insert(messages_table, null, contentValues)
        }
        cursor.close()
    }
    fun insertGptHelperData(role:String, messages: String){
        val db = this.writableDatabase
        val datetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val contentValues = ContentValues()
        contentValues.put("role", role)
        contentValues.put("messages", messages)
        contentValues.put("time", datetime)

        // Check if data already exists in the database
        val query = "SELECT * FROM $GptHelper_table WHERE role = ? AND messages = ? AND time = ?"
        val cursor = db.rawQuery(query, arrayOf(role, messages, datetime))

        if (!cursor.moveToFirst()) {
            // If data does not exist in the database, insert it
            db.insert(GptHelper_table, null, contentValues)
        }

        cursor.close()
        db.close()
    }
    fun readGptHelperData(): MutableList<JsonObject> {
        val list: MutableList<JsonObject> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $GptHelper_table ORDER BY time DESC LIMIT 30" // Assuming 'time' is your timestamp column
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val data=JsonObject()
                data.addProperty("role",result.getString(0))
                data.addProperty("content",result.getString(1))
                Log.i("data", data.toString())
                list.add(data)
            } while (result.moveToNext())
        }
        Log.i("data", list.toString())
        result.close()
        db.close()
        return list
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
    fun getContactData(id: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $contacts_table WHERE id = ?", arrayOf(id))
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
        return db.rawQuery("SELECT * FROM $messages_table", null)
    }
    fun getLatestChats(currentUserId: String): Cursor? {
        val db = this.readableDatabase

        // Check if the table is empty
        val countQuery = "SELECT count(*) FROM $messages_table"
        var cursor = db.rawQuery(countQuery, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        // If the table is not empty, perform the query
        if (count > 0) {
            val query = """
            SELECT * FROM $messages_table
            WHERE (id1,time) IN (
                SELECT id1, MAX(time) 
                FROM $messages_table 
                WHERE id1 = ? OR id2 = ?
                GROUP BY id1
            ) ORDER BY time DESC
        """
            cursor= db.rawQuery(query, arrayOf(currentUserId, currentUserId))
            while(cursor.moveToNext()){
                Log.i("data",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+" "+cursor.getString(4))
            }
            return db.rawQuery(query, arrayOf(currentUserId, currentUserId))
        } else {
            return null
        }
    }
    fun getname(id: String): String {
        val db = this.readableDatabase
        val query = "Select name from $contacts_table where id = ?"
        val result = db.rawQuery(query, arrayOf(id))
        if (result.moveToFirst()) {
            return result.getString(0)
        }
        result.close()
        db.close()
        return ""
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
    fun getship(id: String): String {
        val db = this.readableDatabase
        val query = "Select ship from $contacts_table where id = ?"
        val result = db.rawQuery(query, arrayOf(id))
        if (result.moveToFirst()) {
            return result.getString(0)
        }
        result.close()
        db.close()
        return ""
    }
    //关闭数据库
    final fun closeDB() {
        val db = this.writableDatabase
        db.close()
    }
}