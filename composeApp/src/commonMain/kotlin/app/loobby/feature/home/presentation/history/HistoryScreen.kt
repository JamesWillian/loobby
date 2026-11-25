package app.loobby.feature.home.presentation.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.EmptyState
import app.loobby.core.ui.components.ScreenHeader

/** A aba Histórico, por enquanto só o aviso: a lista das encerradas chega na Fase 3. */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Histórico")
        EmptyState(
            icon = Icons.Outlined.History,
            title = "Seu histórico vem aí",
            message = "As gameplays encerradas vão aparecer aqui, com o que cada uma jogou.",
        )
    }
}
