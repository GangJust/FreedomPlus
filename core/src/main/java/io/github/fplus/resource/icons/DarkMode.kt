package io.github.fplus.resource.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.github.fplus.resource.IconRes

public val IconRes.DarkMode: ImageVector
    get() {
        if (_darkMode != null) {
            return _darkMode!!
        }
        _darkMode = Builder(name = "DarkMode", defaultWidth = 48.0.dp, defaultHeight = 48.0.dp,
                viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(480.0f, 846.0f)
                quadToRelative(-152.0f, 0.0f, -259.0f, -107.0f)
                reflectiveQuadTo(114.0f, 480.0f)
                quadToRelative(0.0f, -126.0f, 67.5f, -216.5f)
                reflectiveQuadTo(364.0f, 134.0f)
                quadToRelative(56.0f, -16.0f, 81.0f, 8.5f)
                reflectiveQuadToRelative(10.0f, 86.5f)
                quadToRelative(-2.0f, 15.0f, -3.0f, 30.5f)
                reflectiveQuadToRelative(-1.0f, 28.5f)
                quadToRelative(0.0f, 92.0f, 65.0f, 156.5f)
                reflectiveQuadTo(673.0f, 509.0f)
                horizontalLineToRelative(28.5f)
                quadToRelative(13.5f, 0.0f, 28.5f, -1.0f)
                quadToRelative(69.0f, -15.0f, 91.5f, 15.0f)
                reflectiveQuadToRelative(3.5f, 85.0f)
                quadToRelative(-42.0f, 103.0f, -131.5f, 170.5f)
                reflectiveQuadTo(480.0f, 846.0f)
                close()
            }
        }
        .build()
        return _darkMode!!
    }

private var _darkMode: ImageVector? = null
