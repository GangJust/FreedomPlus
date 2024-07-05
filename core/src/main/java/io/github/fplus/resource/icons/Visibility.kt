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

public val IconRes.Visibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = Builder(name = "Visibility", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            path(fill = SolidColor(Color(0xFF333333)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(480.12f, 630.0f)
                quadTo(551.0f, 630.0f, 600.5f, 580.38f)
                quadTo(650.0f, 530.77f, 650.0f, 459.88f)
                quadTo(650.0f, 389.0f, 600.38f, 339.5f)
                quadTo(550.77f, 290.0f, 479.88f, 290.0f)
                quadTo(409.0f, 290.0f, 359.5f, 339.62f)
                quadTo(310.0f, 389.23f, 310.0f, 460.12f)
                quadTo(310.0f, 531.0f, 359.62f, 580.5f)
                quadTo(409.23f, 630.0f, 480.12f, 630.0f)
                close()
                moveTo(479.9f, 562.96f)
                quadTo(436.96f, 562.96f, 407.0f, 532.9f)
                quadTo(377.04f, 502.84f, 377.04f, 459.9f)
                quadTo(377.04f, 416.96f, 407.1f, 387.0f)
                quadTo(437.16f, 357.04f, 480.1f, 357.04f)
                quadTo(523.04f, 357.04f, 553.0f, 387.1f)
                quadTo(582.96f, 417.16f, 582.96f, 460.1f)
                quadTo(582.96f, 503.04f, 552.9f, 533.0f)
                quadTo(522.84f, 562.96f, 479.9f, 562.96f)
                close()
                moveTo(480.0f, 773.0f)
                quadTo(338.61f, 773.0f, 222.57f, 695.74f)
                quadTo(106.52f, 618.48f, 42.41f, 492.58f)
                quadTo(38.81f, 486.49f, 36.97f, 477.56f)
                quadTo(35.13f, 468.64f, 35.13f, 459.91f)
                quadTo(35.13f, 451.18f, 36.97f, 442.26f)
                quadTo(38.81f, 433.34f, 42.41f, 426.86f)
                quadTo(106.51f, 301.69f, 222.56f, 224.34f)
                quadTo(338.61f, 147.0f, 480.0f, 147.0f)
                quadTo(621.39f, 147.0f, 737.44f, 224.34f)
                quadTo(853.49f, 301.69f, 917.59f, 426.86f)
                quadTo(921.19f, 433.34f, 923.03f, 442.26f)
                quadTo(924.87f, 451.18f, 924.87f, 459.91f)
                quadTo(924.87f, 468.64f, 923.03f, 477.56f)
                quadTo(921.19f, 486.49f, 917.59f, 492.58f)
                quadTo(853.48f, 618.48f, 737.43f, 695.74f)
                quadTo(621.39f, 773.0f, 480.0f, 773.0f)
                close()
                moveTo(480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                quadTo(480.0f, 460.0f, 480.0f, 460.0f)
                close()
                moveTo(479.88f, 700.0f)
                quadTo(600.65f, 700.0f, 701.76f, 634.5f)
                quadTo(802.87f, 569.0f, 855.87f, 460.0f)
                quadTo(802.87f, 351.0f, 701.88f, 285.5f)
                quadTo(600.88f, 220.0f, 480.12f, 220.0f)
                quadTo(359.35f, 220.0f, 258.24f, 285.5f)
                quadTo(157.13f, 351.0f, 103.13f, 460.0f)
                quadTo(157.13f, 569.0f, 258.12f, 634.5f)
                quadTo(359.12f, 700.0f, 479.88f, 700.0f)
                close()
            }
        }
        .build()
        return _visibility!!
    }

private var _visibility: ImageVector? = null
