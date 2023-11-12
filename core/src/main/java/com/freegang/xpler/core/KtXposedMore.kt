package com.freegang.xpler.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.KtXposedHelpers.Companion.setLpparam
import com.freegang.xpler.core.bridge.ConstructorHook
import com.freegang.xpler.core.bridge.MethodHook
import com.freegang.xpler.core.bridge.MethodHookImpl
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

// Method
/**
 * 对某个方法直接Hook
 *
 * @param block hook代码块, 可在内部书写hook逻辑
 */
fun Method.hook(block: MethodHook.() -> Unit) {
    val methodHookImpl = MethodHookImpl(this)
    block.invoke(methodHookImpl)
    methodHookImpl.startHook()
}

/**
 * 对某个方法直接调用
 *
 * @param obj 含有该方法的实例对象
 * @param args 参数列表值
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
inline fun <reified T> Method.call(obj: Any, vararg args: Any?): T? {
    return XposedBridge.invokeOriginalMethod(this, obj, args) as T?
}


// Object
/**
 * 从实例对象中直接调用某个方法
 *
 * @param methodName 方法名
 * @param args 参数列表值
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
inline fun <reified T> Any.callMethod(methodName: String, vararg args: Any?): T? {
    return XposedHelpers.callMethod(this, methodName, *args) as T?
}

/**
 * 从实例对象中直接调用某个方法
 *
 * @param methodName 方法名
 * @param argsTypes 参数类型列表
 * @param args 参数列表值 (需要与[argsTypes]类型一一对应)
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
inline fun <reified T> Any.callMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    return XposedHelpers.callMethod(this, methodName, argsTypes, *args) as T?
}

/**
 * 从实例对象中直接获取某个字段的值
 *
 * @param fieldName 字段名
 * @return 该字段的值, 可能是 null 即被赋值
 */
inline fun <reified T> Any.getObjectField(fieldName: String): T? {
    return XposedHelpers.getObjectField(this, fieldName) as T?
}

/**
 * 对实例对象中的某个字段赋值
 *
 * @param fieldName 字段名
 * @param value 字段值
 */
fun Any.setObjectField(fieldName: String, value: Any) {
    XposedHelpers.setObjectField(this, fieldName, value)
}

/**
 * 需要在 [de.robv.android.xposed.callbacks.XC_LoadPackage.handleLoadPackage] 时
 * 调用 [setLpparam]方法进行存储, 否则将无法使用
 * 可以参考 [com.freegang.xpler.HookInit.handleLoadPackage] 的实现
 */
val Any.lpparam: XC_LoadPackage.LoadPackageParam get() = KtXposedHelpers.lpparam


// String
/**
 * 将某个字符串转换为Class, 如果该类不存在抛出异常
 *
 * @param classLoader 类加载器, 默认为[XposedBridge.BOOTCLASSLOADER]
 * @throws ClassNotFoundError
 * @return 被找到的类
 */
fun String.findClass(classLoader: ClassLoader = XposedBridge.BOOTCLASSLOADER): Class<*>? {
    return XposedHelpers.findClass(this, classLoader)
}

/**
 * 将某个字符串转换为Class同时Hook，如果该类不存在抛出异常
 *
 * @param classLoader 类加载器, 默认为[XposedBridge.BOOTCLASSLOADER]
 * @throws ClassNotFoundError
 * @return KtXposedHelpers
 */
fun String.hookClass(classLoader: ClassLoader = XposedBridge.BOOTCLASSLOADER): KtXposedHelpers {
    val clazz = XposedHelpers.findClass(this, classLoader)
    return KtXposedHelpers.hookClass(clazz)
}


// Class
/**
 * 从Class中直接获取某个静态字段的值
 *
 * @param fieldName 字段名
 * @return 该字段的值, 可能是 null 即被赋值
 */
inline fun <reified T> Class<*>.getStaticObjectField(fieldName: String): T? {
    val get = XposedHelpers.getStaticObjectField(this, fieldName) ?: null
    return get as T?
}

/**
 * 从Class中直接调用某个静态方法
 *
 * @param methodName 方法名
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
inline fun <reified T> Class<*>.callStaticMethod(methodName: String, vararg args: Any): T? {
    val method = XposedHelpers.findMethodBestMatch(this, methodName, *XposedHelpers.getParameterTypes(*args))
    return XposedBridge.invokeOriginalMethod(method, null, args) as T?
}

/**
 * 从Class中直接调用某个静态方法
 *
 * @param methodName 方法名
 * @param argsTypes 参数类型列表
 * @param args 参数列表值 (需要与[argsTypes]类型一一对应)
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
inline fun <reified T> Class<*>.callStaticMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    val method = XposedHelpers.findMethodBestMatch(this, methodName, *argsTypes)
    return XposedBridge.invokeOriginalMethod(method, null, args) as T?
}

/**
 * Hook某个Class的构造方法
 *
 * @param argsTypes 参数类型列表
 * @throws block hook代码块, 可在内部书写hook逻辑
 * @return KtXposedHelpers
 */
