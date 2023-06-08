package com.freegang.plugin

import android.annotation.SuppressLint
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
import androidx.activity.ComponentActivity
import com.freegang.ktutils.reflect.findMethodAndInvoke
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.loader.hostClassloader
import com.freegang.xpler.loader.moduleClassloader
import java.io.InputStream

open class XplerActivity : ComponentActivity() {

    protected val mClassLoader by lazy {
        PluginClassloader()
    }

    protected val mResources by lazy {
        PluginResource(super.getResources())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    override fun getClassLoader(): ClassLoader = mClassLoader

    override fun getResources(): Resources = mResources

    protected class PluginClassloader : ClassLoader() {
        override fun findClass(name: String?): Class<*> {
            //KLogCat.d("XplerActivity - 插件加载类: $name")
            try {
                return moduleClassloader!!.loadClass(name)
            } catch (e: Exception) {
                //KLogCat.d("模块未找到: $name")
            }
            try {
                return hostClassloader!!.loadClass(name)
            } catch (e: Exception) {
                //KLogCat.d("宿主未找到: $name")
            }
            return super.findClass(name)
        }
    }

    protected class PluginResource(resources: Resources) :
        Resources(resources.assets, resources.displayMetrics, resources.configuration) {

        val moduleResources by lazy {
            val assetManager = AssetManager::class.java.newInstance()
            assetManager.findMethodAndInvoke("addAssetPath", KtXposedHelpers.modulePath)
            Resources(assetManager, resources.displayMetrics, resources.configuration)
        }

        val moduleAssets get() = moduleResources.assets

        override fun getText(id: Int): CharSequence {
            return try {
                super.getText(id)
            } catch (e: Exception) {
                moduleResources.getText(id)
            }
        }

        override fun getText(id: Int, def: CharSequence?): CharSequence {
            return try {
                super.getText(id, def)
            } catch (e: Exception) {
                moduleResources.getText(id, def)
            }
        }

        @SuppressLint("NewApi")
        override fun getFont(id: Int): Typeface {
            return try {
                super.getFont(id)
            } catch (e: Exception) {
                moduleResources.getFont(id)
            }
        }

        override fun getQuantityText(id: Int, quantity: Int): CharSequence {
            return try {
                super.getQuantityText(id, quantity)
            } catch (e: Exception) {
                moduleResources.getQuantityText(id, quantity)
            }
        }

        override fun getString(id: Int): String {
            return try {
                super.getString(id)
            } catch (e: Exception) {
                moduleResources.getString(id)
            }
        }

        override fun getString(id: Int, vararg formatArgs: Any?): String {
            return try {
                super.getString(id, *formatArgs)
            } catch (e: Exception) {
                moduleResources.getString(id, *formatArgs)
            }
        }

        override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String {
            return try {
                super.getQuantityString(id, quantity, *formatArgs)
            } catch (e: Exception) {
                moduleResources.getQuantityString(id, quantity, *formatArgs)
            }
        }

        override fun getQuantityString(id: Int, quantity: Int): String {
            return try {
                super.getQuantityString(id, quantity)
            } catch (e: Exception) {
                moduleResources.getQuantityString(id, quantity)
            }
        }

        override fun getTextArray(id: Int): Array<CharSequence> {
            return try {
                super.getTextArray(id)
            } catch (e: Exception) {
                moduleResources.getTextArray(id)
            }
        }

        override fun getStringArray(id: Int): Array<String> {
            return try {
                super.getStringArray(id)
            } catch (e: Exception) {
                moduleResources.getStringArray(id)
            }
        }

        override fun getIntArray(id: Int): IntArray {
            return try {
                super.getIntArray(id)
            } catch (e: Exception) {
                moduleResources.getIntArray(id)
            }
        }

        override fun obtainTypedArray(id: Int): TypedArray {
            return try {
                super.obtainTypedArray(id)
            } catch (e: Exception) {
                moduleResources.obtainTypedArray(id)
            }
        }

        override fun getDimension(id: Int): Float {
            return try {
                super.getDimension(id)
            } catch (e: Exception) {
                moduleResources.getDimension(id)
            }
        }

        override fun getDimensionPixelOffset(id: Int): Int {
            return try {
                super.getDimensionPixelOffset(id)
            } catch (e: Exception) {
                moduleResources.getDimensionPixelOffset(id)
            }
        }

        override fun getDimensionPixelSize(id: Int): Int {
            return try {
                super.getDimensionPixelSize(id)
            } catch (e: Exception) {
                moduleResources.getDimensionPixelSize(id)
            }
        }

        override fun getFraction(id: Int, base: Int, pbase: Int): Float {
            return try {
                super.getFraction(id, base, pbase)
            } catch (e: Exception) {
                moduleResources.getFraction(id, base, pbase)
            }
        }

        override fun getDrawable(id: Int): Drawable {
            return try {
                super.getDrawable(id)
            } catch (e: Exception) {
                moduleResources.getDrawable(id)
            }
        }

        override fun getDrawable(id: Int, theme: Theme?): Drawable {
            return try {
                super.getDrawable(id, theme)
            } catch (e: Exception) {
                moduleResources.getDrawable(id, theme)
            }
        }

        override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
            return try {
                super.getDrawableForDensity(id, density)
            } catch (e: Exception) {
                moduleResources.getDrawableForDensity(id, density)
            }
        }

        override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
            return try {
                super.getDrawableForDensity(id, density, theme)
            } catch (e: Exception) {
                moduleResources.getDrawableForDensity(id, density, theme)
            }
        }

