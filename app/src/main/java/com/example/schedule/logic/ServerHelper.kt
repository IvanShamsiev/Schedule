package com.example.schedule.logic

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

object ServerHelper {

    @JvmStatic
    fun call(url: String, callback: Callback) {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).enqueue(callback)
    }



}