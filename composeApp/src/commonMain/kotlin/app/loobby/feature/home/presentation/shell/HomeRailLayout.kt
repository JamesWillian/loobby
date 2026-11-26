package app.loobby.feature.home.presentation.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.loobby.theme.Radius

/**
 * A barra lateral ao lado do conteúdo. O conteúdo fica sempre no mesmo lugar da
 * árvore, com ou sem a barra, para não perder o estado ao abrir e fechar.
 */
@Composable
fun HomeRailLayout(
    showRail: Boolean,
    rail: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (showRail) rail()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = if (showRail) Radius.lg else 0.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}
