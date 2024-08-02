package com.Meditation.Sounds.frequencies.feature.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
            val request: Request = Request.Builder().url("https://api.openai.com/v1/threads").header("Authorization", "Bearer " + "sk-proj-yVqZ9CNC2bkMpkKyOCKfT3BlbkFJNeIW9gfDPWEuDsxbRAX9").header("OpenAI-Beta", "assistants=v2").post(requestBody).build()

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


    fun addMessageToThread(question: String?) {
        _typingMessage.value = MessageChatBot("Typing", MessageChatBot.SEND_BY_BOT)
        val jsonBody = JSONObject()
        try {
            jsonBody.put("role", "user")
            jsonBody.put("content", question)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val threadId = SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID)
        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody.toString())
        val request: Request = Request.Builder().url("https://api.openai.com/v1/threads/$threadId/messages").header("Authorization", "Bearer " + "sk-proj-yVqZ9CNC2bkMpkKyOCKfT3BlbkFJNeIW9gfDPWEuDsxbRAX9").header("OpenAI-Beta", "assistants=v2").post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runMessageThread()
                }
            }
        })
    }


    fun runMessageThread() {
        val jsonBody = JSONObject()
        try {
            jsonBody.put("assistant_id", "asst_WCn2gkamQDyisW4FzJEsFbqI")
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val threadId = SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID)
        val requestBody: RequestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonBody.toString())
        val request: Request = Request.Builder().url("https://api.openai.com/v1/threads/$threadId/runs").header("Authorization", "Bearer " + "sk-proj-yVqZ9CNC2bkMpkKyOCKfT3BlbkFJNeIW9gfDPWEuDsxbRAX9").header("OpenAI-Beta", "assistants=v2").post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        if (response.body != null) {
                            val jsonObject = JSONObject(response.body!!.string())
                            retrieveMessageThread(jsonObject.getString("id"))
                        }
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                }
            }
        })
    }

    fun retrieveMessageThread(runId: String) {
        val threadId = SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID)
        val request: Request = Request.Builder().url("https://api.openai.com/v1/threads/$threadId/runs/$runId").header("Authorization", "Bearer " + "sk-proj-yVqZ9CNC2bkMpkKyOCKfT3BlbkFJNeIW9gfDPWEuDsxbRAX9").header("OpenAI-Beta", "assistants=v2").get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        if (response.body != null) {
                           val jsonObject = JSONObject(response.body!!.string())
                           if (jsonObject.getString("status") == "completed") {
                               getMessageChatBot()
                           } else {
                               retrieveMessageThread(runId)
                           }
                        }
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                }
            }
        })
    }

    fun getMessageChatBot() {
        val threadId = SharedPreferenceHelper.getInstance().get(Constants.PREF_CHATBOT_THREAD_ID)
        val request: Request = Request.Builder().url("https://api.openai.com/v1/threads/$threadId/messages").header("Authorization", "Bearer " + "sk-proj-yVqZ9CNC2bkMpkKyOCKfT3BlbkFJNeIW9gfDPWEuDsxbRAX9").header("OpenAI-Beta", "assistants=v2").get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _bodyMessage.postValue("Failed to load response due to" + e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        if (response.body != null) {
                            val jsonObject = JSONObject(response.body!!.string())
                            val jsonData = jsonObject.getJSONArray("data")
                            if (jsonData.length() > 0 && jsonData.getJSONObject(0).getJSONArray("content").length() > 0) {
                                val result = jsonData.getJSONObject(0).getJSONArray("content").getJSONObject(0).getJSONObject("text").getString("value")
                                _bodyMessage.postValue(result.trim { it <= ' ' })
                            } else {
                                _bodyMessage.postValue("")
                            }
                        }
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                } else {
                    if (response.body != null) {
                        _bodyMessage.postValue("Failed to load response due to" + response.body.toString())
                    }
                }
            }
        })
    }
}