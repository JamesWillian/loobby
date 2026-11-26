package app.loobby.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

/**
 * Coleta o estado sem esperar o próximo despacho. Tela com campo de texto precisa
 * disto: com o `collectAsState()` comum, a tecla que chega antes de a coleta rodar
 * é aplicada sobre o texto velho, e o caractere anterior some (digitação rápida,
 * colar, autocorreção do iOS).
 */
@Composable
fun <T> StateFlow<T>.collectAsImmediateState(): State<T> = collectAsState(Dispatchers.Main.immediate)
