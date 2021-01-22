package com.words.storageapp.util.utilities

import android.net.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*

class ConnectivityChecker(
    private val connectivityManager: ConnectivityManager
) : LifecycleObserver {

    private var monitoringConnectivity = false

    private val _connectedStatus = MutableLiveData<Boolean>()
    val connectedStatus: LiveData<Boolean>
        get() = _connectedStatus

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectedStatus.postValue(true)
            // we are connected, so we can stop listening
            connectivityManager.unregisterNetworkCallback(this)
            monitoringConnectivity = false
        }

        override fun onLost(network: Network) {
            _connectedStatus.postValue(false)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopMonitoringConnectivity() {
        if (monitoringConnectivity) {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
            monitoringConnectivity = false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startMonitoringConnectivity() {
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        val connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        _connectedStatus.postValue(connected)
        if (!connected) {
            // we don't have internet connection, so we listen to notifications in connection status
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                connectivityCallback
            )
            monitoringConnectivity = true
        }
    }
}