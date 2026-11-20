package app.loobby.core.appconfig

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/** versionCode e versionName do build.gradle.kts, lidos do pacote instalado. */
fun androidAppVersion(context: Context): AppVersion {
    val info = runCatching {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
    }.getOrNull()

    return AppVersion(
        platform = AppPlatform.ANDROID,
        // Sem o pacote (não acontece com o app instalado), 0: o app nunca se bloqueia.
        build = info?.let(PackageInfoCompat::getLongVersionCode) ?: 0L,
        name = info?.versionName.orEmpty(),
    )
}
