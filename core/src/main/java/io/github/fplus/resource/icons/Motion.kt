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

public val IconRes.Motion: ImageVector
    get() {
        if (_motion != null) {
            return _motion!!
        }
        _motion = Builder(name = "Motion", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(2.88f, 7.88f)
                lineToRelative(1.54f, 1.54f)
                curveTo(4.15f, 10.23f, 4.0f, 11.1f, 4.0f, 12.0f)
                curveToRelative(0.0f, 4.41f, 3.59f, 8.0f, 8.0f, 8.0f)
                reflectiveCurveToRelative(8.0f, -3.59f, 8.0f, -8.0f)
                reflectiveCurveToRelative(-3.59f, -8.0f, -8.0f, -8.0f)
                curveToRelative(-0.9f, 0.0f, -1.77f, 0.15f, -2.58f, 0.42f)
                lineTo(7.89f, 2.89f)
                curveTo(9.15f, 2.32f, 10.54f, 2.0f, 12.0f, 2.0f)
                curveToRelative(5.52f, 0.0f, 10.0f, 4.48f, 10.0f, 10.0f)
                reflectiveCurveToRelative(-4.48f, 10.0f, -10.0f, 10.0f)
                reflectiveCurveTo(2.0f, 17.52f, 2.0f, 12.0f)
                curveTo(2.0f, 10.53f, 2.32f, 9.14f, 2.88f, 7.88f)
                close()
                moveTo(7.0f, 5.5f)
                curveTo(7.0f, 6.33f, 6.33f, 7.0f, 5.5f, 7.0f)
                reflectiveCurveTo(4.0f, 6.33f, 4.0f, 5.5f)
                reflectiveCurveTo(4.67f, 4.0f, 5.5f, 4.0f)
                reflectiveCurveTo(7.0f, 4.67f, 7.0f, 5.5f)
                close()
            }
        }
        .build()
        return _motion!!
    }

private var _motion: ImageVector? = null
