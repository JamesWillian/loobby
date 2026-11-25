package app.loobby.feature.gameplays.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.loobby.theme.Spacing

/**
 * O "Nova gameplay" flutuante, no canto de baixo de uma lista de gameplays. Recolhe
 * para o "+" quando a lista desce, para tapar menos os cards.
 */
@Composable
fun NewGameplayFab(
    visible: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.padding(FabMargin),
    ) {
        ExtendedFloatingActionButton(
            text = { Text(LABEL) },
            // Recolhido, o texto sai: o ícone passa a dizer o que o botão faz.
            icon = { Icon(Icons.Outlined.Add, contentDescription = if (expanded) null else LABEL) },
            onClick = onClick,
            expanded = expanded,
        )
    }
}

/** O espaço que a lista deixa embaixo para o último card não ficar sob o botão. */
val NewGameplayFabClearance: Dp get() = FabHeight + FabMargin * 2

private const val LABEL = "Nova gameplay"

/** A altura do FAB estendido do Material 3. */
private val FabHeight = 56.dp

private val FabMargin = Spacing.lg
