package com.freegang.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

// see at: https://blog.csdn.net/ly_xiamu/article/details/125830327
// see at: https://www.saoniuhuo.com/question/detail-2624369.html
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeverOverScrollMode(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        content()
    }
}

object NoIndication : Indication {
    private object NoIndicationInstance : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
        }
    }

    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        return NoIndicationInstance
    }
}