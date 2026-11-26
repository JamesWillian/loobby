package app.loobby.feature.notifications.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.loobby.MainActivity
import app.loobby.R
import com.google.firebase.messaging.FirebaseMessagingService
import app.loobby.feature.notifications.platform.GameplayPushExtras.putGameplayExtras
import com.google.firebase.messaging.RemoteMessage

/**
 * Recebe pushes do FCM quando o app está em foreground (ou, em alguns casos,
 * mesmo em background se o payload tem apenas `data` sem `notification`).
 *
 * Monta uma notificação local com:
 *  - canal único "loobby_default"
 *  - deep link para a gameplay (os extras de [GameplayPushExtras])
 *
 * Também intercepta `onNewToken` — neste caso, não registra aqui porque não
 * temos acesso ao Koin fora do Application.onCreate. Em vez disso, o
 * [app.loobby.feature.notifications.data.NotificationBootstrapper] (Turno 5)
 * re-registra sempre que a sessão muda, cobrindo refresh de token no próximo login.
 */
class FourPlayersMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Persistimos a última versão conhecida em SharedPreferences; o
        // bootstrapper lê isso no login e registra.
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_TOKEN, token).apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "4Players"
        val body = data["body"] ?: message.notification?.body.orEmpty()
        // Push da 2.0 abre a gameplay; o da v1 (eventId) só abre o app.
        val gameplayId = data[GameplayPushExtras.GAMEPLAY_ID]
        val notificationKey = listOfNotNull(data[GameplayPushExtras.TYPE], gameplayId).joinToString(":").ifEmpty { title }

        ensureChannel()

        val contentIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putGameplayExtras(data)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationKey.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_stat_fourplayers)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Um aviso por tipo e gameplay: o lembrete não apaga o desempate.
        manager.notify(notificationKey.hashCode(), notification)
    }

    /**
     * Cria o canal ou, se ele já existe, atualiza o nome e a descrição (o id não muda,
     * para a pessoa não perder o que configurou nele).
     */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "4Players",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos das gameplays"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        // Ids gravados no aparelho: não mudam com o nome novo do app.
        private const val CHANNEL_ID = "loobby_default"

        const val PREFS = "loobby.notifications"
        const val KEY_LAST_TOKEN = "last_fcm_token"
    }
}
