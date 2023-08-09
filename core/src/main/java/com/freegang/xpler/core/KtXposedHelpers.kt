package com.freegang.xpler.core

import android.content.Context
import android.content.res.Resources.Theme
import android.content.res.XModuleResources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.freegang.xpler.core.bridge.ConstructorHook
import com.freegang.xpler.core.bridge.ConstructorHookImpl
import com.freegang.xpler.core.bridge.MethodHook
import com.freegang.xpler.core.bridge.MethodHookImpl
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

/// Xposed hook 的基础封装
class KtXposedHelpers {
    private var clazz: Class<*>? = null

    companion object {
        private val instances = KtXposedHelpers()
        private var mModulePath: String? = null
        private var mModuleRes: XModuleResources? = null
        private var mLpparam: XC_LoadPackage.LoadPackageParam? = null

        /**
         * Hook某个类
         * @param clazz 类
         */
        fun hookClass(clazz: Class<*>): KtXposedHelpers {
            instances.clazz = clazz
            return instances
        }

        /**
         * Hook某个类
         * @param className 类名
         * @param classLoader 类加载器
         */
        fun hookClass(className: String, classLoader: ClassLoader): KtXposedHelpers {
            val findClass = XposedHelpers.findClass(className, classLoader)
            instances.clazz = findClass
            return instances
        }

        /**
         * 加载模块中的xml布局文件
         *
         * 需要注意的是, 模块中的xml不能直接引入模块自身的资源文件,
         * 如: @color/module_blank, @drawable/ic_logo 等
         *
         * 如需加载资源文件见[getDrawable]、[getColor]、[getDrawable]、[getModuleAnimation]
         *
         * @param context context
         * @param id id
         * @return View
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
        fun getLayout(@LayoutRes id: Int): XmlResourceParser {
            return moduleRes.getLayout(id)
        }

        /**
         * 获取模块中的 drawable
         *
         * @param id id
         * @return Drawable
         */
        fun getDrawable(@DrawableRes id: Int, theme: Theme? = null): Drawable? {
            return ResourcesCompat.getDrawable(moduleRes, id, theme)
        }

        /**
         * 获取模块中的 color
         *
         * @param id id
         * @return color int
         */
        fun getColor(@ColorRes id: Int, theme: Theme? = null): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                moduleRes.getColor(id, theme)
            } else {
                moduleRes.getColor(id)
            }
        }

        /**
         * 获取模块中的 Float
         *
         * @param id id
         * @return Float
         */
        fun getDimension(@DimenRes id: Int): Float {
            return moduleRes.getDimension(id)
        }

        /**
         * 获取模块中的 Animation
         *
         * @param id id
         * @return Animation XmlResourceParser
         */
        fun getAnimation(@AnimatorRes @AnimRes id: Int): XmlResourceParser {
            return moduleRes.getAnimation(id)
        }

        /**
         * 获取模块中的 String
         *
         * @param id id
         * @return String
         */
        fun getString(@StringRes id: Int): String {
            return moduleRes.getString(id)
        }

        /**
         * 获取模块中的 String Array
         *
         * @param id id
         * @return String Array
         */
        fun getStringArray(@ArrayRes id: Int): Array<String> {
            return moduleRes.getStringArray(id)
        }

        /**
         * 由 [de.robv.android.xposed.IXposedHookZygoteInit.initZygote] 时调用该方法
         * 可以参考 [com.freegang.xpler.HookInit.initZygote] 的实现
         *
         * @param modulePath 模块路径
         */
        fun initModule(modulePath: String) {
            mModulePath = modulePath
            mModuleRes = XModuleResources.createInstance(modulePath, null)
        }

        /**
         * 返回模块路径, 需要首先 [initModule] 调用
         */
        val modulePath: String
            get() {
                return mModulePath!!
            }

        /**
         * 返回模块资源, 需要首先 [initModule] 调用
         */
        val moduleRes: XModuleResources
            get() {
                return mModuleRes!!
            }

        /**
         * 存储 XC_LoadPackage.LoadPackageParam 实例
         * @param lpparam
         */
        fun setLpparam(lpparam: XC_LoadPackage.LoadPackageParam) {
            this.mLpparam = lpparam
        }

        /**
         * 需要在 [de.robv.android.xposed.callbacks.XC_LoadPackage.handleLoadPackage] 时
         * 调用 [setLpparam]方法进行存储, 否则将无法使用
         * 可以参考 [com.freegang.xpler.HookInit.handleLoadPackage] 的实现
         */
        val lpparam: XC_LoadPackage.LoadPackageParam get() = this.mLpparam!!
    }

    /**
     * Hook某个方法
     *
     * @param argsTypes 参数类型列表
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun constructor(vararg argsTypes: Any, block: ConstructorHook.() -> Unit): KtXposedHelpers {
        val constructorHookImpl = ConstructorHookImpl(clazz!!, *argsTypes)
        block.invoke(constructorHookImpl)
        constructorHookImpl.startHook()
        return this
    }

    /**
     * Hook某个类中的所有构造方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun constructorsAll(block: MethodHook.() -> Unit): KtXposedHelpers {
        val constructors = clazz!!.declaredConstructors
        for (c in constructors) {
            c.isAccessible = true
            val constructorHookImpl = MethodHookImpl(c)
            block.invoke(constructorHookImpl)
            constructorHookImpl.startHook()
        }
        return this
    }

    /**
     * Hook某个方法
     *
     * @param methodName 方法
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun method(methodName: Method, block: MethodHook.() -> Unit): KtXposedHelpers {
        val methodHookImpl = MethodHookImpl(methodName)
        block.invoke(methodHookImpl)
        methodHookImpl.startHook()
        return this
    }

    /**
     * Hook某个方法
     *
     * @param methodName 方法名
     * @param argsTypes 参数类型列表
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun method(methodName: String, vararg argsTypes: Any, block: MethodHook.() -> Unit): KtXposedHelpers {
        val methodHookImpl = MethodHookImpl(clazz!!, methodName, *argsTypes)
        block.invoke(methodHookImpl)
        methodHookImpl.startHook()
        return this
    }

    /**
     * Hook某个类中的所有方法(构造方法除外)
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun methodAll(block: MethodHook.() -> Unit): KtXposedHelpers {
        val methods = clazz!!.declaredMethods
        for (method in methods) {
            method.isAccessible = true
            val methodHookImpl = MethodHookImpl(method)
            block.invoke(methodHookImpl)
            methodHookImpl.startHook()
        }
        return this
    }

    /**
     * Hook某个类中所有[methodName]同名方法,
     *
     * 不在乎参数类型、数量
     *
     * @param methodName 方法名
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    fun methodAllByName(methodName: String, block: MethodHook.() -> Unit): KtXposedHelpers {
        val methods = clazz!!.declaredMethods
        for (method in methods) {
            if (method.name == methodName) {
                method.isAccessible = true
                val methodHookImpl = MethodHookImpl(method)
                block.invoke(methodHookImpl)
                methodHookImpl.startHook()
            }
        }
        return this
    }

    /**
     * Hook某个类中所有方法返回类型为[returnType]的方法,
     *
     * 不在乎方法名, 参数类型
     *
     * @param returnType 返回类型
     */
    fun methodAllByReturnType(returnType: Class<*>, block: MethodHook.() -> Unit): KtXposedHelpers {
        val methods = clazz!!.declaredMethods
        for (method in methods) {
            if (method.returnType == returnType) {
                method.isAccessible = true
                val methodHookImpl = MethodHookImpl(method)
                block.invoke(methodHookImpl)
                methodHookImpl.startHook()
            }
        }
        return this
    }
}