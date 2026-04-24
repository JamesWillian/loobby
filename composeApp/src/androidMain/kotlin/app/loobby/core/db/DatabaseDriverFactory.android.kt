package app.loobby.core.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.loobby.db.LoobbyDatabase

/**
 * No Android o driver requer um Context (usa o applicationContext para abrir
 * o arquivo dentro de `/data/data/<pkg>/databases/`). O schema é gerado pelo
 * plugin do SQLDelight a partir dos .sq em `src/commonMain/sqldelight`.
 */
actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = LoobbyDatabase.Schema,
            context = context,
            name = DATABASE_FILE_NAME
        )
}

private const val DATABASE_FILE_NAME = "loobby_cache.db"
