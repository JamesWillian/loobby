package app.loobby

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.loobby.core.navigation.DeepLinkCoordinator
import app.loobby.feature.notifications.platform.LoobbyFirebaseMessagingService
import app.loobby.theme.LoobbyTheme
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {

    private val deepLinkCoordinator: DeepLinkCoordinator by lazy {
        KoinPlatformTools.defaultContext().get().get()
    }

    /**
     * Lançador para solicitar POST_NOTIFICATIONS em runtime (Android 13+).
     * Falha silenciosa: se o usuário negar, apenas não recebe pushes — não bloqueia o app.
     */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignore result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Se a Activity foi aberta pela notificação, processa o eventId aqui.
        handlePushIntent(intent)

        // Pede permissão de notificação em Android 13+.
        maybeRequestNotificationPermission()

        setContent {
            // detecta o tema do sistema
            val isDarkTheme = isSystemInDarkTheme()

            // ajusta ícones da status bar (true = ícones escuros, false = ícones claros)
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme

            LoobbyTheme(darkTheme = isDarkTheme) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Activity em singleTop/existing-task recebe nova intent aqui.
        setIntent(intent)
        handlePushIntent(intent)
    }

    private fun handlePushIntent(intent: Intent?) {
        if (intent == null) return
        val eventId = intent.getStringExtra(LoobbyFirebaseMessagingService.EXTRA_EVENT_ID)
        val groupId = intent.getStringExtra(LoobbyFirebaseMessagingService.EXTRA_GROUP_ID)
        if (!eventId.isNullOrBlank()) {
            deepLinkCoordinator.pushEventDeepLink(eventId = eventId, groupId = groupId)
            // Remove os extras para não reprocessar em onCreate após rotação.
            intent.removeExtra(LoobbyFirebaseMessagingService.EXTRA_EVENT_ID)
            intent.removeExtra(LoobbyFirebaseMessagingService.EXTRA_GROUP_ID)
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MaterialTheme {
        App()
    }
}
