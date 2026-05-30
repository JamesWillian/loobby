package app.loobby.theme

import androidx.compose.ui.graphics.Color

// ── Paleta ────────────────────────────────────────────────────────────────────

object LoobbyColors {

    // ── Brand ─────────────────────────────────────────────────────────────────
    val Primary           = Color(0xFF005EFF)
    val Secondary         = Color(0xFF4D8FFF)   // usado para ações secundárias (ex: câmera de avatar)
    val Brand             = Color(0xFFE9BB41)   // ouro da logo — destaque de seleção na sidebar

    // ── Superfícies — dark ────────────────────────────────────────────────────
    val Background        = Color(0xFF1A1A24)
    val Surface           = Color(0xFF0F0F14)
    val SurfaceVar        = Color(0xFF22222E)

    // ── Superfícies — light ───────────────────────────────────────────────────
    val BackgroundLt      = Color(0xFFF7F7F7)
    val SurfaceLt         = Color(0xFFFFFFFF)
    val SurfaceVarLt      = Color(0xFFE8E8F0)

    // ── Conteúdo sobre cores de marca ──────────────────────────────────────────
    val OnPrimary         = Color(0xFFFFFFFF)
    val OnSecondary       = Color(0xFFFFFFFF)
    val OnBgDark          = Color(0xFFE8E8F0)
    val OnBgLight         = Color(0xFF111118)
    val OnSurfaceVarD     = Color(0xFFAAAAAC)
    val OnSurfaceVarL     = Color(0xFF55555C)

    // ── Error ──────────────────────────────────────────────────────────────────
    val Error             = Color(0xFFCF4545)
    val OnError           = Color(0xFFFFFFFF)

    // ── Banners (offline + verificação de email) ───────────────────────────────
    val BannerSurface     = Color(0xFF1A1D27)
    val Warning           = Color(0xFFFBBF24)   // ícone âmbar nos banners
    val SubtleText        = Color(0xFFA0A0A0)   // texto secundário do banner
    val ErrorInline       = Color(0xFFEF4444)   // mensagem de erro inline no banner

    // ── Success (botão de reenvio de email) ────────────────────────────────────
    val Success           = Color(0xFF4ADE80)
    val OnSuccess         = Color(0xFF0F1117)
    val SuccessDisabled   = Color(0xFF2A2D37)
    val OnSuccessDisabled = Color(0xFF707070)

    // ── RSVP — "Vou" (verde confirmado) ───────────────────────────────────────
    val Confirmed         = Color(0xFF2E7D32)
    val ConfirmedLight    = Color(0xFF4CAF50)
    val ConfirmedBg       = Color(0xFF1B3A1F)

    // ── RSVP — "Não vou" (vermelho) ───────────────────────────────────────────
    val Declined          = Color(0xFFEF5350)
    val DeclinedBg        = Color(0xFF3A1B1B)
    val DeclinedIcon      = Color(0xFFC62828)

    // ── RSVP — "Talvez" / Reserva (laranja) ───────────────────────────────────
    val Maybe             = Color(0xFFFFA726)
    val MaybeBg           = Color(0xFF3A2C0A)
    val MaybeIcon         = Color(0xFFF57F17)

    // ── Times (roxo) ──────────────────────────────────────────────────────────
    val TeamsAccent       = Color(0xFF7E57C2)
    val TeamsBg           = Color(0xFF1E1530)
    val TeamsIcon         = Color(0xFF512DA8)

    // ── Color picker de times (fallback quando hex inválido) ───────────────────
    val TeamColorFallback = Color(0xFF7C4DFF)
}
