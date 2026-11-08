package pope.project.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Stroke icons re-drawn from the SVGs of the reference design (24×24 viewBox). */
object PopeIcons {
    val Calendar by lazy {
        strokeIcon(
            "Calendar",
            "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
            "M16 2v4M8 2v4M3 10h18",
            "M9 14h1m5 0h1M9 18h1m5 0h1",
        )
    }
    val Clock by lazy { strokeIcon("Clock", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0-20 0", "M12 6v6l4 2") }
    val Sun by lazy {
        strokeIcon(
            "Sun",
            "M7 12a5 5 0 1 0 10 0a5 5 0 1 0-10 0",
            "M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42",
        )
    }
    val Moon by lazy { strokeIcon("Moon", "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z") }
    val Pin by lazy {
        strokeIcon("Pin", "M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0z", "M9 10a3 3 0 1 0 6 0a3 3 0 1 0-6 0")
    }
    val Briefcase by lazy {
        strokeIcon(
            "Briefcase",
            "M4 7h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2z",
            "M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2",
        )
    }
    val Note by lazy {
        strokeIcon(
            "Note",
            "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z",
            "M14 2v6h6M16 13H8M16 17H8M10 9H8",
        )
    }
    val Play by lazy { strokeIcon("Play", "M5 3L19 12L5 21Z", strokeWidth = 2.2f) }
    val Check by lazy { strokeIcon("Check", "M20 6 9 17l-5-5", strokeWidth = 2.5f) }
    val Back by lazy { strokeIcon("Back", "M19 12H5m7-7-7 7 7 7", strokeWidth = 2.5f) }
    val Camera by lazy {
        strokeIcon(
            "Camera",
            "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z",
            "M9 13a3 3 0 1 0 6 0a3 3 0 1 0-6 0",
        )
    }
    val Send by lazy { strokeIcon("Send", "M22 2 11 13M22 2 15 22 11 13 2 9l20-7z", strokeWidth = 2.2f) }
    val User by lazy {
        strokeIcon("User", "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2", "M8 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0")
    }
    val LogOut by lazy {
        strokeIcon("LogOut", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", "M16 17l5-5-5-5M21 12H9")
    }
    val LogIn by lazy {
        strokeIcon("LogIn", "M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4", "M10 17l5-5-5-5M15 12H3")
    }
    val Plus by lazy { strokeIcon("Plus", "M12 5v14M5 12h14") }
    val Close by lazy { strokeIcon("Close", "M18 6 6 18M6 6l12 12", strokeWidth = 2.5f) }
    val Sparkle by lazy {
        strokeIcon("Sparkle", "M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9z", "M19 16v4M17 18h4")
    }
}

private fun strokeIcon(name: String, vararg paths: String, strokeWidth: Float = 2f): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
