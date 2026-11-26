package app.loobby.feature.gameplays.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import app.loobby.feature.gameplays.presentation.model.ModeHighlight

/**
 * "Na frente: Counter-Strike 2", "Empate: A e B", "Jogo: Hades", "Jogo livre". Na
 * encerrada, o que se jogou: "Escolhido: CS2", ou "Finalistas: A, B e C" quando a
 * votação tinha mais de um finalista.
 */
@Composable
fun ModeHighlightLine(highlight: ModeHighlight, modifier: Modifier = Modifier, isEnded: Boolean = false) {
    val (prefix, value) = when (highlight) {
        is ModeHighlight.Game -> "Jogo: " to highlight.name
        is ModeHighlight.Leading -> when {
            highlight.isTie -> "Empate: " to highlight.names.joinNames()
            isEnded && highlight.names.size > 1 -> "Finalistas: " to highlight.names.joinNames()
            isEnded -> "Escolhido: " to highlight.names.joinNames()
            else -> "Na frente: " to highlight.names.joinNames()
        }
        ModeHighlight.NoVotes -> "" to if (isEnded) "Ninguém votou" else "Ainda sem votos"
        ModeHighlight.Free -> "" to "Jogo livre"
    }
    Text(
        text = buildAnnotatedString {
            append(prefix)
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)) {
                append(value)
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

private fun List<String>.joinNames(): String = when (size) {
    0 -> ""
    1 -> first()
    else -> dropLast(1).joinToString(", ") + " e " + last()
}
