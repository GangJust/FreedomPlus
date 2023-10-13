package com.freegang.plugin

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
import com.freegang.ktutils.reflect.fieldSetFirst
import com.freegang.ktutils.reflect.methodInvokes
import com.freegang.xpler.core.KtXposedHelpers
import java.io.InputStream

class PluginResource(
    resources: Resources,
) : Resources(
    resources.assets,
    resources.displayMetrics,
    resources.configuration,
) {

    private val pluginResources by lazy {
        try {
            if (KtXposedHelpers.modulePath.isEmpty()) return@lazy this
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.methodInvokes("addAssetPath", args = arrayOf(KtXposedHelpers.modulePath))
            Resources(assetManager, resources.displayMetrics, resources.configuration)
        } catch (e: Exception) {
            this
        }
    }

    val pluginAssets: AssetManager get() = pluginResources.assets

    override fun getText(id: Int): CharSequence {
        return try {
            super.getText(id)
        } catch (e: Exception) {
            pluginResources.getText(id)
        }
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence {
        return try {
            super.getText(id, def)
        } catch (e: Exception) {
            pluginResources.getText(id, def)
        }
    }

    @SuppressLint("NewApi")
    override fun getFont(id: Int): Typeface {
        return try {
            super.getFont(id)
        } catch (e: Exception) {
            pluginResources.getFont(id)
        }
    }

    override fun getQuantityText(id: Int, quantity: Int): CharSequence {
        return try {
            super.getQuantityText(id, quantity)
        } catch (e: Exception) {
            pluginResources.getQuantityText(id, quantity)
        }
    }

    override fun getString(id: Int): String {
        return try {
            super.getString(id)
        } catch (e: Exception) {
            pluginResources.getString(id)
        }
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        return try {
            super.getString(id, *formatArgs)
        } catch (e: Exception) {
            pluginResources.getString(id, *formatArgs)
        }
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String {
        return try {
            super.getQuantityString(id, quantity, *formatArgs)
        } catch (e: Exception) {
            pluginResources.getQuantityString(id, quantity, *formatArgs)
        }
    }

    override fun getQuantityString(id: Int, quantity: Int): String {
        return try {
            super.getQuantityString(id, quantity)
        } catch (e: Exception) {
            pluginResources.getQuantityString(id, quantity)
        }
    }

    override fun getTextArray(id: Int): Array<CharSequence> {
        return try {
            super.getTextArray(id)
        } catch (e: Exception) {
            pluginResources.getTextArray(id)
        }
    }

    override fun getStringArray(id: Int): Array<String> {
        return try {
            super.getStringArray(id)
        } catch (e: Exception) {
            pluginResources.getStringArray(id)
        }
    }

    override fun getIntArray(id: Int): IntArray {
        return try {
            super.getIntArray(id)
        } catch (e: Exception) {
            pluginResources.getIntArray(id)
        }
    }

    override fun obtainTypedArray(id: Int): TypedArray {
        return try {
            super.obtainTypedArray(id)
        } catch (e: Exception) {
            pluginResources.obtainTypedArray(id)
        }
    }

    override fun getDimension(id: Int): Float {
        return try {
            super.getDimension(id)
        } catch (e: Exception) {
            pluginResources.getDimension(id)
        }
    }

    override fun getDimensionPixelOffset(id: Int): Int {
        return try {
            super.getDimensionPixelOffset(id)
        } catch (e: Exception) {
            pluginResources.getDimensionPixelOffset(id)
        }
    }

    override fun getDimensionPixelSize(id: Int): Int {
        return try {
            super.getDimensionPixelSize(id)
        } catch (e: Exception) {
            pluginResources.getDimensionPixelSize(id)
        }
    }

    override fun getFraction(id: Int, base: Int, pbase: Int): Float {
        return try {
            super.getFraction(id, base, pbase)
        } catch (e: Exception) {
            pluginResources.getFraction(id, base, pbase)
        }
    }

    override fun getDrawable(id: Int): Drawable {
        return try {
            super.getDrawable(id)
        } catch (e: Exception) {
            pluginResources.getDrawable(id)
        }
    }

    override fun getDrawable(id: Int, theme: Theme?): Drawable {
        return try {
            super.getDrawable(id, theme)
        } catch (e: Exception) {
            pluginResources.getDrawable(id, theme)
        }
    }

    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        return try {
            super.getDrawableForDensity(id, density)
        } catch (e: Exception) {
            pluginResources.getDrawableForDensity(id, density)
        }
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        return try {
            super.getDrawableForDensity(id, density, theme)
        } catch (e: Exception) {
            pluginResources.getDrawableForDensity(id, density, theme)
        }
    }

    override fun getMovie(id: Int): Movie {
        return try {
            super.getMovie(id)
        } catch (e: Exception) {
            pluginResources.getMovie(id)
        }
    }

    override fun getColor(id: Int): Int {
        return try {
            super.getColor(id)
        } catch (e: Exception) {
            pluginResources.getColor(id)
        }
    }

    override fun getColor(id: Int, theme: Theme?): Int {
        return try {
            super.getColor(id, theme)
        } catch (e: Exception) {
            pluginResources.getColor(id, theme)
        }
    }

    override fun getColorStateList(id: Int): ColorStateList {
        return try {
            super.getColorStateList(id)
        } catch (e: Exception) {
            pluginResources.getColorStateList(id)
        }
    }

    override fun getColorStateList(id: Int, theme: Theme?): ColorStateList {
        return try {
            super.getColorStateList(id, theme)
        } catch (e: Exception) {
            pluginResources.getColorStateList(id, theme)
        }
    }

    override fun getBoolean(id: Int): Boolean {
        return try {
            super.getBoolean(id)
        } catch (e: Exception) {
            pluginResources.getBoolean(id)
        }
    }

    override fun getInteger(id: Int): Int {
        return try {
            super.getInteger(id)
        } catch (e: Exception) {
            pluginResources.getInteger(id)
        }
    }

    @SuppressLint("NewApi")
    override fun getFloat(id: Int): Float {
        return try {
            super.getFloat(id)
        } catch (e: Exception) {
            pluginResources.getFloat(id)
        }
    }

    override fun getLayout(id: Int): XmlResourceParser {
        return try {
            super.getLayout(id)
        } catch (e: Exception) {
            pluginResources.getLayout(id)
        }
    }

    override fun getAnimation(id: Int): XmlResourceParser {
        return try {
            super.getAnimation(id)
        } catch (e: Exception) {
            pluginResources.getAnimation(id)
        }
    }

    override fun getXml(id: Int): XmlResourceParser {
        return try {
            super.getXml(id)
        } catch (e: Exception) {
            pluginResources.getXml(id)
        }
    }

    override fun openRawResource(id: Int): InputStream {
        return try {
            super.openRawResource(id)
        } catch (e: Exception) {
            pluginResources.openRawResource(id)
        }
    }

    override fun openRawResource(id: Int, value: TypedValue?): InputStream {
        return try {
            super.openRawResource(id, value)
        } catch (e: Exception) {
            pluginResources.openRawResource(id, value)
        }
    }

    override fun openRawResourceFd(id: Int): AssetFileDescriptor {
        return try {
            super.openRawResourceFd(id)
        } catch (e: Exception) {
            pluginResources.openRawResourceFd(id)
        }
    }

    override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        try {
            super.getValue(id, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValue(id, outValue, resolveRefs)
        }
    }

    override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) {
        try {
            super.getValue(name, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValue(name, outValue, resolveRefs)
        }
    }

    override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        try {
            super.getValueForDensity(id, density, outValue, resolveRefs)
        } catch (e: Exception) {
            pluginResources.getValueForDensity(id, density, outValue, resolveRefs)
        }
    }

    override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray {
        return try {
            super.obtainAttributes(set, attrs)
        } catch (e: Exception) {
            pluginResources.obtainAttributes(set, attrs)
        }
    }

    override fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) {
        try {
            super.updateConfiguration(config, metrics)
        } catch (e: Exception) {
            pluginResources.updateConfiguration(config, metrics)
        }
    }

    override fun getDisplayMetrics(): DisplayMetrics {
        return try {
            super.getDisplayMetrics()
        } catch (e: Exception) {
            pluginResources.getDisplayMetrics()
        }
    }

    override fun getConfiguration(): Configuration {
        return try {
            super.getConfiguration()
        } catch (e: Exception) {
            pluginResources.getConfiguration()
        }
    }

    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int {
        return try {
            super.getIdentifier(name, defType, defPackage)
        } catch (e: Exception) {
            pluginResources.getIdentifier(name, defType, defPackage)
        }
    }

    override fun getResourceName(resid: Int): String {
        return return try {
            super.getResourceName(resid)
        } catch (e: Exception) {
            pluginResources.getResourceName(resid)
        }
    }

    override fun getResourcePackageName(resid: Int): String {
        return try {
            super.getResourcePackageName(resid)
        } catch (e: Exception) {
            pluginResources.getResourcePackageName(resid)
        }
    }

    override fun getResourceTypeName(resid: Int): String {
        return try {
            super.getResourceTypeName(resid)
        } catch (e: Exception) {
            pluginResources.getResourceTypeName(resid)
        }
    }

    override fun getResourceEntryName(resid: Int): String {
        return try {
            super.getResourceEntryName(resid)
        } catch (e: Exception) {
            pluginResources.getResourceEntryName(resid)
        }
    }

    override fun parseBundleExtras(parser: XmlResourceParser?, outBundle: Bundle?) {
        try {
            super.parseBundleExtras(parser, outBundle)
        } catch (e: Exception) {
            pluginResources.parseBundleExtras(parser, outBundle)
        }
    }

    override fun parseBundleExtra(tagName: String?, attrs: AttributeSet?, outBundle: Bundle?) {
        try {
            super.parseBundleExtra(tagName, attrs, outBundle)
        } catch (e: Exception) {
            pluginResources.parseBundleExtra(tagName, attrs, outBundle)
        }
    }

    @SuppressLint("NewApi")
    override fun addLoaders(vararg loaders: ResourcesLoader?) {
        try {
            super.addLoaders(*loaders)
        } catch (e: Exception) {
            pluginResources.addLoaders(*loaders)
        }
    }

    @SuppressLint("NewApi")
    override fun removeLoaders(vararg loaders: ResourcesLoader?) {
        try {
            super.removeLoaders(*loaders)
        } catch (e: Exception) {
            pluginResources.removeLoaders(*loaders)
        }
    }
}

fun injectRes(activity: Activity?) {
    activity?.runCatching {
        this.fieldSetFirst("mResources", PluginResource(resources))
    }
}