        override fun getMovie(id: Int): Movie {
            return try {
                super.getMovie(id)
            } catch (e: Exception) {
                moduleResources.getMovie(id)
            }
        }

        override fun getColor(id: Int): Int {
            return try {
                super.getColor(id)
            } catch (e: Exception) {
                moduleResources.getColor(id)
            }
        }

        override fun getColor(id: Int, theme: Theme?): Int {
            return try {
                super.getColor(id, theme)
            } catch (e: Exception) {
                moduleResources.getColor(id, theme)
            }
        }

        override fun getColorStateList(id: Int): ColorStateList {
            return try {
                super.getColorStateList(id)
            } catch (e: Exception) {
                moduleResources.getColorStateList(id)
            }
        }

        override fun getColorStateList(id: Int, theme: Theme?): ColorStateList {
            return try {
                super.getColorStateList(id, theme)
            } catch (e: Exception) {
                moduleResources.getColorStateList(id, theme)
            }
        }

        override fun getBoolean(id: Int): Boolean {
            return try {
                super.getBoolean(id)
            } catch (e: Exception) {
                moduleResources.getBoolean(id)
            }
        }

        override fun getInteger(id: Int): Int {
            return try {
                super.getInteger(id)
            } catch (e: Exception) {
                moduleResources.getInteger(id)
            }
        }

        @SuppressLint("NewApi")
        override fun getFloat(id: Int): Float {
            return try {
                super.getFloat(id)
            } catch (e: Exception) {
                moduleResources.getFloat(id)
            }
        }

        override fun getLayout(id: Int): XmlResourceParser {
            return try {
                super.getLayout(id)
            } catch (e: Exception) {
                moduleResources.getLayout(id)
            }
        }

        override fun getAnimation(id: Int): XmlResourceParser {
            return try {
                super.getAnimation(id)
            } catch (e: Exception) {
                moduleResources.getAnimation(id)
            }
        }

        override fun getXml(id: Int): XmlResourceParser {
            return try {
                super.getXml(id)
            } catch (e: Exception) {
                moduleResources.getXml(id)
            }
        }

        override fun openRawResource(id: Int): InputStream {
            return try {
                super.openRawResource(id)
            } catch (e: Exception) {
                moduleResources.openRawResource(id)
            }
        }

