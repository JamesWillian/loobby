package app.loobby

fun initials(text: String): String =
    text.split(" ").filter { it.isNotEmpty() }.joinToString("") { it.first().toString() }

/** Iniciais do grupo num cinza neutro nos dois temas: o azul da marca é a cor de seleção. */
fun groupImagePlaceholder(groupName: String): String =
    "https://placehold.co/100x100/e8e8f0/55555c.png?text=${initials(groupName)}"

fun userAvatarPlaceholder(): String =
    "https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png"
