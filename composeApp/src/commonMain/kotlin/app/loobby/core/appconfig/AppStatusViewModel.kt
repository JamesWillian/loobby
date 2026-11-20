package app.loobby.core.appconfig

import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Versão mínima e aviso do /app/config, consultados ao abrir o app e ao voltar para ele.
 *
 * Começa pelo que ficou guardado da última consulta, para uma versão bloqueada não
 * mostrar o app nem por um instante. Qualquer falha — sem rede, API antiga sem a rota,
 * resposta ilegível — mantém o que já se sabia, e sem nada guardado o app abre: a
 * verificação nunca é motivo para travar quem pode usar o app.
 */
class AppStatusViewModel(
    private val api: AppConfigApi,
    private val store: AppConfigStore,
    private val version: AppVersion,
    private val crashReporter: CrashReporter,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /** Uma consulta por vez; a que chega durante outra é descartada. */
    private val refreshMutex = Mutex()

    /** Só lido e escrito com o [refreshMutex]. */
    private var lastSuccess: TimeMark? = null

    private var config: AppConfigResponse? = store.lastConfig()?.let(::decodeOrNull)

    private val _status = MutableStateFlow(currentStatus())
    val status: StateFlow<AppStatus> = _status.asStateFlow()

    init {
        refresh()
    }

    /**
     * Consulta a API de novo, se a última consulta bem-sucedida tiver mais de
     * [MIN_REFRESH_INTERVAL]. Chamado a cada volta ao app: quem deixa o app aberto em
     * segundo plano por dias também vê a versão mínima nova.
     */
    fun refresh() {
        scope.launch {
            if (!refreshMutex.tryLock()) return@launch
            try {
                if (lastSuccess?.let { it.elapsedNow() < MIN_REFRESH_INTERVAL } == true) return@launch
                val raw = withTimeoutOrNull(FETCH_TIMEOUT) { api.fetchConfig() } ?: return@launch
                val fresh = AppConfigJson.decodeFromString<AppConfigResponse>(raw)
                store.saveConfig(raw)
                config = fresh
                lastSuccess = timeSource.markNow()
                _status.value = currentStatus()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Rede e respostas de erro da API ficam de fora; resposta que não deu para
                // ler vai para o Crashlytics, porque é a API e o app em desacordo.
                crashReporter.reportIfUnexpected(e)
            } finally {
                refreshMutex.unlock()
            }
        }
    }

    /** A pessoa fechou o aviso: ele não volta neste aparelho. */
    fun dismissNotice(id: String) {
        store.markNoticeSeen(id)
        _status.update { if (it.notice?.id == id) it.copy(notice = null) else it }
    }

    private fun currentStatus(): AppStatus =
        config?.resolve(version, store.seenNoticeIds()) ?: AppStatus()

    private fun decodeOrNull(raw: String): AppConfigResponse? =
        runCatching { AppConfigJson.decodeFromString<AppConfigResponse>(raw) }.getOrNull()

    private companion object {
        val MIN_REFRESH_INTERVAL = 5.minutes
        val FETCH_TIMEOUT = 15.seconds
    }
}
