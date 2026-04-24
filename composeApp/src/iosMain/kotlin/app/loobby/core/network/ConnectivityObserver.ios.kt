package app.loobby.core.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

/**
 * Implementação iOS do [ConnectivityObserver] usando `NWPathMonitor` do
 * framework Network (iOS 12+).
 *
 * `nw_path_status_satisfied` significa que o sistema tem uma rota
 * "satisfatória" para tráfego de rede. É o equivalente mais próximo do
 * `NET_CAPABILITY_VALIDATED` do Android (a Apple não expõe validação por
 * captive portal no NWPath público).
 *
 * O monitor é guardado como propriedade para impedir que seja liberado
 * enquanto o observer estiver vivo (o callback pararia de disparar).
 * Como registramos o observer como `single` no Koin, ele vive pelo
 * processo inteiro — ou seja, não precisamos de método de stop.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ConnectivityObserver {

    private val _state = MutableStateFlow(false)

    // Mantém referências fortes ao monitor e à queue pelo tempo de vida do
    // observer (que é singleton). Sem isso, o NWPathMonitor seria liberado
    // e os callbacks parariam de chegar.
    private val monitor: nw_path_monitor_t? = nw_path_monitor_create()
    private val queue = dispatch_queue_create("app.loobby.connectivity", null)

    init {
        val m = monitor
        if (m != null) {
            nw_path_monitor_set_queue(m, queue)
            nw_path_monitor_set_update_handler(m) { path ->
                val satisfied = nw_path_get_status(path) == nw_path_status_satisfied
                _state.value = satisfied
            }
            nw_path_monitor_start(m)
        }
    }

    actual val isOnline: Flow<Boolean> = _state.asStateFlow()

    actual fun isOnlineNow(): Boolean = _state.value
}
