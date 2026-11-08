package pope.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import pope.project.ui.theme.PopeTheme

/* ─── Section label (.section-label) ─── */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.em,
        color = PopeTheme.colors.fgDim,
    )
}

/* ─── Section container (.section) ─── */
@Composable
fun Section(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/* ─── Chips (.chip-*) ─── */
enum class ChipVariant { NEUTRAL, OK, WARN, DANGER }

@Composable
fun PopeChip(
    text: String,
    variant: ChipVariant = ChipVariant.NEUTRAL,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    compact: Boolean = false,
) {
    val c = PopeTheme.colors
    val (bg, fg) = when (variant) {
        ChipVariant.NEUTRAL -> c.accentLt to c.accent
        ChipVariant.OK -> c.okBg to c.ok
        ChipVariant.WARN -> c.warnBg to c.warn
        ChipVariant.DANGER -> c.dangerBg to c.danger
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 3.dp else 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        if (text.isNotEmpty()) {
            Text(text, fontSize = if (compact) 11.sp else 12.sp, fontWeight = FontWeight.SemiBold, color = fg, maxLines = 1)
        }
    }
}

/* ─── Cards ─── */
@Composable
fun PopeCard(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(20.dp),
    spacing: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = PopeTheme.colors
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.border, shape)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@Composable
fun PopeDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(PopeTheme.colors.border))
}

/* ─── Detail row (.detail-row) ─── */
@Composable
fun DetailRow(icon: ImageVector, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = PopeTheme.colors.fgMid, modifier = Modifier.padding(top = 2.dp).size(16.dp))
        content()
    }
}

/* ─── Buttons (.btn-*) ─── */
enum class ButtonVariant { PRIMARY, OK, SECONDARY, DANGER_SOFT }

@Composable
fun PopeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val c = PopeTheme.colors
    val (bg, fg) = when (variant) {
        ButtonVariant.PRIMARY -> c.navy to Color.White
        ButtonVariant.OK -> c.ok to Color.White
        ButtonVariant.SECONDARY -> c.accentLt to c.accent
        ButtonVariant.DANGER_SOFT -> c.dangerBg to c.danger
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .height(52.dp)
            .scale(if (pressed) 0.98f else 1f)
            .alpha(if (!enabled) 0.5f else if (pressed) 0.8f else 1f)
            .clip(shape)
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(color = fg, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                if (text.isNotEmpty()) Spacer(Modifier.width(8.dp))
            }
            if (text.isNotEmpty()) Text(text, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/* ─── Back link (.back-btn) ─── */
@Composable
fun BackLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(pope.project.ui.icons.PopeIcons.Back, contentDescription = null, tint = c.accent, modifier = Modifier.size(16.dp))
        Text(text, color = c.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/* ─── Avatar (.header-avatar) ─── */
@Composable
fun Avatar(initials: String, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(PopeTheme.colors.navyMid),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.39f).sp)
    }
}

/* ─── Dashed outline used for "Adjuntar foto" tiles ─── */
fun Modifier.dashedBorder(color: Color, radius: Dp = 12.dp, width: Dp = 1.5.dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
    )
}

/* ─── Empty state (.empty-state) ─── */
@Composable
fun EmptyState(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = c.ok, modifier = Modifier.size(40.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.fg)
        Text(subtitle, fontSize = 14.sp, color = c.fgMid, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
