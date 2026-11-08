package pope.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import pope.project.di.initKoin
import pope.project.domain.repository.AuthRepository
import pope.project.platform.SystemBarsAppearance
import pope.project.platform.rememberKeyValueStore
import pope.project.ui.navigation.MainShell
import pope.project.ui.screens.login.LoginScreen
import pope.project.ui.theme.LocalThemeController
import pope.project.ui.theme.PopeTheme
import pope.project.ui.theme.THEME_KEY
import pope.project.ui.theme.ThemeController
import pope.project.ui.theme.resolveInitialNight
import kotlin.time.Clock

@Composable
fun App() {
    remember { initKoin() }
    val store = rememberKeyValueStore()
    val themeController = remember(store) {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        ThemeController(resolveInitialNight(store.getString(THEME_KEY), hour), store)
    }

    PopeTheme(isNight = themeController.isNight) {
        SystemBarsAppearance(themeController.isNight)
        CompositionLocalProvider(LocalThemeController provides themeController) {
            Surface(Modifier.fillMaxSize(), color = PopeTheme.colors.bg, contentColor = PopeTheme.colors.fg) {
                val auth = koinInject<AuthRepository>()
                val session by auth.session.collectAsStateWithLifecycle()
                AnimatedContent(
                    targetState = session,
                    contentKey = { it != null },
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                ) { profile ->
                    if (profile == null) LoginScreen() else MainShell(profile)
                }
            }
        }
    }
}
