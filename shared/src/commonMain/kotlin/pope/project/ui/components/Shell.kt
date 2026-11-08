package pope.project.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pope.project.domain.model.StaffProfile
import pope.project.ui.icons.PopeIcons
import pope.project.ui.theme.PopeTheme

/* ─── .app-header ─── */
@Composable
fun PopeHeader(
    profile: StaffProfile,
    dateLabel: String,
    isNight: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PopeTheme.colors
    Column(modifier.fillMaxWidth().background(c.navy).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(profile.initials)
            Column(Modifier.weight(1f)) {
                Text(
                    profile.fullName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${profile.role} · Pope Cleaning".uppercase(),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    letterSpacing = 0.06.em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(dateLabel, fontFamily = PopeTheme.mono, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onToggleTheme),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isNight) PopeIcons.Moon else PopeIcons.Sun,
                    contentDescription = "Cambiar tema",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/* ─── .tab-bar ─── */
data class TabItem<T>(val key: T, val label: String, val icon: ImageVector)

@Composable
fun <T> PopeTabBar(
    tabs: List<TabItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PopeTheme.colors
    Column(modifier.fillMaxWidth().background(c.surface)) {
        PopeDivider()
        Row(Modifier.fillMaxWidth().height(64.dp)) {
            tabs.forEach { tab ->
                val active = tab.key == selected
                val color = if (active) c.accent else c.fgDim
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab.key) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    Text(
                        tab.label.uppercase(),
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.04.em,
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/* ─── .toast ─── */
@Stable
class ToastState(private val scope: CoroutineScope) {
    var message by mutableStateOf("")
        private set
    var visible by mutableStateOf(false)
        private set
    private var hideJob: Job? = null

    fun show(text: String) {
        hideJob?.cancel()
        message = text
        visible = true
        hideJob = scope.launch {
            delay(2800)
            visible = false
        }
    }
}

@Composable
fun rememberToastState(): ToastState {
    val scope = rememberCoroutineScope()
    return remember(scope) { ToastState(scope) }
}

val LocalToast = staticCompositionLocalOf<ToastState> { error("ToastState not provided") }

@Composable
fun PopeToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    AnimatedVisibility(
        visible = state.visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(c.fg)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(state.message, color = c.surface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
