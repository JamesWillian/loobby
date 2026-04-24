package app.loobby.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.network.LocalIsOnline

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
    // Reenvio do email dispara a API; desabilita offline.
    val isOnline = LocalIsOnline.current
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = Color(0xFF1a1d27),
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
                    tint = Color(0xFFfbbf24),
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "Verifique seu email",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Text(
                    text = if (email != null) {
                        "Enviamos um link de confirmação para $email. Verifique para desbloquear todos os recursos."
                    } else {
                        "Verifique seu email para desbloquear todos os recursos."
                    },
                    color = Color(0xFFa0a0a0),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Mensagem de sucesso/erro do reenvio
                if (message != null) {
                    Text(
                        text = message,
                        color = if ("reenviado" in message.lowercase()) Color(0xFF4ade80) else Color(0xFFef4444),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onResendClick,
                    enabled = !isResending && cooldownSeconds <= 0 && isOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4ade80),
                        contentColor = Color(0xFF0f1117),
                        disabledContainerColor = Color(0xFF2a2d37),
                        disabledContentColor = Color(0xFF707070)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0f1117)
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