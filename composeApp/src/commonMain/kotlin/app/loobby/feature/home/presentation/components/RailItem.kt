package app.loobby.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.loobby.theme.FourPlayersColors
import app.loobby.theme.Radius

/** Um item da barra lateral: quadrado de cantos arredondados, com borda quando escolhido. */
@Composable
fun RailItem(
    selected: Boolean,
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(2.dp, if (selected) FourPlayersColors.Brand else Color.Transparent),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) FourPlayersColors.Brand.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}
