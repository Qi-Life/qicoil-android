package com.Meditation.Sounds.frequencies.feature.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.feature.chatbot.ChatApi.Companion
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

class ChatBotViewModel() : ViewModel() {
    private var client: OkHttpClient = OkHttpClient()
    private var _typingMessage = MutableLiveData<MessageChatBot>()
    val typingMessage: LiveData<MessageChatBot> get() = _typingMessage

    private var _bodyMessage = MutableLiveData<String>()
    val bodyMessage: LiveData<String> get() = _bodyMessage

    fun createThreadChatBot() {
        if (SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID).isNullOrEmpty()) {
            val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), "")
            val request: Request = Request.Builder().url(ChatApi.chatUrl).header("Authorization", "Bearer " + Companion.chatApiKey).header("OpenAI-Beta", "assistants=v2").post(requestBody).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            if (response.body != null) {
                                val jsonObject = JSONObject(response.body!!.string())
                                SharedPreferenceHelper.getInstance().set(Constants.PREF_CHATBOT_THREAD_ID, jsonObject.getString("id"))
                            }
                        } catch (e: JSONException) {
                            throw RuntimeException(e)
                        }
                    }
                }
            })
        }
    }

    fun sendMessageChat(question: String) {
        _typingMessage.value = MessageChatBot("Typing", MessageChatBot.SEND_BY_BOT)
        val threadId = SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID)
        val jsonBody = JSONObject()
        try {
            jsonBody.put("thread_id", threadId)
            jsonBody.put("message", question)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody.toString())
        val request: Request = Request.Builder().url("https://combined-quantum.ingeniusstudios.com/public/api/openai/runThread").post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _bodyMessage.postValue("Failed!")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        if (response.body != null) {
                            val jsonObject = response.body?.string()?.let { JSONObject(it) }
                            _bodyMessage.postValue(jsonObject?.getString("message"))
                        }
                    } catch (e: JSONException) {
                        _bodyMessage.postValue("")
                        throw RuntimeException(e)
                    }
                } else {
                    _bodyMessage.postValue("Failed!")
                }
            }
        })
    }
}