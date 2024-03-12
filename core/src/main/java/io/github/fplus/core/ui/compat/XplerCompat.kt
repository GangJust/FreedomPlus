package io.github.fplus.core.ui.compat

import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.freegang.ktutils.log.KLogCat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@Composable
fun painterResourceCompat(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val res = context.resources
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string

    KLogCat.i("Resources: $res", "Path: $path")

    if (path?.endsWith(".xml") == true) {
        return painterResource(id = id)
    }
    val imageBitmap = remember(path, id, context.theme) {
        try {
            ImageBitmap.imageResource(res, id)
        } catch (throwable: Throwable) {
            val drawable: Drawable =
                ContextCompat.getDrawable(context, id) ?: throw IllegalArgumentException("not found drawable, path: $path")
            drawable.toBitmap().asImageBitmap()
        }
    }
    return BitmapPainter(imageBitmap)
}

/**
 * Helper method to seek to the first tag within the VectorDrawable xml asset
 */
@Throws(XmlPullParserException::class)
private fun XmlPullParser.seekToStartTag(): XmlPullParser {
    var type = next()
    while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
        // Empty loop
        type = next()
        KLogCat.i(this.name)
    }
    if (type != XmlPullParser.START_TAG) {
        throw XmlPullParserException("No start tag found")
    }
    return this
}