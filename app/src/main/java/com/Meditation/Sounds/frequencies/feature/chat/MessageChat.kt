package com.Meditation.Sounds.frequencies.feature.chat

class MessageChat(var message: String, var sentBy: String) {
    companion object {
        var SEND_BY_ME: String = "me"
        var SEND_BY_BOT: String = "bot"
    }
}

