package app.loobby

fun initials(text: String): String =
    text.split(" ").filter { it.isNotEmpty() }.joinToString("") { it.first().toString() }