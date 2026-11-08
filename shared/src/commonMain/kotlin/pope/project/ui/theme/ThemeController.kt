package pope.project.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import pope.project.platform.KeyValueStore

const val THEME_KEY = "pope-staff-theme"

/** Same rule as the reference `init()`: day between 06:00 and 17:59. */
fun isNightHour(hour: Int): Boolean = hour < 6 || hour >= 18

fun resolveInitialNight(saved: String?, hour: Int): Boolean = when (saved) {
    "night" -> true
    "day" -> false
    else -> isNightHour(hour)
}

@Stable
class ThemeController(initialNight: Boolean, private val store: KeyValueStore) {
    var isNight by mutableStateOf(initialNight)
        private set

    fun toggle() = updateNight(!isNight)

    fun updateNight(night: Boolean) {
        isNight = night
        store.putString(THEME_KEY, if (night) "night" else "day")
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided")
}
