package com.github.se.travelpouch.helper

import android.util.Log
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/** A helper class to manage network calls and handle responses. */
class NetworkManager(private val client: OkHttpClient) {

    /**
     * Makes a network call and handles the response or failure.
     *
     * @param request The HTTP request to be executed.
     * @param onSuccess Callback that is called when the request is successful.
     * @param onFailure Callback that is called when the request fails.
     */
    fun executeRequest(
        request: Request,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        client
            .newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("NetworkManager", "Failed to execute request", e)
                        onFailure(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                val exception = Exception("Unexpected code $response")
                                Log.d("NetworkManager", "Unexpected code $response")
                                onFailure(exception)
                                return
                            }

                            val body = response.body?.string()
                            if (body != null) {
                                onSuccess(body)
                            } else {
                                val exception = Exception("Empty response body")
                                Log.d("NetworkManager", "Empty body")
                                onFailure(exception)
                            }
                        }
                    }
                })
    }
}