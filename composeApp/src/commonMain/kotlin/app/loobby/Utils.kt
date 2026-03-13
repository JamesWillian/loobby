package app.loobby

fun initials(text: String): String =
    text.split(" ").filter { it.isNotEmpty() }.joinToString("") { it.first().toString() }

fun groupImagePlaceholder(groupName: String): String =
    "https://placehold.co/100x100/005eff/FFFFFF.png?text=${initials(groupName)}"

fun userAvatarPlaceholder(): String =
    "https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png"
