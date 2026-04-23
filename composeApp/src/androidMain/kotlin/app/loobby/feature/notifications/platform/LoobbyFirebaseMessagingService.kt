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
import com.google.firebase.messaging.RemoteMessage

/**
 * Recebe pushes do FCM quando o app está em foreground (ou, em alguns casos,
 * mesmo em background se o payload tem apenas `data` sem `notification`).
 *
 * Monta uma notificação local com:
 *  - canal único "loobby_default"
 *  - deep link para o evento (passa eventId como extra para a MainActivity)
 *
 * Também intercepta `onNewToken` — neste caso, não registra aqui porque não
 * temos acesso ao Koin fora do Application.onCreate. Em vez disso, o
 * [app.loobby.feature.notifications.data.NotificationBootstrapper] (Turno 5)
 * re-registra sempre que a sessão muda, cobrindo refresh de token no próximo login.
 */
class LoobbyFirebaseMessagingService : FirebaseMessagingService() {

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
        val title = data["title"] ?: message.notification?.title ?: "Loobby"
        val body = data["body"] ?: message.notification?.body.orEmpty()
        val eventId = data["eventId"]
        val groupId = data["groupId"]

        ensureChannel()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (eventId != null) putExtra(EXTRA_EVENT_ID, eventId)
            if (groupId != null) putExtra(EXTRA_GROUP_ID, groupId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            (eventId ?: "").hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use hash do eventId como id para permitir stacking de notificações distintas
        manager.notify((eventId ?: title).hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Loobby",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de eventos e presenças"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "loobby_default"
        const val EXTRA_EVENT_ID = "loobby.extra.eventId"
        const val EXTRA_GROUP_ID = "loobby.extra.groupId"

        const val PREFS = "loobby.notifications"
        const val KEY_LAST_TOKEN = "last_fcm_token"
    }
}
