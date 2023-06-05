package com.freegang.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable  //垂直滚动
fun VerScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
        content = {
            Box(
                content = { content() },
                modifier = Modifier.verticalScroll(verticalScroll)
            )
        }
    )
}

@Composable  //水平滚动
fun HorScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    val horizontalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
        content = {
            Box(
                content = { content() },
                modifier = Modifier
                    .horizontalScroll(horizontalScroll)
            )
        }
    )
}

@Composable  //滚动(包括垂直、水平)
fun ScrollableContainer(
    maxWidth: Dp = Dp.Infinity,
    maxHeight: Dp = Dp.Infinity,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable () -> Unit,
) {
    val verticalScroll = rememberScrollState(0)
    val horizontalScroll = rememberScrollState(0)
    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .heightIn(max = maxHeight)
            .padding(contentPadding),
        content = {
            Box(
                content = { content() },
                modifier = Modifier
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll),
            )
        }
    )
}