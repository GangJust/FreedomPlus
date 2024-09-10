package io.github.fplus.plugin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.content.res.loader.ResourcesLoader
import android.graphics.Movie
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import com.freegang.extension.findMethodInvoke
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XposedHelpers
import io.github.xpler.core.XplerHelper
import io.github.xpler.core.XplerModule
import java.io.InputStream

class PluginResources(
    private val originResources: Resources,
) : Resources(
    originResources.assets,
    originResources.displayMetrics,
    originResources.configuration,
) {

    private val pluginResources by lazy {
        if (XplerModule.modulePath.isEmpty()) {
            KLogCat.i("未获取到模块路径!")
            originResources
        } else {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.findMethodInvoke<Any>(XplerModule.modulePath) { name("addAssetPath") }
            Resources(assetManager, originResources.displayMetrics, originResources.configuration)
        }
    }

    val pluginAssets: AssetManager get() = pluginResources.assets

    override fun getText(id: Int): CharSequence {
        /*return try {
            originResources.getText(id)
        } catch (e: Exception) {
            try {
                pluginResources.getText(id)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getText(id)
        } catch (e: Exception) {
            try {
                originResources.getText(id)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence {
        /*return try {
            originResources.getText(id, def)
        } catch (e: Exception) {
            try {
                pluginResources.getText(id, def)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getText(id, def)
        } catch (e: Exception) {
            try {
                originResources.getText(id, def)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    @SuppressLint("NewApi")
    override fun getFont(id: Int): Typeface {
        /*return try {
            originResources.getFont(id)
        } catch (e: Exception) {
            pluginResources.getFont(id)
        }*/

        return try {
            pluginResources.getFont(id)
        } catch (e: Exception) {
            originResources.getFont(id)
        }
    }

    override fun getQuantityText(id: Int, quantity: Int): CharSequence {
        /*return try {
            originResources.getQuantityText(id, quantity)
        } catch (e: Exception) {
            try {
                pluginResources.getQuantityText(id, quantity)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getQuantityText(id, quantity)
        } catch (e: Exception) {
            try {
                originResources.getQuantityText(id, quantity)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getString(id: Int): String {
        /*return try {
            originResources.getString(id)
        } catch (e: Exception) {
            try {
                pluginResources.getString(id)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getString(id)
        } catch (e: Exception) {
            try {
                originResources.getString(id)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        /*return try {
            originResources.getString(id, *formatArgs)
        } catch (e: Exception) {
            try {
                pluginResources.getString(id, *formatArgs)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getString(id, *formatArgs)
        } catch (e: Exception) {
            try {
                originResources.getString(id, *formatArgs)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String {
        /*return try {
            originResources.getQuantityString(id, quantity, *formatArgs)
        } catch (e: Exception) {
            try {
                pluginResources.getQuantityString(id, quantity, *formatArgs)
            } catch (e: Exception) {
                "Unknown"
            }
        }*/

        return try {
            pluginResources.getQuantityString(id, quantity, *formatArgs)
        } catch (e: Exception) {
            try {
                originResources.getQuantityString(id, quantity, *formatArgs)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getQuantityString(id: Int, quantity: Int): String {
        return try {
            pluginResources.getQuantityString(id, quantity)
        } catch (e: Exception) {
            try {
                originResources.getQuantityString(id, quantity)
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun getTextArray(id: Int): Array<CharSequence> {
        /*return try {
            originResources.getTextArray(id)
        } catch (e: Exception) {
            pluginResources.getTextArray(id)
        }*/

        return try {
            pluginResources.getTextArray(id)
        } catch (e: Exception) {
            originResources.getTextArray(id)
        }
    }

    override fun getStringArray(id: Int): Array<String> {
        /*return try {
            originResources.getStringArray(id)
        } catch (e: Exception) {
            pluginResources.getStringArray(id)
        }*/

        return try {
            pluginResources.getStringArray(id)
        } catch (e: Exception) {
            originResources.getStringArray(id)
        }
    }

    override fun getIntArray(id: Int): IntArray {
        /*return try {
            originResources.getIntArray(id)
        } catch (e: Exception) {
            pluginResources.getIntArray(id)
        }*/

        return try {
            pluginResources.getIntArray(id)
        } catch (e: Exception) {
            originResources.getIntArray(id)
        }
    }

    override fun obtainTypedArray(id: Int): TypedArray {
        /*return try {
            originResources.obtainTypedArray(id)
        } catch (e: Exception) {
            pluginResources.obtainTypedArray(id)
        }*/

        return try {
            pluginResources.obtainTypedArray(id)
        } catch (e: Exception) {
            originResources.obtainTypedArray(id)
        }
    }

    override fun getDimension(id: Int): Float {
        /*return try {
            originResources.getDimension(id)
        } catch (e: Exception) {
            pluginResources.getDimension(id)
        }*/

        return try {
            pluginResources.getDimension(id)
        } catch (e: Exception) {
            originResources.getDimension(id)
        }
    }

    override fun getDimensionPixelOffset(id: Int): Int {
        /*return try {
            originResources.getDimensionPixelOffset(id)
        } catch (e: Exception) {
            pluginResources.getDimensionPixelOffset(id)
        }*/

        return try {
            pluginResources.getDimensionPixelOffset(id)
        } catch (e: Exception) {
            originResources.getDimensionPixelOffset(id)
        }
    }

    override fun getDimensionPixelSize(id: Int): Int {
        /*return try {
            originResources.getDimensionPixelSize(id)
        } catch (e: Exception) {
            pluginResources.getDimensionPixelSize(id)
        }*/

        return try {
            pluginResources.getDimensionPixelSize(id)
        } catch (e: Exception) {
            originResources.getDimensionPixelSize(id)
        }
    }

    override fun getFraction(id: Int, base: Int, pbase: Int): Float {
        /* return try {
             originResources.getFraction(id, base, pbase)
         } catch (e: Exception) {
             pluginResources.getFraction(id, base, pbase)
         }*/

        return try {
            pluginResources.getFraction(id, base, pbase)
        } catch (e: Exception) {
            originResources.getFraction(id, base, pbase)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun getDrawable(id: Int): Drawable {
        /*return try {
            originResources.getDrawable(id)
        } catch (e: Exception) {
            pluginResources.getDrawable(id)
        }*/

        return try {
            pluginResources.getDrawable(id)
        } catch (e: Exception) {
            originResources.getDrawable(id)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun getDrawable(id: Int, theme: Theme?): Drawable {
        /*return try {
            originResources.getDrawable(id, theme)
        } catch (e: Exception) {
            pluginResources.getDrawable(id, theme)
        }*/

        return try {
            pluginResources.getDrawable(id, theme)
        } catch (e: Exception) {
            originResources.getDrawable(id, theme)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        /*return try {
            originResources.getDrawableForDensity(id, density)
        } catch (e: Exception) {
            pluginResources.getDrawableForDensity(id, density)
        }*/

        return try {
            pluginResources.getDrawableForDensity(id, density)
        } catch (e: Exception) {
            originResources.getDrawableForDensity(id, density)
        }
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        /*return try {
            originResources.getDrawableForDensity(id, density, theme)
        } catch (e: Exception) {
            pluginResources.getDrawableForDensity(id, density, theme)
        }*/

        return try {
            pluginResources.getDrawableForDensity(id, density, theme)
        } catch (e: Exception) {
            originResources.getDrawableForDensity(id, density, theme)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getMovie(id: Int): Movie {
        /*return try {
            originResources.getMovie(id)
        } catch (e: Exception) {
            pluginResources.getMovie(id)
        }*/

        return try {
            pluginResources.getMovie(id)
        } catch (e: Exception) {
            originResources.getMovie(id)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getColor(id: Int): Int {
        /*return try {
            originResources.getColor(id)
        } catch (e: Exception) {
            pluginResources.getColor(id)
        }*/

        return try {
            pluginResources.getColor(id)
        } catch (e: Exception) {
            originResources.getColor(id)
        }
    }

    override fun getColor(id: Int, theme: Theme?): Int {
        /*return try {
            originResources.getColor(id, theme)
        } catch (e: Exception) {
            pluginResources.getColor(id, theme)
        }*/

        return try {
            pluginResources.getColor(id, theme)
        } catch (e: Exception) {
            originResources.getColor(id, theme)
        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Deprecated("Deprecated in Java")
    override fun getColorStateList(id: Int): ColorStateList {
        /*return try {
            originResources.getColorStateList(id)
        } catch (e: Exception) {
            pluginResources.getColorStateList(id)
        }*/

        return try {
            pluginResources.getColorStateList(id)
        } catch (e: Exception) {
            originResources.getColorStateList(id)
        }
    }

    override fun getColorStateList(id: Int, theme: Theme?): ColorStateList {
        /*return try {
            originResources.getColorStateList(id, theme)
        } catch (e: Exception) {
            pluginResources.getColorStateList(id, theme)
        }*/

        return try {
            pluginResources.getColorStateList(id, theme)
        } catch (e: Exception) {
            originResources.getColorStateList(id, theme)
        }
    }

    override fun getBoolean(id: Int): Boolean {
        /*return try {
            originResources.getBoolean(id)
        } catch (e: Exception) {
            pluginResources.getBoolean(id)
        }*/

        return try {
            pluginResources.getBoolean(id)
        } catch (e: Exception) {
            originResources.getBoolean(id)
        }
    }

    override fun getInteger(id: Int): Int {
        /*return try {
            originResources.getInteger(id)
        } catch (e: Exception) {
            pluginResources.getInteger(id)
        }*/

        return try {
            pluginResources.getInteger(id)
        } catch (e: Exception) {
            originResources.getInteger(id)
        }
    }

    @SuppressLint("NewApi")
    override fun getFloat(id: Int): Float {
        /*return try {
            originResources.getFloat(id)
        } catch (e: Exception) {
            pluginResources.getFloat(id)
        }*/

        return try {
            pluginResources.getFloat(id)
        } catch (e: Exception) {
            originResources.getFloat(id)
        }
    }

    override fun getLayout(id: Int): XmlResourceParser {
        /*return try {
            originResources.getLayout(id)
        } catch (e: Exception) {
            pluginResources.getLayout(id)
        }*/

        return try {
            pluginResources.getLayout(id)
        } catch (e: Exception) {
            originResources.getLayout(id)
        }
    }

    override fun getAnimation(id: Int): XmlResourceParser {
        /*return try {
            originResources.getAnimation(id)
        } catch (e: Exception) {
            pluginResources.getAnimation(id)
        }*/

        return try {
            pluginResources.getAnimation(id)
        } catch (e: Exception) {
            originResources.getAnimation(id)
        }
    }

    override fun getXml(id: Int): XmlResourceParser {
        /*return try {
            originResources.getXml(id)
        } catch (e: Exception) {
            pluginResources.getXml(id)
        }*/

        return try {
            pluginResources.getXml(id)
        } catch (e: Exception) {
            originResources.getXml(id)
        }
    }

    override fun openRawResource(id: Int): InputStream {
        /*return try {
            originResources.openRawResource(id)
        } catch (e: Exception) {
            pluginResources.openRawResource(id)
        }*/

        return try {
            pluginResources.openRawResource(id)
        } catch (e: Exception) {
            originResources.openRawResource(id)
        }
    }

    override fun openRawResource(id: Int, value: TypedValue?): InputStream {
        /*return try {
            originResources.openRawResource(id, value)
        } catch (e: Exception) {
            pluginResources.openRawResource(id, value)
        }*/

        return try {
            pluginResources.openRawResource(id, value)
        } catch (e: Exception) {
            originResources.openRawResource(id, value)
        }
    }

    override fun openRawResourceFd(id: Int): AssetFileDescriptor {
        /*return try {
            originResources.openRawResourceFd(id)
        } catch (e: Exception) {
            pluginResources.openRawResourceFd(id)
        }*/

        return try {
            pluginResources.openRawResourceFd(id)
        } catch (e: Exception) {
            originResources.openRawResourceFd(id)
        }
    }

    override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        /*try {
            originResources.getValue(id, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValue(id, outValue, resolveRefs)
        }*/

        try {
            pluginResources.getValue(id, outValue, resolveRefs)
        } catch (e: Exception) {
            originResources.getValue(id, outValue, resolveRefs)
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) {
        /*try {
            originResources.getValue(name, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValue(name, outValue, resolveRefs)
        }*/

        try {
            pluginResources.getValue(name, outValue, resolveRefs)
        } catch (e: Exception) {
            originResources.getValue(name, outValue, resolveRefs)
        }
    }

    override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        /*try {
            originResources.getValueForDensity(id, density, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValueForDensity(id, density, outValue, resolveRefs)
        }*/

        try {
            pluginResources.getValueForDensity(id, density, outValue, resolveRefs)
        } catch (e: Exception) {
            originResources.getValueForDensity(id, density, outValue, resolveRefs)
        }
    }

    override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray {
        /*return try {
            originResources.obtainAttributes(set, attrs)
        } catch (e: Exception) {
            pluginResources.obtainAttributes(set, attrs)
        }*/

        return try {
            pluginResources.obtainAttributes(set, attrs)
        } catch (e: Exception) {
            originResources.obtainAttributes(set, attrs)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) {
        /*try {
            originResources.updateConfiguration(config, metrics)
        } catch (e: Exception) {
            pluginResources.updateConfiguration(config, metrics)
        }*/

        try {
            pluginResources.updateConfiguration(config, metrics)
        } catch (e: Exception) {
            originResources.updateConfiguration(config, metrics)
        }
    }

    override fun getDisplayMetrics(): DisplayMetrics {
        /*return try {
            originResources.displayMetrics
        } catch (e: Exception) {
            pluginResources.displayMetrics
        }*/

        return try {
            pluginResources.displayMetrics
        } catch (e: Exception) {
            originResources.displayMetrics
        }
    }

    override fun getConfiguration(): Configuration {
        /*return try {
            originResources.configuration
        } catch (e: Exception) {
            pluginResources.configuration
        }*/

        return try {
            pluginResources.configuration
        } catch (e: Exception) {
            originResources.configuration
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int {
        /*return try {
            originResources.getIdentifier(name, defType, defPackage)
        } catch (e: Exception) {
            pluginResources.getIdentifier(name, defType, defPackage)
        }*/

        return try {
            pluginResources.getIdentifier(name, defType, defPackage)
        } catch (e: Exception) {
            originResources.getIdentifier(name, defType, defPackage)
        }
    }

    override fun getResourceName(resid: Int): String {
        /*return try {
            originResources.getResourceName(resid)
        } catch (e: Exception) {
            pluginResources.getResourceName(resid)
        }*/

        return try {
            pluginResources.getResourceName(resid)
        } catch (e: Exception) {
            originResources.getResourceName(resid)
        }
    }

    override fun getResourcePackageName(resid: Int): String {
        /*return try {
            originResources.getResourcePackageName(resid)
        } catch (e: Exception) {
            pluginResources.getResourcePackageName(resid)
        }*/

        return try {
            pluginResources.getResourcePackageName(resid)
        } catch (e: Exception) {
            originResources.getResourcePackageName(resid)
        }
    }

    override fun getResourceTypeName(resid: Int): String {
        /* return try {
             originResources.getResourceTypeName(resid)
         } catch (e: Exception) {
             pluginResources.getResourceTypeName(resid)
         }*/

        return try {
            pluginResources.getResourceTypeName(resid)
        } catch (e: Exception) {
            originResources.getResourceTypeName(resid)
        }
    }

    override fun getResourceEntryName(resid: Int): String {
        /*return try {
            originResources.getResourceEntryName(resid)
        } catch (e: Exception) {
            pluginResources.getResourceEntryName(resid)
        }*/

        return try {
            pluginResources.getResourceEntryName(resid)
        } catch (e: Exception) {
            originResources.getResourceEntryName(resid)
        }
    }

    override fun parseBundleExtras(parser: XmlResourceParser?, outBundle: Bundle?) {
        /*try {
            originResources.parseBundleExtras(parser, outBundle)
        } catch (e: Exception) {
            pluginResources.parseBundleExtras(parser, outBundle)
        }*/

        try {
            pluginResources.parseBundleExtras(parser, outBundle)
        } catch (e: Exception) {
            originResources.parseBundleExtras(parser, outBundle)
        }
    }

    override fun parseBundleExtra(tagName: String?, attrs: AttributeSet?, outBundle: Bundle?) {
        /* try {
             originResources.parseBundleExtra(tagName, attrs, outBundle)
         } catch (e: Exception) {
             pluginResources.parseBundleExtra(tagName, attrs, outBundle)
         }*/

        try {
            pluginResources.parseBundleExtra(tagName, attrs, outBundle)
        } catch (e: Exception) {
            originResources.parseBundleExtra(tagName, attrs, outBundle)
        }
    }

    @SuppressLint("NewApi")
    override fun addLoaders(vararg loaders: ResourcesLoader?) {
        /*try {
            originResources.addLoaders(*loaders)
        } catch (e: Exception) {
            pluginResources.addLoaders(*loaders)
        }*/

        try {
            pluginResources.addLoaders(*loaders)
        } catch (e: Exception) {
            originResources.addLoaders(*loaders)
        }
    }

    @SuppressLint("NewApi")
    override fun removeLoaders(vararg loaders: ResourcesLoader?) {
        /*try {
            originResources.removeLoaders(*loaders)
        } catch (e: Exception) {
            pluginResources.removeLoaders(*loaders)
        }*/

        try {
            pluginResources.removeLoaders(*loaders)
        } catch (e: Exception) {
            originResources.removeLoaders(*loaders)
        }
    }
}

// 代理Resources
fun proxyRes(activity: Activity?) {
    activity?.runCatching {
        XplerHelper.setFieldValue(this, "mResources", PluginResources(activity.resources))
    }
}

// 合并Resources
fun injectRes(res: Resources?) {
    res?.runCatching {
        XplerHelper.invokeMethod(res.assets, "addAssetPath", XplerModule.modulePath)
    }
}