fun Class<*>.hookConstructor(vararg argsTypes: Any, block: ConstructorHook.() -> Unit): KtXposedHelpers {
    return KtXposedHelpers
        .hookClass(this)
        .constructor(*argsTypes) { block.invoke(this) }
}

/**
 * Hook某个Class的某个方法
 *
 * @param methodName 方法名
 * @param argsTypes 参数类型列表
 * @throws block hook代码块, 可在内部书写hook逻辑
 * @return KtXposedHelpers
 */
fun Class<*>.hookMethod(methodName: String, vararg argsTypes: Any, block: MethodHook.() -> Unit): KtXposedHelpers {
    return KtXposedHelpers
        .hookClass(this)
        .method(methodName, *argsTypes) { block.invoke(this) }
}

/**
 * Hook某个Class的所有构造方法
 * @throws block hook代码块, 可在内部书写hook逻辑
 * @return KtXposedHelpers
 */
fun Class<*>.hookConstructorsAll(block: MethodHook.() -> Unit) {
    KtXposedHelpers
        .hookClass(this)
        .constructorsAll { block.invoke(this) }
}

/**
 * Hook某个Class的所有方法
 * @throws block hook代码块, 可在内部书写hook逻辑
 * @return KtXposedHelpers
 */
fun Class<*>.hookMethodAll(block: MethodHook.() -> Unit) {
    KtXposedHelpers
        .hookClass(this)
        .methodAll { block.invoke(this) }
}


// ClassLoader
/**
 * Hook某个Class
 *
 * @param clazz 类
 * @return KtXposedHelpers
 */
fun ClassLoader.hookClass(clazz: Class<*>): KtXposedHelpers {
    return KtXposedHelpers.hookClass(clazz.name, this)
}

/**
 * Hook某个Class
 *
 * @param className 类名
 * @return KtXposedHelpers
 */
fun ClassLoader.hookClass(className: String): KtXposedHelpers {
    return KtXposedHelpers.hookClass(className, this)
}


// Context
/**
 * 加载模块中的xml布局文件
 *
 * 需要注意的是, 模块中的xml不能直接引入模块自身的资源文件,
 * 如: @color/module_blank, @drawable/ic_logo 等
 *
 * 否则无法加载成功, 如需使用模块中的资源, 见: [KtXposedHelpers]
 *
 * @param id module layout xml id
 */
inline fun <reified T : View> Context.inflateModuleView(@LayoutRes id: Int): T {
    return KtXposedHelpers.inflateView(this, id)
}

/**
 * 获取模块中的 drawable
 *
 * @param id id
 * @return Drawable
 */
fun Context.getModuleDrawable(@DrawableRes id: Int): Drawable? {
    return KtXposedHelpers.getDrawable(id)
}

/**
 * 获取模块中的 color
 *
 * @param id id
 * @return color int
 */
fun Context.getModuleColor(@ColorRes id: Int): Int {
    return KtXposedHelpers.getColor(id)
}

/**
 * 获取模块中的 Animation
 *
 * @param id id
 * @return Animation XmlResourceParser
 */
fun Context.getModuleAnimation(@AnimatorRes @AnimRes id: Int): XmlResourceParser {
    return KtXposedHelpers.getAnimation(id)
}

/**
 * 获取模块中的 String
 *
 * @param id id
 * @return String
 */
fun Context.getModuleString(@StringRes id: Int): String {
    return KtXposedHelpers.moduleRes.getString(id)
}


// Xposed
/**
 * Hook某个Class
 *
 * @param clazz 类
 * @return KtXposedHelpers
 */
fun XC_LoadPackage.LoadPackageParam.hookClass(clazz: Class<*>): KtXposedHelpers {
    return KtXposedHelpers.hookClass(clazz.name, this.classLoader)
}

/**
 * Hook某个Class
 *
 * @param className 类名
 * @return KtXposedHelpers
 */
fun XC_LoadPackage.LoadPackageParam.hookClass(className: String): KtXposedHelpers {
    return KtXposedHelpers.hookClass(className, this.classLoader)
}

/**
 * 查找某个类
 *
 * @param className 类名
 * @return 找到的某个类
 */
fun XC_LoadPackage.LoadPackageParam.findClass(className: String): Class<*> {
    return XposedHelpers.findClass(className, this.classLoader)
}

/**
 * 打印Xposed日志
 *
 * @param log 内容
 */
fun XC_LoadPackage.LoadPackageParam.xposedLog(log: String) {
    XposedBridge.log(log)
}

/**
 * 打印Xposed日志
 *
 * @param log 内容
 */
fun XC_LoadPackage.LoadPackageParam.xposedLog(log: Throwable) {
    XposedBridge.log(log)
}

/**
 * 打印Xposed日志
 *
 * @param log 内容
 */
fun XC_MethodHook.MethodHookParam.xposedLog(log: String) {
    XposedBridge.log(log)
}

/**
 * 打印Xposed日志
 *
 * @param log 内容
 */
fun XC_MethodHook.MethodHookParam.xposedLog(log: Throwable) {
    XposedBridge.log(log)
}

/**
 * 打印Xposed日志
 *
 * 并打印KLogCat日志
 *
 * @param log 内容
 */
