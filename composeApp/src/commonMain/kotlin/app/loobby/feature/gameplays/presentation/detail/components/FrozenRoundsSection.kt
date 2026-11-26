package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.feature.games.presentation.GameCoverImage
import app.loobby.feature.gameplays.presentation.model.FrozenRound
import app.loobby.theme.Spacing

/** As rodadas que congelaram quando o desempate abriu, recolhidas por padrão. */
@Composable
fun FrozenRoundsSection(rounds: List<FrozenRound>, modifier: Modifier = Modifier) {
    if (rounds.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Esconder as rodadas anteriores" else "Ver as rodadas anteriores")
            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null)
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                rounds.forEach { FrozenRoundBlock(it) }
            }
        }
    }
}

@Composable
private fun FrozenRoundBlock(round: FrozenRound) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = if (round.round == 1) "Votação principal" else "Desempate · rodada ${round.round}",
            style = MaterialTheme.typography.labelLarge,
        )
        round.entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                GameCoverImage(url = entry.coverUrl, size = 24.dp)
                Text(text = entry.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    text = if (entry.votes == 1) "1 voto" else "${entry.votes} votos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
