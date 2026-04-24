package app.loobby.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Observa o estado de conectividade de rede do aparelho.
 *
 * Contrato:
 *  - [isOnline] emite `true` quando o aparelho tem uma rede com capacidade
 *    real de internet (validada pelo SO quando possível), e `false` caso
 *    contrário. Emite o valor atual imediatamente para novos collectors.
 *  - [isOnlineNow] devolve um snapshot síncrono do último estado conhecido.
 *    Útil para guard-clauses em repositórios/useCases antes de chamar API.
 *
 * Não faz ping em servidor próprio — apenas reflete o que o sistema
 * operacional reporta. Implementações actuais:
 *   • Android: ConnectivityManager + NetworkCapabilities.NET_CAPABILITY_VALIDATED
 *   • iOS:     NWPathMonitor.pathUpdateHandler
 */
expect class ConnectivityObserver {
    val isOnline: Flow<Boolean>
    fun isOnlineNow(): Boolean
}
