package app.loobby.feature.notifications.platform

import android.content.Intent
import app.loobby.core.navigation.DeepLinkCoordinator

/**
 * As chaves do `data` dos pushes da 2.0. Com o app fechado, o Android entrega o
 * `data` como extras do Intent que abre o app; com o app aberto, a notificação
 * montada aqui usa as mesmas chaves. Assim os dois caminhos chegam iguais.
 */
object GameplayPushExtras {
    const val GAMEPLAY_ID = "gameplayId"
    const val GROUP_ID = "groupId"
    const val TYPE = "type"

    /** Copia do push para o Intent o que abre a gameplay. */
    fun Intent.putGameplayExtras(data: Map<String, String>): Intent = apply {
        data[GAMEPLAY_ID]?.let { putExtra(GAMEPLAY_ID, it) }
        data[GROUP_ID]?.let { putExtra(GROUP_ID, it) }
        data[TYPE]?.let { putExtra(TYPE, it) }
    }

    /**
     * Se o app abriu por um push de gameplay, manda abrir a gameplay e tira os
     * extras, para não abrir de novo quando a Activity for recriada.
     */
    fun Intent.consumeGameplayDeepLink(deepLinks: DeepLinkCoordinator) {
        val gameplayId = getStringExtra(GAMEPLAY_ID) ?: return
        deepLinks.openGameplay(
            gameplayId = gameplayId,
            groupId = getStringExtra(GROUP_ID),
            pushType = getStringExtra(TYPE),
        )
        removeExtra(GAMEPLAY_ID)
        removeExtra(GROUP_ID)
        removeExtra(TYPE)
    }
}
