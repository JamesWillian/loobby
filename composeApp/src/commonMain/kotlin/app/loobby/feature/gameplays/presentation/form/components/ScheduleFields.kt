package app.loobby.feature.gameplays.presentation.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.DateTimeFields
import app.loobby.theme.Spacing

/** Data e hora da gameplay, com o atalho para começar agora. */
@Composable
fun ScheduleFields(
    dateDigits: String,
    timeDigits: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onStartNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        DateTimeFields(
            dateDigits = dateDigits,
            timeDigits = timeDigits,
            onDateChange = onDateChange,
            onTimeChange = onTimeChange,
        )
        AssistChip(
            onClick = onStartNow,
            label = { Text("Começar agora") },
            leadingIcon = { Icon(Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
    }
}
