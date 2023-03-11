package com.freegang.xpler.utils.other

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import com.freegang.xpler.xp.KtXposedHelpers
import com.freegang.xpler.xp.getModuleRes

/// Xposed Resource
/// need in: Github/Xpler
object KResourceUtils {

    /**
     * 加载模块中的xml布局文件
     *
     * 需要注意的是, 模块中的xml不能直接引入模块自身的资源文件,
     * 如: @color/module_blank, @drawable/ic_logo 等
     *
     * 如需加载资源文件见[getDrawable]、[getColor]、[getDrawable]、[getAnimation]
     *
     * @param context context
     * @param id id
     * @param inflateView
     */
    fun <T : View> inflateView(context: Context, @LayoutRes id: Int): T {
        return LayoutInflater.from(context).inflate(getLayout(id), null, false) as T
    }

    /**
     * 获取模块中的 layout, 该方法不会加载layout
     *
     * @param id id
     * @return Layout XmlResourceParser
     */
    @Throws
    fun getLayout(@LayoutRes id: Int): XmlResourceParser {
        return KtXposedHelpers.getModuleRes().getLayout(id)
    }

    /**
     * 获取模块中的 drawable
     *
     * @param id id
     * @return Drawable
     */
    @Throws
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ResourcesCompat.getDrawable(KtXposedHelpers.getModuleRes(), id, null)
    }

    /**
     * 获取模块中的 color
     *
     * @param id id
     * @return color int
     */
    @Throws
    fun getColor(@ColorRes id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KtXposedHelpers.getModuleRes().getColor(id, null)
        } else {
            KtXposedHelpers.getModuleRes().getColor(id)
        }
    }

    /**
     * 获取模块中的 Float
     *
     * @param id id
     * @return Float
     */
    @Throws
    fun getDimension(@DimenRes id: Int): Float {
        return KtXposedHelpers.getModuleRes().getDimension(id)
    }

    /**
     * 获取模块中的 Animation
     *
     * @param id id
     * @return Animation XmlResourceParser
     */
    @Throws
    fun getAnimation(@AnimatorRes @AnimRes id: Int): XmlResourceParser {
        return KtXposedHelpers.getModuleRes().getAnimation(id)
    }

    /**
     * 获取模块中的 String
     *
     * @param id id
     * @return String
     */
    @Throws
    fun getString(@StringRes id: Int): String {
        return KtXposedHelpers.getModuleRes().getString(id)
    }
}