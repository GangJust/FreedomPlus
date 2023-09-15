package com.freegang.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Compose中实现的文本高亮样式辅助类
 *
 * 实现逻辑以及使用方法: https://juejin.cn/post/7271103456857210939
 */
data class HighlightStyle(
    var text: String,
    var range: IntRange,
    var color: Color,
) {
    fun isEmpty(): Boolean = this == empty

    companion object {
        val empty: HighlightStyle = HighlightStyle(
            "",
            IntRange.EMPTY,
            Color.Transparent,
        )
    }
}

class HighlightStyleBuilder(val text: String) {
    private val highlightStyles = mutableListOf<HighlightStyle>()

    fun append(
        regex: Regex,
        color: Color = Color.Black,
        block: ((MatchResult) -> HighlightStyle)? = null,
    ): HighlightStyleBuilder {
        for (result in regex.findAll(text)) {
            highlightStyles.add(
                block?.invoke(result) ?: HighlightStyle(
                    text = result.value,
                    range = result.range,
                    color = color
                )
            )
        }
        return this
    }

    fun build(): AnnotatedString {
        val styles = highlightStyles.sortedBy { it.range.first }
        return buildAnnotatedString {
            var startIndex = 0
            for (style in styles) {
                if (style.isEmpty()) continue
                if (style.range.first < startIndex) continue
                append(text.substring(startIndex, style.range.first))
                withStyle(SpanStyle(color = style.color)) { append(style.text) }
                startIndex = style.range.last + 1
            }
            if (startIndex < text.length) {
                append(text.substring(startIndex))
            }
        }
    }
}

inline fun buildHighlightStyle(
    text: String, block: HighlightStyleBuilder.() -> Unit,
): AnnotatedString {
    val builder = HighlightStyleBuilder(text)
    block.invoke(builder)
    return builder.build()
}