package app.loobby.core.lifecycle

import androidx.compose.runtime.Composable

@Composable
expect fun OnResumeEffect(onResume: () -> Unit)