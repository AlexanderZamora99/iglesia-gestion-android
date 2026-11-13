package cl.iglesia.gestion

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta institucional. Todos los colores de texto cumplen WCAG AA (4.5:1) sobre blanco.
val Brand = Color(0xFF0057FF)
val BrandLight = Color(0xFF1D4ED8)
val BrandSurface = Color(0xFFEFF6FF)
val BrandContainer = Color(0xFFDBEAFE)
val Gold = Color(0xFFD5B66A)
val GoldText = Color(0xFF725B27)
val GoldSurface = Color(0xFFFAF6EA)
val AppBackground = Color(0xFFF7F8FA)
val CardBorder = Color(0xFFE9EDF1)
val MutedSurface = Color(0xFFF2F4F6)
val TextPrimary = Color(0xFF18232D)
val TextSecondary = Color(0xFF53616D)
// Más oscuro que slate-500 para mantener 4.5:1 también sobre AppBackground y MutedSurface.
val TextMuted = Color(0xFF64727D)
val Success = Color(0xFF15803D)
val SuccessSurface = Color(0xFFDCFCE7)
val Warning = Color(0xFFB45309)
val WarningSurface = Color(0xFFFEF3C7)
val Danger = Color(0xFFB42318)
val DangerSurface = Color(0xFFFEE4E2)

/** Colores semánticos por estado: Por hacer, En curso, En pausa, Completada. */
data class StatusStyle(val label: String, val content: Color, val container: Color)

fun statusStyle(status: String, feminine: Boolean = true): StatusStyle = when (status) {
    "pending" -> StatusStyle("Por hacer", TextSecondary, MutedSurface)
    "in_progress" -> StatusStyle("En curso", BrandLight, BrandSurface)
    "paused" -> StatusStyle("En pausa", Warning, WarningSurface)
    "completed" -> StatusStyle(if (feminine) "Completada" else "Completado", Success, SuccessSurface)
    else -> StatusStyle("Sin estado", TextSecondary, MutedSurface)
}

// Jerarquía tipográfica moderada; los tamaños en sp respetan la escala del sistema.
private val ChurchTypography = Typography(
    displayLarge = TextStyle(fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.8).sp),
    displayMedium = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleSmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 25.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, color = TextPrimary),
    bodySmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = TextSecondary),
    labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
)

private val ChurchShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ChurchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Brand,
            onPrimary = Color.White,
            primaryContainer = BrandContainer,
            onPrimaryContainer = Brand,
            secondary = GoldText,
            onSecondary = Color.White,
            secondaryContainer = GoldSurface,
            onSecondaryContainer = GoldText,
            tertiary = BrandLight,
            background = AppBackground,
            onBackground = TextPrimary,
            surface = Color.White,
            onSurface = TextPrimary,
            surfaceVariant = MutedSurface,
            onSurfaceVariant = TextSecondary,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainer = Color.White,
            surfaceContainerHigh = Color.White,
            surfaceContainerHighest = MutedSurface,
            outline = Color(0xFFCBD5E1),
            outlineVariant = CardBorder,
            error = Danger,
            errorContainer = DangerSurface,
        ),
        typography = ChurchTypography,
        shapes = ChurchShapes,
        content = content,
    )
}
