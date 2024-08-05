package com.Meditation.Sounds.frequencies.feature.chatbot

class MessageChatBot(var message: String, var sentBy: String) {
    var statusTyping: Boolean = false

    companion object {
        var SEND_BY_ME: String = "me"
        var SEND_BY_BOT: String = "bot"
    }
}

