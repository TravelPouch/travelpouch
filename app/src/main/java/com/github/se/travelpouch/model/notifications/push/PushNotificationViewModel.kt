package com.github.se.travelpouch.model.notifications.push

import androidx.browser.trusted.Token
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.travelpouch.model.notifications.Notification
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PushNotificationViewModel : ViewModel() {

    private val api : FcmApi = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/v1/projects/travelpouch-1/messages:send")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FcmApi::class.java)

    fun sendMessage(isBroadcast: Boolean, token: String, notification: Notification) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = if(isBroadcast) null else token,
                notification = notification
            )

            try {
                if(isBroadcast) {
                    api.broadcast(messageDto)
                } else {
                    api.send(messageDto)
                }
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}