        override fun openRawResource(id: Int, value: TypedValue?): InputStream {
            return try {
                super.openRawResource(id, value)
            } catch (e: Exception) {
                moduleResources.openRawResource(id, value)
            }
        }

        override fun openRawResourceFd(id: Int): AssetFileDescriptor {
            return try {
                super.openRawResourceFd(id)
            } catch (e: Exception) {
                moduleResources.openRawResourceFd(id)
            }
        }

        override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
            try {
                super.getValue(id, outValue, resolveRefs)
            } catch (e: Exception) {
                moduleResources.getValue(id, outValue, resolveRefs)
            }
        }

        override fun getValue(name: String?, outValue: TypedValue?, resolveRefs: Boolean) {
            try {
                super.getValue(name, outValue, resolveRefs)
            } catch (e: Exception) {
                moduleResources.getValue(name, outValue, resolveRefs)
            }
        }

        override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue?, resolveRefs: Boolean) {
            try {
                super.getValueForDensity(id, density, outValue, resolveRefs)
            } catch (e: Exception) {
                moduleResources.getValueForDensity(id, density, outValue, resolveRefs)
            }
        }

        override fun obtainAttributes(set: AttributeSet?, attrs: IntArray?): TypedArray {
            return try {
                super.obtainAttributes(set, attrs)
            } catch (e: Exception) {
                moduleResources.obtainAttributes(set, attrs)
            }
        }

        override fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) {
            try {
                super.updateConfiguration(config, metrics)
            } catch (e: Exception) {
                moduleResources.updateConfiguration(config, metrics)
            }
        }

        override fun getDisplayMetrics(): DisplayMetrics {
            return try {
                super.getDisplayMetrics()
            } catch (e: Exception) {
                moduleResources.getDisplayMetrics()
            }
        }

        override fun getConfiguration(): Configuration {
            return try {
                super.getConfiguration()
            } catch (e: Exception) {
                moduleResources.getConfiguration()
            }
        }

        override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int {
            return try {
                super.getIdentifier(name, defType, defPackage)
            } catch (e: Exception) {
                moduleResources.getIdentifier(name, defType, defPackage)
            }
        }

        override fun getResourceName(resid: Int): String {
            return return try {
                super.getResourceName(resid)
            } catch (e: Exception) {
                moduleResources.getResourceName(resid)
            }
        }

        override fun getResourcePackageName(resid: Int): String {
            return try {
                super.getResourcePackageName(resid)
            } catch (e: Exception) {
                moduleResources.getResourcePackageName(resid)
            }
        }

        override fun getResourceTypeName(resid: Int): String {
            return try {
                super.getResourceTypeName(resid)
            } catch (e: Exception) {
                moduleResources.getResourceTypeName(resid)
            }
        }

        override fun getResourceEntryName(resid: Int): String {
            return try {
                super.getResourceEntryName(resid)
            } catch (e: Exception) {
                moduleResources.getResourceEntryName(resid)
            }
        }

        override fun parseBundleExtras(parser: XmlResourceParser?, outBundle: Bundle?) {
            try {
                super.parseBundleExtras(parser, outBundle)
            } catch (e: Exception) {
                moduleResources.parseBundleExtras(parser, outBundle)
            }
        }

        override fun parseBundleExtra(tagName: String?, attrs: AttributeSet?, outBundle: Bundle?) {
            try {
                super.parseBundleExtra(tagName, attrs, outBundle)
            } catch (e: Exception) {
                moduleResources.parseBundleExtra(tagName, attrs, outBundle)
            }
        }

        @SuppressLint("NewApi")
        override fun addLoaders(vararg loaders: ResourcesLoader?) {
            try {
                super.addLoaders(*loaders)
            } catch (e: Exception) {
                moduleResources.addLoaders(*loaders)
            }
        }

        @SuppressLint("NewApi")
        override fun removeLoaders(vararg loaders: ResourcesLoader?) {
            try {
                super.removeLoaders(*loaders)
            } catch (e: Exception) {
                moduleResources.removeLoaders(*loaders)
            }
        }
    }
}