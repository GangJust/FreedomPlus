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

public val IconRes.Acute: ImageVector
    get() {
        if (_acute != null) {
            return _acute!!
        }
        _acute = Builder(name = "Acute", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(600.0f, 800.0f)
                quadToRelative(-134.0f, 0.0f, -227.0f, -93.0f)
                reflectiveQuadToRelative(-93.0f, -227.0f)
                quadToRelative(0.0f, -133.0f, 93.0f, -226.5f)
                reflectiveQuadTo(600.0f, 160.0f)
                quadToRelative(133.0f, 0.0f, 226.5f, 93.5f)
                reflectiveQuadTo(920.0f, 480.0f)
                quadToRelative(0.0f, 134.0f, -93.5f, 227.0f)
                reflectiveQuadTo(600.0f, 800.0f)
                close()
                moveTo(600.0f, 720.0f)
                quadToRelative(100.0f, 0.0f, 170.0f, -70.0f)
                reflectiveQuadToRelative(70.0f, -170.0f)
                quadToRelative(0.0f, -100.0f, -70.0f, -170.0f)
                reflectiveQuadToRelative(-170.0f, -70.0f)
                quadToRelative(-100.0f, 0.0f, -170.0f, 70.0f)
                reflectiveQuadToRelative(-70.0f, 170.0f)
                quadToRelative(0.0f, 100.0f, 70.0f, 170.0f)
                reflectiveQuadToRelative(170.0f, 70.0f)
                close()
                moveTo(640.0f, 464.0f)
                verticalLineToRelative(-104.0f)
                quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
                reflectiveQuadTo(600.0f, 320.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
                reflectiveQuadTo(560.0f, 360.0f)
                verticalLineToRelative(121.0f)
                quadToRelative(0.0f, 8.0f, 3.5f, 15.5f)
                reflectiveQuadTo(572.0f, 509.0f)
                lineToRelative(91.0f, 91.0f)
                quadToRelative(12.0f, 12.0f, 28.5f, 12.0f)
                reflectiveQuadToRelative(28.5f, -12.0f)
                quadToRelative(12.0f, -12.0f, 12.0f, -28.5f)
                reflectiveQuadTo(720.0f, 543.0f)
                lineToRelative(-80.0f, -79.0f)
                close()
                moveTo(120.0f, 360.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
                reflectiveQuadTo(80.0f, 320.0f)
                quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
                reflectiveQuadTo(120.0f, 280.0f)
                horizontalLineToRelative(80.0f)
                quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
                reflectiveQuadTo(240.0f, 320.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(200.0f, 360.0f)
                horizontalLineToRelative(-80.0f)
                close()
                moveTo(80.0f, 520.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
                reflectiveQuadTo(40.0f, 480.0f)
                quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
                reflectiveQuadTo(80.0f, 440.0f)
                horizontalLineToRelative(120.0f)
                quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
                reflectiveQuadTo(240.0f, 480.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(200.0f, 520.0f)
                lineTo(80.0f, 520.0f)
                close()
                moveTo(120.0f, 680.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
                reflectiveQuadTo(80.0f, 640.0f)
                quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
                reflectiveQuadTo(120.0f, 600.0f)
                horizontalLineToRelative(80.0f)
                quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
                reflectiveQuadTo(240.0f, 640.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(200.0f, 680.0f)
                horizontalLineToRelative(-80.0f)
                close()
                moveTo(600.0f, 480.0f)
                close()
            }
        }
        .build()
        return _acute!!
    }

private var _acute: ImageVector? = null
