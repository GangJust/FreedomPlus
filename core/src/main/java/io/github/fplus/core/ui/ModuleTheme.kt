package io.github.fplus.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Typography
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
    val typography = autoTypography(isDark = if (followSystem) isSystemInDarkTheme() else isDark)
    val colors = autoColors(isDark = if (followSystem) isSystemInDarkTheme() else isDark)
    val darkIcons = !((if (followSystem) isSystemInDarkTheme() else isDark))

    MaterialTheme(
        typography = typography,
        colors = colors,
    ) {
        rememberSystemUiController().run {
            setSystemBarsColor(
                color = MaterialTheme.colors.background,
                darkIcons = darkIcons,
            )
        }

        Surface(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            color = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.background,
            content = content,
        )
    }
}

@Composable
private fun autoTypography(isDark: Boolean): Typography {
    return if (isDark) {
        MaterialTheme.typography.copy(
            h1 = TextStyle(fontSize = 57.sp, color = Color(0xFFFFFFFF)),
            h2 = TextStyle(fontSize = 45.sp, color = Color(0xFFFFFFFF)),
            h3 = TextStyle(fontSize = 36.sp, color = Color(0xFFFFFFFF)),
            h4 = TextStyle(fontSize = 32.sp, color = Color(0xFFFFFFFF)),
            h5 = TextStyle(fontSize = 28.sp, color = Color(0xFFFFFFFF)),
            h6 = TextStyle(fontSize = 24.sp, color = Color(0xFFFFFFFF)),
            subtitle1 = TextStyle(fontSize = 22.sp, color = Color(0xFFFFFFFF)),
            subtitle2 = TextStyle(fontSize = 16.sp, color = Color(0xFFE3E2E6)),
            body1 = TextStyle(fontSize = 14.sp, color = Color(0xFFFFFFFF)),
            body2 = TextStyle(fontSize = 12.sp, color = Color(0xFFE3E2E6)),
            button = TextStyle(fontSize = 14.sp, color = Color(0xFFFFFFFF)),
            caption = TextStyle(fontSize = 12.sp, color = Color(0xFFFFFFFF)),
            overline = TextStyle(fontSize = 11.sp, color = Color(0xFFFFFFFF)),
        )
    } else {
        MaterialTheme.typography.copy(
            h1 = TextStyle(fontSize = 57.sp, color = Color(0xFF333333)),
            h2 = TextStyle(fontSize = 45.sp, color = Color(0xFF333333)),
            h3 = TextStyle(fontSize = 36.sp, color = Color(0xFF333333)),
            h4 = TextStyle(fontSize = 32.sp, color = Color(0xFF333333)),
            h5 = TextStyle(fontSize = 28.sp, color = Color(0xFF333333)),
            h6 = TextStyle(fontSize = 24.sp, color = Color(0xFF333333)),
            subtitle1 = TextStyle(fontSize = 22.sp, color = Color(0xFF333333)),
            subtitle2 = TextStyle(fontSize = 16.sp, color = Color(0xFF333333)),
            body1 = TextStyle(fontSize = 14.sp, color = Color(0xFF333333)),
            body2 = TextStyle(fontSize = 12.sp, color = Color(0xFF333333)),
            button = TextStyle(fontSize = 14.sp, color = Color(0xFF333333)),
            caption = TextStyle(fontSize = 12.sp, color = Color(0xFF333333)),
            overline = TextStyle(fontSize = 11.sp, color = Color(0xFF333333)),
        )
    }
}

@Composable
private fun autoColors(isDark: Boolean): Colors {
    return if (isDark) {
        MaterialTheme.colors.copy(
            primary = Color(0xFFAFC6FF),
            primaryVariant = Color(0xFF17448F),
            secondary = Color(0xFFBFC6DC),
            secondaryVariant = Color(0xFF404659),
            background = Color(0xFF2C2F39),
            surface = Color(0xFF2C2F39),
            error = Color(0xFFFFB4AB),
            onPrimary = Color(0xFF002D6D),
            onSecondary = Color(0xFF293042),
            onBackground = Color(0xFFE3E2E6),
            onSurface = Color(0xFFE3E2E6),
            onError = Color(0xFF690005),
            isLight = false,
        )
    } else {
        MaterialTheme.colors.copy(
            primary = Color(0xFFE89F5B),
            primaryVariant = Color(0xFFF2C18C),
            secondary = Color(0xFFE89F5B),
            secondaryVariant = Color(0xFFCA9D7C),
            background = Color(0xFFF8FAFB),
            surface = Color(0xFFF8FAFB),
            error = Color(0xFF790000),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF090909),
            onSurface = Color(0xFF090909),
            onError = Color(0xFFFFFFFF),
            isLight = true,
        )
    }
}

val Dp.asSP: TextUnit get() = this.value.sp

val TextUnit.asDp: Dp get() = this.value.dp