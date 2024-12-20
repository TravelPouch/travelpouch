package com.github.se.travelpouch.model.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkConnectivityHelper(private val context: Context) {

  private val _isConnected: MutableStateFlow<Boolean?> = MutableStateFlow(null)
  val isConnected: StateFlow<Boolean?> = _isConnected.asStateFlow()

  fun registerNetworkCallback() {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d("NetworkConnectivityHelper", "Connection available")
        _isConnected.value = true
      }

      override fun onUnavailable() {
        super.onUnavailable()
        Log.d("NetworkConnectivityHelper", "Connection unavailable")
        _isConnected.value = false
      }
    })

    val activeNetwork = connectivityManager.activeNetwork
    val initialCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    if (initialCapabilities != null) {
      if (initialCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
        Log.d("NetworkConnectivityHelper", "Connected to the internet")
        _isConnected.value = true
      } else {
        Log.d("NetworkConnectivityHelper", "Not connected to the internet")
        _isConnected.value = false
      }
    } else {
      Log.d("NetworkConnectivityHelper", "No network capabilities")
      _isConnected.value = false
    }
  }
}
