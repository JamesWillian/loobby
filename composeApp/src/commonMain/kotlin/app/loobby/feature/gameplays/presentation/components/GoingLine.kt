package app.loobby.feature.gameplays.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.AvatarStack
import app.loobby.feature.gameplays.presentation.common.goingLabel
import app.loobby.theme.Spacing

/** Quem vai: os avatares e a contagem. Sem ninguém, "Ninguém confirmou ainda". */
@Composable
fun GoingLine(goingCount: Int, avatars: List<String?>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (goingCount > 0) AvatarStack(avatarUrls = avatars, total = goingCount)
        Text(
            text = if (goingCount > 0) goingLabel(goingCount) else "Ninguém confirmou ainda",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
