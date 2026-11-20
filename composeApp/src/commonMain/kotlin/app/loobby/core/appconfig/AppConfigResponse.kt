package app.loobby.core.appconfig

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Corpo do GET /app/config. A mesma resposta vai para todo mundo: o app escolhe a parte
 * da sua plataforma e compara a própria versão — nada do aparelho é enviado.
 *
 * Todo campo tem default, para a API poder ganhar campos ou deixar de mandar os
 * opcionais sem quebrar esta versão do app.
 */
@Serializable
data class AppConfigResponse(
    val android: PlatformConfigResponse? = null,
    val ios: PlatformConfigResponse? = null,
)

@Serializable
data class PlatformConfigResponse(
    /** Menor número de build que ainda pode usar o app. 0 = nenhuma versão bloqueada. */
    val minVersion: Long = 0,
    /** Para onde o botão Atualizar leva. */
    val storeUrl: String? = null,
    /** Título e texto da tela de atualizar; sem eles, a tela usa os próprios. */
    val update: AppMessageResponse? = null,
    val notice: AppNoticeResponse? = null,
)

@Serializable
data class AppMessageResponse(
    val title: String = "",
    val message: String = "",
)

@Serializable
data class AppNoticeResponse(
    /** Aviso fechado não volta, até a API trocar o id. */
    val id: String = "",
    val title: String = "",
    val message: String = "",
    /** Só aparece até este número de build, inclusive. 0 = em todas as versões. */
    val maxVersion: Long = 0,
)

/** Lê a resposta da rede e a do cache do mesmo jeito, tolerando campo novo ou nulo. */
internal val AppConfigJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
