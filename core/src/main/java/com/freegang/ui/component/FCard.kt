package com.freegang.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


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
        backgroundColor = Color.Transparent,
        shape = border.borderShape,
        content = content
    )
}

data class FCardBorder(
    var borderWidth: Dp = 1.5.dp,
    var borderColor: Color = Color(0xFFD9D9D9),
    var borderShape: Shape = RoundedCornerShape(12.dp),
)