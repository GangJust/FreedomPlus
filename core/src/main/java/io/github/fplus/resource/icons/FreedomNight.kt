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

public val IconRes.FreedomNight: ImageVector
    get() {
        if (_freedomNight != null) {
            return _freedomNight!!
        }
        _freedomNight = Builder(name = "FreedomNight", defaultWidth = 209.0.dp, defaultHeight =
                211.0.dp, viewportWidth = 209.0f, viewportHeight = 211.0f).apply {
            path(fill = SolidColor(Color(0xE6FFFFFF)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(142.3f, 180.72f)
                lineTo(142.3f, 164.67f)
                horizontalLineToRelative(-15.8f)
                arcToRelative(5.33f, 5.33f, 0.0f, false, true, -5.33f, -5.33f)
                arcTo(5.33f, 5.33f, 0.0f, false, true, 126.5f, 154.0f)
                horizontalLineToRelative(15.8f)
                lineTo(142.3f, 137.95f)
                arcToRelative(5.28f, 5.28f, 0.0f, false, true, 5.28f, -5.28f)
                arcToRelative(5.28f, 5.28f, 0.0f, false, true, 5.28f, 5.28f)
                lineTo(152.87f, 154.0f)
                horizontalLineToRelative(15.8f)
                arcTo(5.33f, 5.33f, 0.0f, false, true, 174.0f, 159.33f)
                arcToRelative(5.33f, 5.33f, 0.0f, false, true, -5.33f, 5.33f)
                horizontalLineToRelative(-15.8f)
                verticalLineToRelative(16.05f)
                arcTo(5.28f, 5.28f, 0.0f, false, true, 147.58f, 186.0f)
                arcTo(5.28f, 5.28f, 0.0f, false, true, 142.3f, 180.72f)
                close()
                moveTo(37.96f, 186.0f)
                arcTo(3.96f, 3.96f, 0.0f, false, true, 34.0f, 182.04f)
                verticalLineToRelative(-144.15f)
                arcTo(11.89f, 11.89f, 0.0f, false, true, 45.89f, 26.0f)
                arcToRelative(3.95f, 3.95f, 0.0f, false, true, 1.65f, 0.36f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 2.38f, -0.36f)
                horizontalLineToRelative(89.66f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineToRelative(-89.74f)
                verticalLineToRelative(74.67f)
                horizontalLineToRelative(63.32f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineToRelative(-63.32f)
                lineTo(49.85f, 182.04f)
                arcTo(3.96f, 3.96f, 0.0f, false, true, 45.89f, 186.0f)
                close()
            }
        }
        .build()
        return _freedomNight!!
    }

private var _freedomNight: ImageVector? = null
