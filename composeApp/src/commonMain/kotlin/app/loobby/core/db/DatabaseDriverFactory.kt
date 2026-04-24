package app.loobby.core.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Fábrica do driver do SQLDelight. Cada plataforma fornece sua própria
 * implementação:
 *  - Android: AndroidSqliteDriver (abre/cria o arquivo em `databases/` do app).
 *  - iOS: NativeSqliteDriver (arquivo sqlite no sandbox do app, zona NSCachesDirectory/NSDocumentDirectory gerenciada pelo driver).
 *
 * A criação do driver também é responsável por aplicar o schema (cria tabelas
 * na primeira execução) e rodar migrações nas versões seguintes.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
