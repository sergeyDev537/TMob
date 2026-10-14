package com.nbrb.tmob.data.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = NetworkCallback()

    private val _currentNetwork = MutableStateFlow(provideDefaultCurrentNetwork())

    val isNetworkConnectedFlow: StateFlow<Boolean> =
        _currentNetwork
            .map {
                it.isConnected()
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = _currentNetwork.value.isConnected()
            )

    val isNetworkConnected: Boolean
        get() = getCurrentNetworkConnected()

    fun startListenNetworkState() {
        if (_currentNetwork.value.isListening) { return }

        _currentNetwork.update {
            provideDefaultCurrentNetwork().copy(isListening = true)
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun stopListenNetworkState() {
        if (!_currentNetwork.value.isListening) {
            return
        }

        _currentNetwork.update {
            it.copy(isListening = false)
        }

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun getCurrentNetworkConnected(): Boolean {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private inner class NetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _currentNetwork.update {
                it.copy(isAvailable = true)
            }
        }

        override fun onLost(network: Network) {
            _currentNetwork.update {
                it.copy(
                    isAvailable = false,
                    networkCapabilities = null
                )
            }
        }

        override fun onUnavailable() {
            _currentNetwork.update {
                it.copy(
                    isAvailable = false,
                    networkCapabilities = null
                )
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            _currentNetwork.update {
                it.copy(networkCapabilities = networkCapabilities)
            }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            _currentNetwork.update {
                it.copy(isBlocked = blocked)
            }
        }
    }

    private fun provideDefaultCurrentNetwork(): CurrentNetwork {
        return CurrentNetwork(
            isListening = false,
            networkCapabilities = null,
            isAvailable = false,
            isBlocked = false
        )
    }

    private data class CurrentNetwork(
        val isListening: Boolean,
        val networkCapabilities: NetworkCapabilities?,
        val isAvailable: Boolean,
        val isBlocked: Boolean,
    )

    private fun CurrentNetwork.isConnected(): Boolean {
        return isListening &&
                isAvailable &&
                !isBlocked &&
                networkCapabilities.isNetworkCapabilitiesValid()
    }

    private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean = when {
        this == null -> false
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true

        else -> false
    }
}