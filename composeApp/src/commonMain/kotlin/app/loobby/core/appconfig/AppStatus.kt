package app.loobby.core.appconfig

/** O que o app precisa fazer ao abrir, segundo o /app/config. */
data class AppStatus(
    /** Não nulo: esta versão não pode mais ser usada, e só a tela de atualizar aparece. */
    val requiredUpdate: RequiredUpdate? = null,
    /** Não nulo: um aviso que a pessoa ainda não fechou. */
    val notice: AppNotice? = null,
)

data class RequiredUpdate(
    /** Título e texto enviados pela API. Nulos, a tela usa os próprios. */
    val title: String?,
    val message: String?,
    /** Nulo: a tela fica sem o botão Atualizar. */
    val storeUrl: String?,
)

data class AppNotice(
    val id: String,
    val title: String,
    val message: String,
)

internal const val DEFAULT_NOTICE_TITLE = "Aviso"

/**
 * Decide, para a versão instalada, se o app bloqueia e qual aviso mostra.
 *
 * Na dúvida, o app abre: sem a parte da plataforma na resposta, com versão mínima 0 ou
 * com número de build desconhecido (0), não há bloqueio. Um aviso limitado a algumas
 * versões também não aparece quando o build é desconhecido.
 */
fun AppConfigResponse.resolve(version: AppVersion, seenNoticeIds: Set<String>): AppStatus {
    val platform = when (version.platform) {
        AppPlatform.ANDROID -> android
        AppPlatform.IOS -> ios
    } ?: return AppStatus()
    val buildKnown = version.build > 0

    val update = if (buildKnown && version.build < platform.minVersion) {
        RequiredUpdate(
            title = platform.update?.title?.takeIf { it.isNotBlank() },
            message = platform.update?.message?.takeIf { it.isNotBlank() },
            storeUrl = platform.storeUrl?.takeIf(::isStoreUrl),
        )
    } else {
        null
    }

    val notice = platform.notice
        ?.takeIf { it.id.isNotBlank() && it.message.isNotBlank() }
        ?.takeIf { it.id !in seenNoticeIds }
        ?.takeIf { it.maxVersion <= 0 || (buildKnown && version.build <= it.maxVersion) }
        ?.let { AppNotice(id = it.id, title = it.title.ifBlank { DEFAULT_NOTICE_TITLE }, message = it.message) }

    return AppStatus(requiredUpdate = update, notice = notice)
}

/** Só endereços de loja ou web seguros viram botão: o app não abre esquema arbitrário. */
private fun isStoreUrl(url: String): Boolean =
    url.startsWith("https://") || url.startsWith("market://") || url.startsWith("itms-apps://")
