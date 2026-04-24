package app.loobby.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementação Android do [ConnectivityObserver].
 *
 * Estratégia:
 *  - Usa [ConnectivityManager.registerDefaultNetworkCallback] (API 24+) para
 *    receber callbacks apenas da rede **default** do sistema. Isso evita a
 *    confusão clássica quando há Wi-Fi + Celular ativos simultaneamente.
 *  - Lê o estado **diretamente dos argumentos do callback** (`network` +
 *    `networkCapabilities`), sem re-consultar `cm.activeNetwork`. Isso elimina
 *    a race condition em que, após `onLost`, o `activeNetwork` ainda reportaria
 *    a rede que acabou de cair — sintoma que fazia o observer "travar" depois
 *    do primeiro toggle.
 *  - Mantém um [MutableStateFlow] interno. O callback registrado uma única vez
 *    no `init` vive pelo processo inteiro (o observer é singleton no Koin),
 *    então não há ciclo de subscribe/unsubscribe para quebrar.
 *
 * "Online" exige INTERNET + VALIDATED — o SO precisa ter validado que a rede
 * alcança a internet de fato (filtra captive portal sem tráfego).
 *
 * Requisitos:
 *   - Permissão `android.permission.ACCESS_NETWORK_STATE` no manifest.
 */
actual class ConnectivityObserver(context: Context) {

    private val cm: ConnectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

    private val _state = MutableStateFlow(initialStatus())

    private fun initialStatus(): Boolean {
        // Snapshot síncrono usado apenas para o valor inicial do StateFlow,
        // antes do primeiro callback chegar. Depois disso, a fonte da verdade
        // passa a ser o callback.
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasInternet()
    }

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    init {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Rede default surgiu, mas pode não estar validada ainda.
                // `onCapabilitiesChanged` virá logo em seguida com o status
                // real (VALIDATED). Não emitimos true aqui para evitar
                // "piscar" online durante captive portal.
            }

            override fun onLost(network: Network) {
                // A rede default caiu. Como usamos registerDefaultNetworkCallback,
                // `onLost` aqui sempre significa "ficamos sem rede default" até
                // o próximo `onAvailable`. Emite offline imediatamente.
                _state.value = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Fonte da verdade: capacidades REPORTADAS no argumento.
                // Não consulta cm.activeNetwork (que tem race em transições).
                _state.value = networkCapabilities.hasInternet()
            }
        }

        // Não desregistra — observer é singleton (vive pelo processo inteiro).
        cm.registerDefaultNetworkCallback(callback)
    }

    actual val isOnline: Flow<Boolean> = _state.asStateFlow()

    actual fun isOnlineNow(): Boolean = _state.value
}
