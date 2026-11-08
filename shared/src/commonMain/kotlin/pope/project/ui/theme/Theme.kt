package pope.project.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pope_proyect.shared.generated.resources.Res
import pope_proyect.shared.generated.resources.dm_mono_400
import pope_proyect.shared.generated.resources.dm_mono_500
import pope_proyect.shared.generated.resources.dm_sans_400
import pope_proyect.shared.generated.resources.dm_sans_400_italic
import pope_proyect.shared.generated.resources.dm_sans_500
import pope_proyect.shared.generated.resources.dm_sans_600
import pope_proyect.shared.generated.resources.dm_sans_700

/** Design tokens translated 1:1 from the CSS variables of the reference staff app. */
@Immutable
data class PopeColors(
    val bg: Color,
    val surface: Color,
    val navy: Color,
    val navyMid: Color,
    val accent: Color,
    val accentLt: Color,
    val fg: Color,
    val fgMid: Color,
    val fgDim: Color,
    val ok: Color,
    val okBg: Color,
    val warn: Color,
    val warnBg: Color,
    val danger: Color,
    val dangerBg: Color,
    val border: Color,
    val isNight: Boolean,
)

/** :root[data-theme="day"] — Pope Cleaning site colours (#00275F / #2F7FD1 / #F5F8FC). */
val DayColors = PopeColors(
    bg = Color(0xFFF5F8FC),
    surface = Color(0xFFFFFFFF),
    navy = Color(0xFF00275F),
    navyMid = Color(0xFF2F7FD1),
    accent = Color(0xFF2F7FD1),
    accentLt = Color(0xFFEAF2FB),
    fg = Color(0xFF00275F),
    fgMid = Color(0xFF4A5B75),
    fgDim = Color(0xFF8A9BB4),
    ok = Color(0xFF0E8A5A),
    okBg = Color(0xFFD6F4E8),
    warn = Color(0xFFB45A00),
    warnBg = Color(0xFFFDECD5),
    danger = Color(0xFFC0273A),
    dangerBg = Color(0xFFFDDDE2),
    border = Color(0xFFD9E3F0),
    isNight = false,
)

/** :root[data-theme="night"]. */
val NightColors = PopeColors(
    bg = Color(0xFF06101E),
    surface = Color(0xFF0D1B30),
    navy = Color(0xFF071525),
    navyMid = Color(0xFF0E2040),
    accent = Color(0xFF5BA8F5),
    accentLt = Color(0xFF091829),
    fg = Color(0xFFC8DCF5),
    fgMid = Color(0xFF6A8DB0),
    fgDim = Color(0xFF334D68),
    ok = Color(0xFF34D99E),
    okBg = Color(0xFF052518),
    warn = Color(0xFFF5A623),
    warnBg = Color(0xFF211400),
    danger = Color(0xFFF55068),
    dangerBg = Color(0xFF1E030D),
    border = Color(0xFF112030),
    isNight = true,
)

/** Maps the tokens onto Material 3 so stock widgets (date picker, switches, sheets) stay on-brand. */
fun PopeColors.toColorScheme(): ColorScheme {
    val onAccent = if (isNight) bg else Color.White
    return if (isNight) {
        darkColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = accentLt, onPrimaryContainer = accent,
            secondary = accent, onSecondary = onAccent,
            secondaryContainer = accentLt, onSecondaryContainer = accent,
            background = bg, onBackground = fg,
            surface = surface, onSurface = fg,
            surfaceVariant = accentLt, onSurfaceVariant = fgMid,
            surfaceContainerLowest = surface, surfaceContainerLow = surface,
            surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = accentLt,
            outline = border, outlineVariant = border,
            error = danger, onError = Color.White,
            errorContainer = dangerBg, onErrorContainer = danger,
        )
    } else {
        lightColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = accentLt, onPrimaryContainer = accent,
            secondary = accent, onSecondary = onAccent,
            secondaryContainer = accentLt, onSecondaryContainer = accent,
            background = bg, onBackground = fg,
            surface = surface, onSurface = fg,
            surfaceVariant = accentLt, onSurfaceVariant = fgMid,
            surfaceContainerLowest = surface, surfaceContainerLow = surface,
            surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = accentLt,
            outline = border, outlineVariant = border,
            error = danger, onError = Color.White,
            errorContainer = dangerBg, onErrorContainer = danger,
        )
    }
}

val LocalPopeColors = staticCompositionLocalOf { DayColors }
val LocalMonoFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }

object PopeTheme {
    val colors: PopeColors
        @Composable @ReadOnlyComposable get() = LocalPopeColors.current

    /** DM Mono — used for times and dates, like the reference. */
    val mono: FontFamily
        @Composable @ReadOnlyComposable get() = LocalMonoFamily.current
}

@Composable
private fun dmSansFamily() = FontFamily(
    Font(Res.font.dm_sans_400, FontWeight.Normal),
    Font(Res.font.dm_sans_400_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.dm_sans_500, FontWeight.Medium),
    Font(Res.font.dm_sans_600, FontWeight.SemiBold),
    Font(Res.font.dm_sans_700, FontWeight.Bold),
)

@Composable
private fun dmMonoFamily() = FontFamily(
    Font(Res.font.dm_mono_400, FontWeight.Normal),
    Font(Res.font.dm_mono_500, FontWeight.Medium),
)

private fun popeTypography(sans: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = sans),
        displayMedium = base.displayMedium.copy(fontFamily = sans),
        displaySmall = base.displaySmall.copy(fontFamily = sans),
        headlineLarge = base.headlineLarge.copy(fontFamily = sans),
        headlineMedium = base.headlineMedium.copy(fontFamily = sans),
        headlineSmall = base.headlineSmall.copy(fontFamily = sans),
        titleLarge = base.titleLarge.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.copy(fontFamily = sans, fontWeight = FontWeight.SemiBold),
        // body: 15px / line-height 1.45 like `html, body` in the reference.
        bodyLarge = base.bodyLarge.copy(fontFamily = sans, fontSize = 15.sp, lineHeight = 1.45.em, letterSpacing = 0.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = sans, lineHeight = 1.45.em),
        bodySmall = base.bodySmall.copy(fontFamily = sans, lineHeight = 1.45.em),
        labelLarge = base.labelLarge.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        labelMedium = base.labelMedium.copy(fontFamily = sans),
        labelSmall = base.labelSmall.copy(fontFamily = sans),
    )
}

@Composable
fun PopeTheme(isNight: Boolean, content: @Composable () -> Unit) {
    val colors = if (isNight) NightColors else DayColors
    val sans = dmSansFamily()
    val mono = dmMonoFamily()
    val typography = remember(sans) { popeTypography(sans) }
    CompositionLocalProvider(
        LocalPopeColors provides colors,
        LocalMonoFamily provides mono,
    ) {
        MaterialTheme(colorScheme = colors.toColorScheme(), typography = typography, content = content)
    }
}
