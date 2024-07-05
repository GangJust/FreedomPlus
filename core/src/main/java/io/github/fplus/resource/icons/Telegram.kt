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

public val IconRes.Telegram: ImageVector
    get() {
        if (_telegram != null) {
            return _telegram!!
        }
        _telegram = Builder(name = "Telegram", defaultWidth = 48.0.dp, defaultHeight = 48.0.dp,
                viewportWidth = 1024.0f, viewportHeight = 1024.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(512.0f, 0.0f)
                curveTo(229.2f, 0.0f, 0.0f, 229.2f, 0.0f, 512.0f)
                reflectiveCurveToRelative(229.2f, 512.0f, 512.0f, 512.0f)
                reflectiveCurveToRelative(512.0f, -229.2f, 512.0f, -512.0f)
                reflectiveCurveTo(794.8f, 0.0f, 512.0f, 0.0f)
                close()
                moveTo(755.6f, 355.8f)
                lineToRelative(-81.4f, 383.6f)
                curveToRelative(-6.0f, 27.2f, -22.2f, 33.8f, -44.8f, 21.0f)
                lineToRelative(-124.0f, -91.4f)
                lineToRelative(-59.8f, 57.6f)
                curveToRelative(-6.6f, 6.6f, -12.2f, 12.2f, -25.0f, 12.2f)
                lineToRelative(8.8f, -126.2f)
                lineToRelative(229.8f, -207.6f)
                curveToRelative(10.0f, -8.8f, -2.2f, -13.8f, -15.4f, -5.0f)
                lineToRelative(-284.0f, 178.8f)
                lineToRelative(-122.4f, -38.2f)
                curveToRelative(-26.6f, -8.4f, -27.2f, -26.6f, 5.6f, -39.4f)
                lineToRelative(478.2f, -184.4f)
                curveToRelative(22.2f, -8.0f, 41.6f, 5.4f, 34.4f, 39.0f)
                close()
            }
        }
        .build()
        return _telegram!!
    }

private var _telegram: ImageVector? = null
