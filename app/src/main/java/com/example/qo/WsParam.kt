package com.example.qo

import android.content.Context
import android.content.Intent
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.withContext

class WsParam(Context: Context) {
    val host = "spark-api.xf-yun.com"
    val path = "/v3.1/chat"
    val Spark_url = "wss://spark-api.xf-yun.com/v3.1/chat"
    val APIKey = "4c94b23fa60e8c8c5a8a51861b680c01"
    val APISecret = "NmJjYmViYzg0ZThhY2NiNjE2YzM4MmUz"
    val APPID = "8f22b95b"
    val fullResponse = StringBuilder()

    fun create_url(): String {
        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.format(Date())

        val signatureOrigin = "host: $host\ndate: $date\nGET $path HTTP/1.1"
        val hmacSha256 = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(APISecret.toByteArray(), "HmacSHA256"))
        }
        val signatureSha = hmacSha256.doFinal(signatureOrigin.toByteArray())
        val signatureShaBase64 = Base64.getEncoder().encodeToString(signatureSha)

        val authorizationOrigin = """api_key="$APIKey", algorithm="hmac-sha256", headers="host date request-line", signature="$signatureShaBase64""""
        val authorization = Base64.getEncoder().encodeToString(authorizationOrigin.toByteArray())

        val v = mapOf(
            "authorization" to authorization,
            "date" to date,
            "host" to host
        )
        val url = "$Spark_url?${v.entries.joinToString("&") { "${it.key}=${it.value}" }}"
        println(url)
        return url
    }
    fun getparm(uid: String, messages: MutableList<JsonObject>): String {
        val data = JsonObject().apply {
            add("header", JsonObject().apply {
                addProperty("app_id", APPID)
                addProperty("uid", uid)
            })
            add("parameter", JsonObject().apply {
                add("chat", JsonObject().apply {
                    addProperty("domain", "generalv3")
                    addProperty("temperature", 0.5)
                    addProperty("max_tokens", 1024)
                })
            })
            add("payload", JsonObject().apply {
                add("message", JsonObject().apply {
                    add("text", JsonArray().apply {
                        messages.forEach { message ->
                            val role = message.get("role").asString
                            val content = message.get("content").asString
                            add(JsonObject().apply {
                                addProperty("role", role)
                                addProperty("content", content)
                            })
                        }
                    })
                })
            })
        }
        return Gson().toJson(data)
    }
    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {

        }
        override fun onMessage(webSocket: WebSocket, text: String) {
            val data = Gson().fromJson(text, JsonObject::class.java)
            val choices = data.getAsJsonObject("payload").getAsJsonObject("choices")
            val status = choices.get("status").asInt
            val content = choices.getAsJsonArray("text")[0].asJsonObject.get("content").asString

            // 添加新的content到fullResponse
            fullResponse.append(content)

            if (status == 2) {
                // 在这里，fullResponse.toString()将包含完整的回复
                val rev = fullResponse.toString()
                val intent = Intent("com.example.qo.MESSAGE")
                // 将你的消息作为额外数据放入Intent
                intent.putExtra("message", rev)
                // 发送广播
                Context.sendBroadcast(intent)
                // 清空StringBuilder以便于下一次使用
                fullResponse.clear()

                webSocket.close(1000, null)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("Error : ${t.message}")
        }
    }
    fun sendmessage(webSocket: WebSocket, message: String) {
        webSocket.send(message)
    }
}