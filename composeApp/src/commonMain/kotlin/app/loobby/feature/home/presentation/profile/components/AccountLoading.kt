package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.loobby.theme.Spacing

/** O topo do Perfil enquanto a conta ainda não chegou. */
@Composable
fun AccountLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xxxl), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
