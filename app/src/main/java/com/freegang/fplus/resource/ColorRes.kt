package com.freegang.fplus.resource

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object ColorRes {
    val transparent = Color(0x00000000)

    fun light() = KColors(
        title = Color(0xFF333333),
        subtitle = Color(0XFF666666),
        body = Color(0XFF666666),
        caption = Color(0XFFA3A6B1),
        divider = Color(0xFFD9D9D9),
        icon = Color(0XFF666666),
        checkedThumb = Color(0xFFEDA664),
        checkedTrack = Color(0xFFF0BB88),
        uncheckedThumb = Color(0XFFFDFDFD),
        uncheckedTrack = Color(0xFF999999),
        colors = lightColors(
            primary = Color(0xFF03A9F4),
            primaryVariant = Color(0xFF03A9F4),
            secondary = Color(0xFFEDA664),
            secondaryVariant = Color(0xFFF0BB88),
            background = Color(0xFFF8FAFB),
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFB00020),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000),
            onError = Color(0xFFFFFFFF),
        )
    )

    fun dark() = KColors(
        title = Color(0xFFFFFFFF),
        subtitle = Color(0xFFAAAAAA),
        body = Color(0xFFFFFFFF),
        caption = Color(0xFFAAAAAA),
        divider = Color(0xFFDADADA),
        icon = Color(0xFFFFFFFF),
        checkedThumb = Color(0xFFFFFFFF),
        checkedTrack = Color(0XFFAAAAAA),
        uncheckedThumb = Color(0XFF666666),
        uncheckedTrack = Color(0XFF999999),
        colors = darkColors(
            primary = Color(0xFF03A9F4),
            primaryVariant = Color(0xFF03A9F4),
            secondary = Color(0xFFEDA664),
            secondaryVariant = Color(0xFFF0BB88),
            background = Color(0xFF3A3A3A),
            surface = Color(0xFF585858),
            error = Color(0xFFB00020),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000),
            onError = Color(0xFFFFFFFF),
        )
    )

    class KColors(
        val title: Color,
        val subtitle: Color,
        val body: Color,
        val caption: Color,
        val divider: Color,
        val icon: Color,
        val checkedThumb: Color,
        val checkedTrack: Color,
        val uncheckedThumb: Color,
        val uncheckedTrack: Color,
        val colors: Colors,
    )
}