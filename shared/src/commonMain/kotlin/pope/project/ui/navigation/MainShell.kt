package pope.project.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import pope.project.domain.model.StaffProfile
import pope.project.ui.components.LocalToast
import pope.project.ui.components.PopeHeader
import pope.project.ui.components.PopeTabBar
import pope.project.ui.components.PopeToastHost
import pope.project.ui.components.TabItem
import pope.project.ui.components.rememberToastState
import pope.project.ui.icons.PopeIcons
import pope.project.ui.screens.detail.JobDetailScreen
import pope.project.ui.screens.history.HistoryScreen
import pope.project.ui.screens.incident.IncidentScreen
import pope.project.ui.screens.profile.ProfileScreen
import pope.project.ui.screens.today.TodayScreen
import pope.project.ui.theme.LocalThemeController
import pope.project.util.headerFormat
import kotlin.time.Clock

@Serializable data object TodayRoute
@Serializable data object HistoryRoute
@Serializable data object ProfileRoute
@Serializable data class JobDetailRoute(val jobId: String)
@Serializable data class IncidentRoute(val jobId: String)

enum class MainTab { TODAY, HISTORY, PROFILE }

private val tabs = listOf(
    TabItem(MainTab.TODAY, "Hoy", PopeIcons.Calendar),
    TabItem(MainTab.HISTORY, "Historial", PopeIcons.Clock),
    TabItem(MainTab.PROFILE, "Perfil", PopeIcons.User),
)

private fun NavDestination?.toTab(): MainTab = when {
    this == null -> MainTab.TODAY
    hasRoute<HistoryRoute>() -> MainTab.HISTORY
    hasRoute<ProfileRoute>() -> MainTab.PROFILE
    else -> MainTab.TODAY // Today, JobDetail and Incident live under "Hoy"
}

@Composable
fun MainShell(profile: StaffProfile) {
    val navController = rememberNavController()
    val toast = rememberToastState()
    val theme = LocalThemeController.current
    val clock = koinInject<Clock>()
    val timeZone = koinInject<TimeZone>()
    val dateLabel = remember { clock.todayIn(timeZone).headerFormat() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.destination.toTab()

    CompositionLocalProvider(LocalToast provides toast) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().imePadding()) {
                PopeHeader(profile, dateLabel, theme.isNight, theme::toggle)
                NavHost(
                    navController = navController,
                    startDestination = TodayRoute,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    enterTransition = { fadeIn(tween(180)) },
                    exitTransition = { fadeOut(tween(120)) },
                    popEnterTransition = { fadeIn(tween(180)) },
                    popExitTransition = { fadeOut(tween(120)) },
                ) {
                    composable<TodayRoute> {
                        TodayScreen(onOpenJob = { navController.navigate(JobDetailRoute(it)) })
                    }
                    composable<JobDetailRoute> { entry ->
                        val route = entry.toRoute<JobDetailRoute>()
                        JobDetailScreen(
                            jobId = route.jobId,
                            onBack = { navController.popBackStack() },
                            onReportIncident = { navController.navigate(IncidentRoute(route.jobId)) },
                        )
                    }
                    composable<IncidentRoute> { entry ->
                        val route = entry.toRoute<IncidentRoute>()
                        IncidentScreen(jobId = route.jobId, onClose = { navController.popBackStack() })
                    }
                    composable<HistoryRoute> { HistoryScreen() }
                    composable<ProfileRoute> { ProfileScreen() }
                }
                if (WindowInsets.ime.getBottom(LocalDensity.current) == 0) {
                    PopeTabBar(
                        tabs = tabs,
                        selected = currentTab,
                        onSelect = { tab ->
                            val route: Any = when (tab) {
                                MainTab.TODAY -> TodayRoute
                                MainTab.HISTORY -> HistoryRoute
                                MainTab.PROFILE -> ProfileRoute
                            }
                            // Like the reference: switching tabs always lands on the tab root.
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
            PopeToastHost(
                toast,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp + 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            )
        }
    }
}
