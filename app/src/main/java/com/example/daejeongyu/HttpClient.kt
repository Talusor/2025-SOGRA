package com.example.daejeongyu

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

interface BoolCallBack {
    fun onCallback(boolean: Boolean)
}

class HttpClient private constructor() {
    companion object {
        private var instance: HttpClient = HttpClient()

        fun getInstance(): HttpClient {
            return instance
        }
    }

    val okHttp = OkHttpClient()
    var access_token: String = ""
    var refresh_token: String = ""

    fun login(id: String, pw: String, callback: BoolCallBack) {
        val jsonBody = JSONObject()
        jsonBody.put("loginId", id)
        jsonBody.put("password", pw)

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        val requestBody = jsonBody.toString().toRequestBody(JSON)

        val req = Request.Builder()
            .url(String.format("%s/auth/login", BuildConfig.API_URL))
            .post(requestBody)
            .build()

        okHttp.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onCallback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val resp = response.body.string()
                    val json = JSONObject(resp)

                    access_token = json.getString("accessToken")
                    refresh_token = json.getString("refreshToken")

                    callback.onCallback(true)
                } else
                    callback.onCallback(false)
            }
        })
    }

    fun get(url: String, callback: Callback) {
        val req = Request.Builder()
            .url(String.format("%s/%s", BuildConfig.API_URL, url))
            .header("Authorization", access_token)
            .build()

        okHttp.newCall(req).enqueue(callback)
    }
}