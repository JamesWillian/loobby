package app.loobby.feature.groups.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    isAnonymous: Boolean,
    onCreateGroup: () -> Unit,
    onJoinGroup: () -> Unit,
    onInstantEvent: () -> Unit,
    onLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 360.dp)
        ) {

            // ── Header ──────────────────────────────────────────
            Text(
                text = "\uD83D\uDC4B",          // 👋
                fontSize = 48.sp
            )

            Text(
                text = "Bem-vindo ao Loobby!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Crie ou entre em um grupo para começar a organizar eventos com seus amigos.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // ── Primary actions ─────────────────────────────────
            WelcomeActionButton(
                icon = Icons.Outlined.GroupAdd,
                label = "Criar Grupo",
                onClick = onCreateGroup,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            WelcomeActionButton(
                icon = Icons.Outlined.Group,
                label = "Entrar em Grupo",
                onClick = onJoinGroup,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            WelcomeActionButton(
                icon = Icons.Outlined.ElectricBolt,
                label = "Evento Instantâneo",
                onClick = onInstantEvent,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )

            // ── Auth action (only for anonymous users) ──────────
            if (isAnonymous) {
                Spacer(Modifier.height(4.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(Modifier.height(4.dp))

                WelcomeActionButton(
                    icon = Icons.Outlined.Login,
                    label = "Fazer Login ou Registrar",
                    onClick = onLogin,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun WelcomeActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors()
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = colors
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}