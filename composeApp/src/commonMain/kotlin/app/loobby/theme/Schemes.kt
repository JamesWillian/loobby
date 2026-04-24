package app.loobby.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

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
