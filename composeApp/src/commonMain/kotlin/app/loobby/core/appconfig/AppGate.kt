package app.loobby.core.appconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.lifecycle.OnResumeEffect
import org.koin.compose.koinInject

/**
 * Fica em volta do app inteiro. Com a versão abaixo da mínima, mostra só a tela de
 * atualizar e [content] nem é montado — nada roda por trás dela, nem o login anônimo.
 * Fora isso, mostra [content] e, por cima, o aviso que a pessoa ainda não fechou.
 */
@Composable
fun AppGate(
    vm: AppStatusViewModel = koinInject(),
    content: @Composable () -> Unit,
) {
    val status by vm.status.collectAsState()

    // Também na volta ao app: a versão mínima pode ter subido com ele em segundo plano.
    OnResumeEffect { vm.refresh() }

    val update = status.requiredUpdate
    if (update != null) {
        TrackScreen(AppScreen.UPDATE_REQUIRED)
        UpdateRequiredScreen(update)
    } else {
        content()

        status.notice?.let { notice ->
            // A chave distingue um aviso do outro e fica no aparelho, como nas telas.
            TrackScreen(AppScreen.NOTICE, key = "${AppScreen.NOTICE.id}:${notice.id}")
            AppNoticeDialog(
                notice = notice,
                onDismiss = { vm.dismissNotice(notice.id) }
            )
        }
    }
}
