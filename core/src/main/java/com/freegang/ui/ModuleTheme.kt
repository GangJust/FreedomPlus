package com.freegang.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ModuleTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    followSystem: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            h1 = TextStyle(fontSize = 57.sp),
            h2 = TextStyle(fontSize = 45.sp),
            h3 = TextStyle(fontSize = 36.sp),
            h4 = TextStyle(fontSize = 32.sp),
            h5 = TextStyle(fontSize = 28.sp),
            h6 = TextStyle(fontSize = 24.sp),
            subtitle1 = TextStyle(fontSize = 22.sp),
            subtitle2 = TextStyle(fontSize = 16.sp),
            body1 = TextStyle(fontSize = 16.sp),
            body2 = TextStyle(fontSize = 14.sp),
            button = TextStyle(fontSize = 14.sp),
            caption = TextStyle(fontSize = 12.sp),
            overline = TextStyle(fontSize = 11.sp),
        ),
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFFE89F5B),
            primaryVariant = Color(0xFFF2C18C),
            secondary = Color(0xFFE89F5B),
            secondaryVariant = Color(0xFFCA9D7C),
            background = Color(0xFFFDFAF8),
            surface = Color(0xFFFDFAF8),
            error = Color(0xFF790000),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF090909),
            onSurface = Color(0xFF090909),
            onError = Color(0xFFFFFFFF),
            isLight = true,
        ),
    ) {
        rememberSystemUiController().run {
            setSystemBarsColor(
                color = Color(0xFFF8FAFB),
                darkIcons = if (followSystem) !isSystemInDarkTheme() else !isDark,
            )
            Surface(
                modifier = Modifier,
                color = Color(0xFFF8FAFB),
                contentColor = Color(0xFFF8FAFB),
            ) {
                content()
            }
        }
    }
}

val Dp.asSP: TextUnit get() = this.value.sp

val TextUnit.asDp: Dp get() = this.value.dp