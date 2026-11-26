package app.loobby.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.network.LocalIsOnline
import app.loobby.theme.FourPlayersColors

/**
 * Banner que aparece quando o usuário está registrado mas não verificou o email.
 * Mostra botão de reenvio com countdown de cooldown.
 */
@Composable
fun EmailVerificationBanner(
    visible: Boolean,
    email: String?,
    isResending: Boolean,
    cooldownSeconds: Int,
    message: String?,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = LocalIsOnline.current
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = FourPlayersColors.BannerSurface,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MarkEmailUnread,
                    contentDescription = null,
                    tint = FourPlayersColors.Warning,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "Verifique seu email",
                    color = FourPlayersColors.OnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Text(
                    text = if (email != null) {
                        "Enviamos um link de confirmação para $email. Verifique para desbloquear todos os recursos."
                    } else {
                        "Verifique seu email para desbloquear todos os recursos."
                    },
                    color = FourPlayersColors.SubtleText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (message != null) {
                    Text(
                        text = message,
                        color = if ("reenviado" in message.lowercase()) FourPlayersColors.Success else FourPlayersColors.ErrorInline,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onResendClick,
                    enabled = !isResending && cooldownSeconds <= 0 && isOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FourPlayersColors.Success,
                        contentColor = FourPlayersColors.OnSuccess,
                        disabledContainerColor = FourPlayersColors.SuccessDisabled,
                        disabledContentColor = FourPlayersColors.OnSuccessDisabled
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = FourPlayersColors.OnSuccess
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Enviando...")
                    } else if (cooldownSeconds > 0) {
                        val min = cooldownSeconds / 60
                        val sec = cooldownSeconds % 60
                        Text("Reenviar em ${min}:${if (sec < 10) "0$sec" else "$sec"}")
                    } else {
                        Text("Reenviar email de verificação")
                    }
                }
            }
        }
    }
}
