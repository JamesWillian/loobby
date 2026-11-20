package app.loobby.core.appconfig

/** Onde o app roda: decide qual parte da resposta do /app/config vale. */
enum class AppPlatform { ANDROID, IOS }

/**
 * A versão instalada, fornecida por cada plataforma (androidPlatformModule /
 * iosPlatformModule).
 *
 * [build] é o número que só cresce a cada publicação — o versionCode no Android, o
 * CFBundleVersion no iOS — e é ele que se compara com a versão mínima. Vale 0 quando o
 * sistema não informou um número válido; com 0, o app nunca se bloqueia.
 */
data class AppVersion(
    val platform: AppPlatform,
    val build: Long,
    val name: String,
)
