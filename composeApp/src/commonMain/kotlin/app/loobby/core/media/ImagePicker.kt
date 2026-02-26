package app.loobby.core.media

data class PickedImage(
    val fileName: String,
    val bytes: ByteArray,
    val contentType: String
)

interface ImagePicker {
    suspend fun pickImage(): PickedImage?
}