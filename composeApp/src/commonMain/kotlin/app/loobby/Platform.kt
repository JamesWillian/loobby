package app.loobby

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform