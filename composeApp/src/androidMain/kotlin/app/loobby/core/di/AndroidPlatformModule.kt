package app.loobby.core.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.loobby.LoobbyApplication
import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.network.ConnectivityObserver
import coil3.PlatformContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientEngine> { Android.create() }

    // ConnectivityObserver precisa de Context — usa o application context
    // exposto pelo LoobbyApplication (já inicializado em onCreate).
    single<ConnectivityObserver> { ConnectivityObserver(LoobbyApplication.appContext) }

    // PlatformContext do Coil no Android é um alias para android.content.Context.
    // Usamos o applicationContext para não vazar Activity.
    single<PlatformContext> { LoobbyApplication.appContext }

    // SQLDelight — AndroidSqliteDriver precisa do Context (para abrir o
    // arquivo em /data/data/<pkg>/databases/).
    single<DatabaseDriverFactory> { DatabaseDriverFactory(LoobbyApplication.appContext) }

    // Sobrescreve o `single { Settings() }` do coreModule. O default do
    // multiplatform-settings-no-arg em Android usa SharedPreferences em
    // texto plano — qualquer device com root, debugger ou backup ativo
    // consegue ler os tokens JWT. Aqui trocamos por EncryptedSharedPreferences
    // (AES-256 GCM para valores, AES-256 SIV para chaves) com a master key
    // gerada e guardada no Android KeyStore.
    //
    // O nome do arquivo (`loobby_secure_prefs`) é diferente do default do
    // PreferenceManager para evitar colisão se algum dia voltarmos a usar
    // o Settings padrão.
    single<Settings> {
        val context = LoobbyApplication.appContext
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            "loobby_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        SharedPreferencesSettings(prefs)
    }
}
