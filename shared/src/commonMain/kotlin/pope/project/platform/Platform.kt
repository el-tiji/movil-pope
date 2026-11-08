package pope.project.platform

import androidx.compose.runtime.Composable

/** Tiny persistent key/value store (SharedPreferences / NSUserDefaults). */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

@Composable
expect fun rememberKeyValueStore(): KeyValueStore

/** Handle returned by [rememberCameraLauncher]; call [launch] to open the camera. */
class CameraLauncher(private val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}

/**
 * Opens the platform camera. [onResult] receives a JPEG (downscaled) or null when cancelled.
 */
@Composable
expect fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher

/** Keeps system bar icons legible over the navy header and the themed tab bar. */
@Composable
expect fun SystemBarsAppearance(isNight: Boolean)
