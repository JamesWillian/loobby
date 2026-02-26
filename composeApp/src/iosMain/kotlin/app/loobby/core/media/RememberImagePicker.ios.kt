package app.loobby.core.media

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return object : ImagePicker {
        override suspend fun pickImage(): PickedImage? {
            // TODO: implementar com PhotosUI / PHPicker quando for testar iOS
            return null
        }
    }
}