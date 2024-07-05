package io.github.fplus.resource

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.fplus.resource.icons.Acute
import io.github.fplus.resource.icons.DarkMode
import io.github.fplus.resource.icons.FindFile
import io.github.fplus.resource.icons.Freedom
import io.github.fplus.resource.icons.FreedomNight
import io.github.fplus.resource.icons.Github
import io.github.fplus.resource.icons.History
import io.github.fplus.resource.icons.LightMode
import io.github.fplus.resource.icons.Manage
import io.github.fplus.resource.icons.Motion
import io.github.fplus.resource.icons.Play
import io.github.fplus.resource.icons.SpicyStrips
import io.github.fplus.resource.icons.Telegram
import io.github.fplus.resource.icons.Visibility
import io.github.fplus.resource.icons.VisibilityOff
import kotlin.collections.List as _KtList

private var all: _KtList<ImageVector>? = null

object IconRes

val IconRes.All: _KtList<ImageVector>
    get() {
        return all ?: listOf(
            Acute,
            DarkMode,
            FindFile,
            Freedom,
            FreedomNight,
            Github,
            History,
            LightMode,
            Manage,
            Motion,
            Play,
            SpicyStrips,
            Telegram,
            Visibility,
            VisibilityOff,
        ).also {
            all = it
        }
    }
