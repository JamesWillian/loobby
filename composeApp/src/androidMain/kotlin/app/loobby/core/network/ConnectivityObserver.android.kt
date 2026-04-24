package app.loobby.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

/**
 * Implementação Android do [ConnectivityObserver].
 *
 * Usa [ConnectivityManager.registerNetworkCallback] para observar mudanças
 * em tempo real. Só consideramos "online" uma rede que:
 *   - Está disponível (onAvailable)
 *   - Possui capacidade de INTERNET
 *   - Foi validada pelo SO (NET_CAPABILITY_VALIDATED) — ou seja, o SO
 *     efetivamente alcançou algum servidor (detecta Wi-Fi de captive portal
 *     sem internet real).
 *
 * O estado é exposto como um [kotlinx.coroutines.flow.StateFlow] interno para
 * garantir que o snapshot [isOnlineNow] seja sempre o último valor observado
 * e que todos os collectors recebam o valor corrente imediatamente.
 *
 * Requisitos:
 *   - Permissão `android.permission.ACCESS_NETWORK_STATE` no manifest.
 */
actual class ConnectivityObserver(context: Context) {

    private val appContext: Context = context.applicationContext
    private val cm: ConnectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun currentStatus(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // StateFlow "quente" mantido pela vida toda do processo.
    // Novos collectors recebem imediatamente o último valor conhecido.
    private val state = callbackFlow<Boolean> {
        // Emite o estado inicial
        trySend(currentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(currentStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(currentStatus())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        awaitClose {
            cm.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = currentStatus()
        )

    actual val isOnline: Flow<Boolean> = state

    actual fun isOnlineNow(): Boolean = state.value
}
