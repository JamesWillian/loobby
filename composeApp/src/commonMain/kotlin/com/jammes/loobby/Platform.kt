package com.jammes.loobby

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform