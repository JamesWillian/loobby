package app.loobby.core.appconfig

interface AppConfigApi {
    /** O corpo do GET /app/config como veio, para ir ao cache sem passar por outro formato. */
    suspend fun fetchConfig(): String
}
