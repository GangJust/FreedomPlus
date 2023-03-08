package com.freegang.xpler.utils.other

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.freegang.xpler.xp.KtXposedHelpers
import com.freegang.xpler.xp.getModuleRes

object KResourceUtils {
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ResourcesCompat.getDrawable(KtXposedHelpers.getModuleRes(), id, null)
    }

    fun <T: View> inflateView(context: Context, @LayoutRes id: Int): T {
        return LayoutInflater.from(context).inflate(KtXposedHelpers.getModuleRes().getLayout(id), null, false) as T
    }

    fun getDimension(@DimenRes id: Int): Float {
        return KtXposedHelpers.getModuleRes().getDimension(id)
    }

    fun getString(@StringRes id: Int): String {
        return KtXposedHelpers.getModuleRes().getString(id)
    }
}