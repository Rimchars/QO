package com.example.qo

class Messages {
    var id1: String? = null
    var messages: String? = null
    var id2: String? = null
    var mid: String? = null
    var time: String? = null
    constructor(mid:String?,id1: String?, messages: String?,id2:String?,time:String?) {
        this.mid = mid
        this.id1 = id1
        this.messages = messages
        this.id2 = id2
        this.time = time
    }
}