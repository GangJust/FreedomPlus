package com.freegang.fplus

import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.freegang.fplus.resource.ColorRes
import com.freegang.fplus.resource.ShapeRes
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun FreedomTheme(
    window: Window,
    isImmersive: Boolean = true,
    isDark: Boolean = isSystemInDarkTheme(),
    followSystem: Boolean = true,
    content: @Composable () -> Unit,
) {
    //沉浸式则不设置系统视图
    if (isImmersive) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    //主题自适应
    Themes.ThemeContainer(
        isImmersive = isImmersive,
        isDark = isDark,
        followSystem = followSystem,
        content = content,
    )
}

object Themes {
    var isImmersive = false
    var isDark = false

    val nowColors: ColorRes.KColors
        get() {
            return if (isDark) {
                ColorRes.dark()
            } else {
                ColorRes.light()
            }
        }

    val nowTypography: Typography
        get() {
            return Typography(
                subtitle1 = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = nowColors.title,
                ),
                subtitle2 = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = nowColors.subtitle,
                ),
                body1 = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = nowColors.body,
                ),
                body2 = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = nowColors.body,
                ),
                button = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = nowColors.body,
                ),
                caption = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = nowColors.caption,
                ),
                overline = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = nowColors.body,
                ),
            )
        }

    // 主题容器
    @Composable
    fun ThemeContainer(
        isImmersive: Boolean = true,
        isDark: Boolean = false,
        followSystem: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        Themes.isImmersive = isImmersive
        Themes.isDark = if (followSystem) isSystemInDarkTheme() else isDark

        MaterialTheme(
            colors = nowColors.colors,
            typography = nowTypography,
            shapes = ShapeRes.defaultShapes,
            content = {
                //沉浸式状态栏
                rememberSystemUiController().setSystemBarsColor(
                    color = nowColors.colors.background,
                    darkIcons = !Themes.isDark,
                )
                //沉浸式则补充间隙
                Surface(
                    modifier = if (Themes.isImmersive) Modifier.windowInsetsPadding(WindowInsets.systemBars) else Modifier,
                    color = nowColors.colors.background,
                    contentColor = nowColors.colors.background,
                    content = { content() }
                )
            }
        )
    }
}