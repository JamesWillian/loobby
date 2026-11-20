package app.loobby.core.appconfig

import platform.Foundation.NSBundle

/**
 * CFBundleVersion (CURRENT_PROJECT_VERSION no Xcode) e CFBundleShortVersionString
 * (MARKETING_VERSION). O build pode vir como "12" ou "12.0.1"; vale o primeiro número.
 */
fun iosAppVersion(): AppVersion {
    val bundle = NSBundle.mainBundle
    val build = (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)
        ?.substringBefore('.')
        ?.toLongOrNull()
        ?: 0L
    val name = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
    return AppVersion(platform = AppPlatform.IOS, build = build, name = name.orEmpty())
}
