package app.loobby

import android.app.Application
import app.loobby.core.di.androidPlatformModule

class LoobbyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(extraModules = listOf(androidPlatformModule))
    }
}