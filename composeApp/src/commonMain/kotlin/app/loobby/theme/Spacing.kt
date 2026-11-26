package app.loobby.theme

import androidx.compose.ui.unit.dp

/**
 * Espaçamentos do app. As telas usam só estes valores, em vez de dp soltos, para
 * manterem o mesmo ritmo entre si.
 */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Margem lateral de toda tela. */
    val screen = lg

    /** Distância entre duas seções de uma tela. */
    val section = xxl
}

/** Raios dos cantos: controles, cards e sheets. */
object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
}
