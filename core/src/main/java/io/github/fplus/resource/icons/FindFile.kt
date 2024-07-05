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

public val IconRes.FindFile: ImageVector
    get() {
        if (_findFile != null) {
            return _findFile!!
        }
        _findFile = Builder(name = "FindFile", defaultWidth = 48.0.dp, defaultHeight = 48.0.dp,
                viewportWidth = 48.0f, viewportHeight = 48.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(24.0f, 30.45f)
                quadToRelative(1.85f, 0.0f, 3.125f, -1.25f)
                reflectiveQuadToRelative(1.275f, -3.15f)
                quadToRelative(0.0f, -1.85f, -1.275f, -3.125f)
                reflectiveQuadTo(24.0f, 21.65f)
                quadToRelative(-1.85f, 0.0f, -3.125f, 1.275f)
                reflectiveQuadTo(19.6f, 26.05f)
                quadToRelative(0.0f, 1.9f, 1.275f, 3.15f)
                reflectiveQuadTo(24.0f, 30.45f)
                close()
                moveTo(10.9f, 44.8f)
                quadToRelative(-1.45f, 0.0f, -2.55f, -1.1f)
                quadToRelative(-1.1f, -1.1f, -1.1f, -2.55f)
                verticalLineTo(6.85f)
                quadToRelative(0.0f, -1.45f, 1.1f, -2.55f)
                quadToRelative(1.1f, -1.1f, 2.55f, -1.1f)
                horizontalLineToRelative(17.4f)
                quadToRelative(0.8f, 0.0f, 1.525f, 0.325f)
                quadToRelative(0.725f, 0.325f, 1.225f, 0.925f)
                lineTo(39.9f, 14.6f)
                quadToRelative(0.4f, 0.5f, 0.625f, 1.125f)
                quadToRelative(0.225f, 0.625f, 0.225f, 1.225f)
                verticalLineTo(40.3f)
                lineToRelative(-10.0f, -10.0f)
                quadToRelative(0.65f, -0.85f, 0.975f, -1.975f)
                quadToRelative(0.325f, -1.125f, 0.325f, -2.275f)
                quadToRelative(0.0f, -3.35f, -2.35f, -5.7f)
                reflectiveQuadTo(24.0f, 18.0f)
                quadToRelative(-3.35f, 0.0f, -5.7f, 2.35f)
                reflectiveQuadToRelative(-2.35f, 5.7f)
                quadToRelative(0.0f, 3.4f, 2.35f, 5.725f)
                quadTo(20.65f, 34.1f, 24.0f, 34.1f)
                quadToRelative(1.2f, 0.0f, 2.25f, -0.25f)
                reflectiveQuadTo(28.3f, 33.0f)
                lineToRelative(10.85f, 10.9f)
                quadToRelative(-0.6f, 0.45f, -1.9f, 0.675f)
                quadToRelative(-1.3f, 0.225f, -2.55f, 0.225f)
                close()
            }
        }
        .build()
        return _findFile!!
    }

private var _findFile: ImageVector? = null
