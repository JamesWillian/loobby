package app.loobby.core.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.loobby.db.LoobbyDatabase

/**
 * No iOS o NativeSqliteDriver já administra a localização do arquivo dentro
 * do sandbox do app (Documents/databases/<name>). Não precisa de context.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(
            schema = LoobbyDatabase.Schema,
            name = DATABASE_FILE_NAME
        )
}

private const val DATABASE_FILE_NAME = "loobby_cache.db"
