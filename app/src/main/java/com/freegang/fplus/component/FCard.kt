package com.freegang.fplus.component

import androidx.compose.foundation.border
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freegang.fplus.Themes
import com.freegang.fplus.resource.ColorRes
import com.freegang.fplus.resource.ShapeRes


@Composable
fun FCard(
    modifier: Modifier = Modifier,
    border: FCardBorder = FCardBorder(),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.border(
            width = border.borderWidth,
            color = border.borderColor,
            shape = border.borderShape,
        ),
        elevation = 0.dp,
        backgroundColor = ColorRes.transparent,
        shape = ShapeRes.defaultShapes.large,
        content = content
    )
}

data class FCardBorder(
    var borderWidth: Dp = 1.5.dp,
    var borderColor: Color = Themes.nowColors.divider,
    var borderShape: Shape = ShapeRes.defaultShapes.large,
)