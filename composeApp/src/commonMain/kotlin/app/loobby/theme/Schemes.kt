package app.loobby.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

// ── Schemes ───────────────────────────────────────────────────────────────────

val DarkScheme = darkColorScheme(
    primary              = FourPlayersColors.Primary,
    onPrimary            = FourPlayersColors.OnPrimary,
    primaryContainer     = Color(0xFF003399),
    onPrimaryContainer   = Color(0xFFD0E4FF),

    secondary            = FourPlayersColors.Secondary,
    onSecondary          = FourPlayersColors.OnSecondary,

    background           = FourPlayersColors.Background,
    onBackground         = FourPlayersColors.OnBgDark,

    surface              = FourPlayersColors.Surface,
    onSurface            = FourPlayersColors.OnBgDark,

    surfaceVariant       = FourPlayersColors.SurfaceVar,
    onSurfaceVariant     = FourPlayersColors.OnSurfaceVarD,

    error                = FourPlayersColors.Error,
    onError              = FourPlayersColors.OnError,
)

val LightScheme = lightColorScheme(
    primary              = FourPlayersColors.Primary,
    onPrimary            = FourPlayersColors.OnPrimary,
    primaryContainer     = Color(0xFFD6E4FF),
    onPrimaryContainer   = Color(0xFF001A66),

    secondary            = FourPlayersColors.Secondary,
    onSecondary          = FourPlayersColors.OnSecondary,

    background           = FourPlayersColors.BackgroundLt,
    onBackground         = FourPlayersColors.OnBgLight,

    surface              = FourPlayersColors.SurfaceLt,
    onSurface            = FourPlayersColors.OnBgLight,

    surfaceVariant       = FourPlayersColors.SurfaceVarLt,
    onSurfaceVariant     = FourPlayersColors.OnSurfaceVarL,

    error                = FourPlayersColors.Error,
    onError              = FourPlayersColors.OnError,
)

val textShadow = Shadow(
    color = Color.Black.copy(alpha = 0.6f), // Opacidade da sombra
    offset = Offset(x = 2f, y = 4f),        // Deslocamento (X para os lados, Y para baixo)
    blurRadius = 6f                         // Quão esfumaçada ela é
)

val textLightShadow = Shadow(
    color = Color.Black.copy(alpha = 0.9f), // Opacidade da sombra
    offset = Offset(x = 2f, y = 4f),        // Deslocamento (X para os lados, Y para baixo)
    blurRadius = 8f                         // Quão esfumaçada ela é
)
