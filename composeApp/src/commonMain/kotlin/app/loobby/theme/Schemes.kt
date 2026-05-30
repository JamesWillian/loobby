package app.loobby.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

// ── Schemes ───────────────────────────────────────────────────────────────────

val DarkScheme = darkColorScheme(
    primary              = LoobbyColors.Primary,
    onPrimary            = LoobbyColors.OnPrimary,
    primaryContainer     = Color(0xFF003399),
    onPrimaryContainer   = Color(0xFFD0E4FF),

    secondary            = LoobbyColors.Secondary,
    onSecondary          = LoobbyColors.OnSecondary,

    background           = LoobbyColors.Background,
    onBackground         = LoobbyColors.OnBgDark,

    surface              = LoobbyColors.Surface,
    onSurface            = LoobbyColors.OnBgDark,

    surfaceVariant       = LoobbyColors.SurfaceVar,
    onSurfaceVariant     = LoobbyColors.OnSurfaceVarD,

    error                = LoobbyColors.Error,
    onError              = LoobbyColors.OnError,
)

val LightScheme = lightColorScheme(
    primary              = LoobbyColors.Primary,
    onPrimary            = LoobbyColors.OnPrimary,
    primaryContainer     = Color(0xFFD6E4FF),
    onPrimaryContainer   = Color(0xFF001A66),

    secondary            = LoobbyColors.Secondary,
    onSecondary          = LoobbyColors.OnSecondary,

    background           = LoobbyColors.BackgroundLt,
    onBackground         = LoobbyColors.OnBgLight,

    surface              = LoobbyColors.SurfaceLt,
    onSurface            = LoobbyColors.OnBgLight,

    surfaceVariant       = LoobbyColors.SurfaceVarLt,
    onSurfaceVariant     = LoobbyColors.OnSurfaceVarL,

    error                = LoobbyColors.Error,
    onError              = LoobbyColors.OnError,
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
