package com.example.qo

import android.app.Application
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.net.NetworkInterface
import java.net.Socket
import java.util.*

class socket : Application() {
    var socket: Socket? = null
    private lateinit var WifiManager: WifiManager
    var ip:  String = "暂未实现"

    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch(Dispatchers.IO) {
            socket = Socket("47.120.50.73", 8080)
            ip = getip()
        }
    }
    fun getSocketInstance(): Socket? {
        return socket
    }
    fun getip(useIPv4: Boolean = true): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        // Check if IPv4 or IPv6 to return the correct format
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            // Handle exception
        }
        return ""
    }
}