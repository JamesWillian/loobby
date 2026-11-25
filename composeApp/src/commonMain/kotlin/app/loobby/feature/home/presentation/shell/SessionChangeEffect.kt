package app.loobby.feature.home.presentation.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * Chama [onUserChanged] quando outra conta entra no lugar da que estava (login,
 * cadastro, ou a conta anônima nova depois do logout). A primeira conta da
 * abertura não conta, e o perfil sumindo por um instante (null) também não.
 */
@Composable
fun SessionChangeEffect(userId: String?, onUserChanged: () -> Unit) {
    var lastUserId by remember { mutableStateOf<String?>(null) }
    val callback by rememberUpdatedState(onUserChanged)
    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        val previous = lastUserId
        lastUserId = userId
        if (previous != null && userId != previous) callback()
    }
}