fun KLogCat.Companion.xposedLog(log: String, xposed: Boolean = true) {
    d(log)
    if (xposed) {
        XposedBridge.log(log)
    }
}

/**
 * 打印Xposed日志
 *
 * 并打印KLogCat日志
 *
 * @param log 内容
 */
fun KLogCat.Companion.xposedLog(log: Throwable, xposed: Boolean = true) {
    e(log)
    if (xposed) {
        XposedBridge.log(log)
    }
}

/**
 * 对任意对象, 打印Xposed日志
 */
fun Any.xposedLog() {
    if (this is Map<*, *>) {
        XposedBridge.log(
            "映射对象: $this -- 内容: ${
                this.map { "key=${it.key},value=${it.value}" }.joinToString(", ")
            }"
        )
    }
    if (this is Collection<*>) {
        XposedBridge.log("集合对象: $this -- 内容: ${this.joinToString(", ")}")
        return
    }
    if (this is Array<*>) {
        XposedBridge.log("数组对象: $this -- 内容: ${this.joinToString(", ")}")
        return
    }
    XposedBridge.log("$this")
}

/**
 * 对任意对象, 打印KLogCat日志
 */
fun Any.logd(save: Boolean = false) {
    if (save) {
        KLogCat.openStorage()
    } else {
        KLogCat.closeStorage()
    }

    if (this is Map<*, *>) {
        KLogCat.d(
            "$this",
            this.map { "key=${it.key},value=${it.value}" }.joinToString(", "),
        )
        return
    }
    if (this is Collection<*>) {
        KLogCat.d(
            "$this",
            this.joinToString(", "),
        )
        return
    }
    if (this is Array<*>) {
        KLogCat.d(
            "$this",
            this.joinToString(", ")
        )
        return
    }
    KLogCat.d("$this")
    KLogCat.closeStorage()
}

/**
 * 打印方法中的堆栈信息
 */
fun Any.dumpStackLog() {
    try {
        throw Exception("Stack trace")
    } catch (e: Exception) {
        KLogCat.d(e.stackTraceToString())
    }
}

/**
 * 将被Hook的某个方法中的持有实例转为Application, 如果该实例对象不是Application则抛出异常
 */
@get:Throws(TypeCastException::class)
val XC_MethodHook.MethodHookParam.thisApplication: Application
    get() {
        if (thisObject !is Application) throw TypeCastException("$thisObject unable to cast to Application")
        return thisObject as Application
    }

/**
 * 将被Hook的某个方法中的持有实例转为Activity, 如果该实例对象不是Activity则抛出异常
 */
@get:Throws(TypeCastException::class)
val XC_MethodHook.MethodHookParam.thisActivity: Activity
    get() {
        if (thisObject !is Activity) throw TypeCastException("$thisObject unable to cast to Activity")
        return thisObject as Activity
    }

/**
 * 将被Hook的某个方法中的持有实例转为Context, 如果该实例对象不是Context则抛出异常
 */
@get:Throws(TypeCastException::class)
val XC_MethodHook.MethodHookParam.thisContext: Context
    get() {
        if (thisObject is View) return thisView.context
        if (thisObject !is Context) throw TypeCastException("$thisObject unable to cast to Context!")
        return thisObject as Context
    }

/**
 * 将被Hook的某个方法中的持有实例转为View, 如果该实例对象不是View则抛出异常
 */
@get:Throws(TypeCastException::class)
val XC_MethodHook.MethodHookParam.thisView: View
    get() {
        if (thisObject !is View) throw TypeCastException("$thisObject unable to cast to View!")
        return thisObject as View
    }

val XC_MethodHook.MethodHookParam.thisViewOrNull: View?
    get() {
        if (thisObject == null || thisObject !is View) return null
        return thisObject as View?
    }

/**
 * 将被Hook的某个方法中的持有实例转为ViewGroup, 如果该实例对象不是ViewGroup则抛出异常
 */
@get:Throws(TypeCastException::class)
val XC_MethodHook.MethodHookParam.thisViewGroup: ViewGroup
    get() {
        if (thisObject !is ViewGroup) throw TypeCastException("$thisObject unable to cast to ViewGroup!")
        return thisObject as ViewGroup
    }

val XC_MethodHook.MethodHookParam.thisViewGroupOrNull: ViewGroup?
    get() {
        if (thisObject == null || thisObject !is ViewGroup) return null
        return thisObject as ViewGroup?
    }

/**
 * args可能会是 NullPointerException,
 * 当 NullPointerException 时无法通过 `*args`解构
 */
val XC_MethodHook.MethodHookParam.argsOrEmpty: Array<Any>
    get() {
        return args ?: emptyArray()
    }

/**
 * DSL特性便捷使用带param参数的方法
 * @param params XC_MethodHook.MethodHookParam
 * @param block XC_MethodHook.MethodHookParam.()
 */
inline fun <R> hookBlockRunning(
    params: XC_MethodHook.MethodHookParam,
    block: XC_MethodHook.MethodHookParam.() -> R,
): Result<R> {
    return runCatching {
        block.invoke(params)
    }
}