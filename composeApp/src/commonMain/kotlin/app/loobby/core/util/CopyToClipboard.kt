package app.loobby.core.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCopyToClipboard(): (text: String) -> Unit
