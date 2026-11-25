package app.loobby

import android.app.Application
import app.loobby.core.di.androidPlatformModule

class FourPlayersApplication : Application() {
    companion object {
        lateinit var appContext: android.content.Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        initKoin(extraModules = listOf(androidPlatformModule))
    }
}