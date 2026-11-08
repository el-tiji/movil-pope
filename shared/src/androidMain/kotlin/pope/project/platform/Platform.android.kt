package pope.project.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import java.io.ByteArrayOutputStream
import java.io.File

private class SharedPrefsStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("pope_staff", Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { SharedPrefsStore(context) }
}

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    var pendingUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingUri?.let(Uri::parse)
        pendingUri = null
        val bytes = if (success && uri != null) {
            runCatching { readDownscaledJpeg(context, uri) }.getOrNull()
        } else null
        currentOnResult(bytes)
    }

    return remember(context) {
        CameraLauncher {
            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
            val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingUri = uri.toString()
            try {
                takePicture.launch(uri)
            } catch (_: ActivityNotFoundException) {
                pendingUri = null
                currentOnResult(null)
            }
        }
    }
}

/** Decodes with subsampling (max ~1600px) and re-encodes as JPEG to keep memory low. */
private fun readDownscaledJpeg(context: Context, uri: Uri, maxSide: Int = 1600): ByteArray? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
    val bitmap = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null
    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
        bitmap.recycle()
        out.toByteArray()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun SystemBarsAppearance(isNight: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false // navy header → light icons
            isAppearanceLightNavigationBars = !isNight
        }
    }
}
