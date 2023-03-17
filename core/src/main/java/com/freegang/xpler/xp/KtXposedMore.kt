package com.freegang.xpler.xp

import android.app.Activity
import android.content.Context
import android.content.res.XModuleResources
import android.view.View
import androidx.annotation.LayoutRes
import com.freegang.xpler.utils.other.KResourceUtils
import com.freegang.xpler.xp.bridge.ConstructorHook
import com.freegang.xpler.xp.bridge.MethodHook
import com.freegang.xpler.xp.bridge.MethodHookImpl
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Field
import java.lang.reflect.Method

//Method
/**
 * 对某个方法直接Hook
 *
 * @param block hook代码块, 可在内部书写hook逻辑
 */
fun Method.hook(block: MethodHook.() -> Unit) {
    val methodHookImpl = MethodHookImpl(this)
    block.invoke(methodHookImpl)
    methodHookImpl.start()
}

/**
 * 对某个方法直接调用
 *
 * @param obj 含有该方法的实例对象
 * @param args 参数列表值
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
fun Method.call(obj: Any, vararg args: Any): Any? {
    return XposedHelpers.callMethod(obj, this.name, *args)
}


//Object
/**
 * 从实例对象中直接寻找某个方法,
 *
 * @param methodName 目标方法名
 * @param args 参数列表值
 * @return 被找到的目标方法, 可能是 null 即没有该方法
 */
fun Any.findMethod(methodName: String, vararg args: Any): Method? {
    return try {
        XposedHelpers.findMethodExact(this::class.java, methodName, *args)
    } catch (e: Exception) {
        null
    }
}

/**
 * 从实例对象中直接寻找某个方法,
 *
 * @param methodName 目标方法名
 * @param argsTypes 参数类型列表
 * @return 被找到的目标方法, 可能是 null 即没有该方法
 */
fun Any.findMethod(methodName: String, vararg argsTypes: Class<*>): Method? {
    return try {
        XposedHelpers.findMethodExact(this::class.java, methodName, *argsTypes)
    } catch (e: Exception) {
        null
    }
}

/**
 * 从实例对象中直接寻找某些名称相同的所有方法,
 *
 * 不在乎参数类型列表, 返回值类型
 *
 * @param methodName 目标方法名
 * @return 被找到的目标方法列表, 可能是 empty 即没有该方法
 */
fun Any.findMethodsByName(methodName: String): List<Method> {
    val result = mutableListOf<Method>()
    val methods = this::class.java.declaredMethods
    for (method in methods) {
        if (method.name == methodName) {
            method.isAccessible = true
            result.add(method)
        }
    }
    return result
}

/**
 * 从实例对象中直接寻找某些返回值类型相同的所有方法,
 *
 * 不在乎方法名, 参数类型列表
 *
 * @param returnType 返回值类型
 * @return 被找到的目标方法列表, 可能是 empty 即没有该方法
 */
fun Any.findMethodsByReturnType(returnType: Class<*>): List<Method> {
    val result = mutableListOf<Method>()
    val methods = this::class.java.declaredMethods
    for (method in methods) {
        if (method.returnType == returnType) {
            method.isAccessible = true
            result.add(method)
        }
    }
    return result
}


/**
 * 从实例对象中直接寻找某些类型相同的所有字段,
 *
 * 不在乎字段名
 *
 * @param type 返回值类型
 * @return 被找到的目标字段列表, 可能是 empty 即没有该字段
 */
fun Any.findFieldByType(type: Class<*>): List<Field> {
    val result = mutableListOf<Field>()
    val fields = this::class.java.declaredFields
    for (field in fields) {
        if (field.type == type) {
            field.isAccessible = true
            result.add(field)
        }
    }
    return result
}

/**
 * 从实例对象中直接调用某个方法
 *
 * @param methodName 方法名
 * @param args 参数列表值
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
fun <T> Any.callMethod(methodName: String, vararg args: Any): T? {
    return XposedHelpers.callMethod(this, methodName, *args) as? T
}

/**
 * 从实例对象中直接调用某个方法
 *
 * @param methodName 方法名
 * @param argsTypes 参数类型列表
 * @param args 参数列表值 (需要与[argsTypes]类型一一对应)
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
fun <T> Any.callMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    return XposedHelpers.callMethod(this, methodName, *argsTypes, *args) as? T
}

/**
 * 从实例对象中直接调用某个静态方法
 *
 * @param methodName 方法名
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
fun <T> Any.callStaticMethod(methodName: String, vararg args: Any): T? {
    return XposedHelpers.callStaticMethod(this::class.java, methodName, *args) as T?
}


/**
 * 从实例对象中直接调用某个静态方法
 *
 * @param methodName 方法名
 * @param argsTypes 参数类型列表
 * @param args 参数列表值 (需要与[argsTypes]类型一一对应)
 * @return 该方法被调用之后的返回值, 可能是 null 即没有返回值
 */
fun <T> Any.callStaticMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    return XposedHelpers.callStaticMethod(this::class.java, methodName, *argsTypes, *args) as T?
}

/**
 * 从实例对象中直接获取某个字段的值
 *
 * @param fieldName 字段名
 * @return 该字段的值, 可能是 null 即被赋值
 */
fun <T> Any.getObjectField(fieldName: String): T? {
    val field = XposedHelpers.findFieldIfExists(this::class.java, fieldName) ?: return null
    return field.get(this) as? T
}

/**
 * 从实例对象中直接获取某个静态字段的值
 *
 * @param fieldName 字段名
 * @return 该字段的值, 可能是 null 即被赋值
 */
fun <T> Any.getStaticObjectField(fieldName: String): T? {
    val field = XposedHelpers.findFieldIfExists(this::class.java, fieldName) ?: return null
    return field.get(null) as? T
}

/**
 * 对示例对象中的某个字段赋值
 *
 * @param fieldName 字段名
 * @param value 字段值
 */
fun Any.setObjectField(fieldName: String, value: Any) {
    XposedHelpers.setObjectField(this, fieldName, value)
}

/**
 * 对示例对象中的某个静态字段赋值
 *
 * @param fieldName 字段名
 * @param value 字段值
 */
fun Any.setStaticObjectField(fieldName: String, value: Any) {
    XposedHelpers.setStaticObjectField(this::class.java, fieldName, value)
}


//String
/**
 * 将某个字符串转换为Class, 如果该类不存在抛出异常
 *
 * @param classLoader 类加载器, 默认为[XposedBridge.BOOTCLASSLOADER]
 * @throws ClassNotFoundError
 * @return 被找到的类
 */
@Throws
fun String.toClass(classLoader: ClassLoader = XposedBridge.BOOTCLASSLOADER): Class<*>? {
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

/**
 * 将某个字符串转换为Class同时Hook，如果该类/方法不存在抛出异常
 *
 * 例: "com.xxx.MainActivity#onCreate".hookMethod(Bundle::class.java){ ... }
 *
 * @param classLoader 类加载器, 默认为[XposedBridge.BOOTCLASSLOADER]
 * @throws ClassNotFoundError|NoSuchMethodException
 * @return KtXposedHelpers
 */
fun String.hookMethod(classLoader: ClassLoader = XposedBridge.BOOTCLASSLOADER, vararg argsTypes: Any, block: MethodHook.() -> Unit)
        : KtXposedHelpers {
    if (!this.contains("#")) throw NoSuchMethodException("please refer to: \"com.xxx.ClassName#MethodName\".hookMethod(...)")
    val indexOf = this.indexOf("#")
    val className = this.substring(0, indexOf)
    val methodName = this.substring(indexOf + 1)

    val clazz = XposedHelpers.findClass(className, classLoader)
    return KtXposedHelpers
        .hookClass(clazz)
        .method(methodName, *argsTypes) {
            block.invoke(this)
        }
}


//Class
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


//ClassLoader
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

/**
 * 查找某个类
 *
 * @param className 类名
 * @return 找到的某个类
 */
fun ClassLoader.findClassByXposed(className: String): Class<*>? {
    return XposedHelpers.findClass(className, this)
}


//Context
/**
 * 加载模块中的xml布局文件
 *
 * 需要注意的是, 模块中的xml不能直接引入模块自身的资源文件,
 * 如: @color/module_blank, @drawable/ic_logo 等
 *
 * 否则无法加载成功, 如需使用模块中的资源, 见: [KResourceUtils]
 *
 * @param id module layout xml id
 */
fun <T : View> Context.inflateModuleView(@LayoutRes id: Int): T {
    return KResourceUtils.inflateView<T>(this, id)
}


//Xposed
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
 * 遍历被Hook的某个方法中的参数列表, 如果参数列表为空, 代码块不执行
 *
 * @param block 代码块
 */
fun XC_MethodHook.MethodHookParam.forEachArgs(block: (any: Any) -> Unit) {
    if (args == null || args.isEmpty()) return
    this.args.forEach(block)
}

/**
 * 遍历被Hook的某个方法中的参数列表, 如果参数列表为空, 代码块不执行
 *
 * @param block 代码块
 */
fun XC_MethodHook.MethodHookParam.forEachIndexArgs(block: (index: Int, any: Any) -> Unit) {
    if (args == null || args.isEmpty()) return
    this.args.forEachIndexed(block)
}

/**
 * 打印被Hook的某个方法中的堆栈信息
 */
fun XC_MethodHook.MethodHookParam.dumpStackLog() {
    try {
        Thread.dumpStack()
    } catch (e: Exception) {
        e.printStackTrace()
        XposedBridge.log(e)
    }
}

/**
 * 将被Hook的某个方法中的持有实例转为Activity, 如果该实例对象不是Activity则抛出异常
 */
val XC_MethodHook.MethodHookParam.thisActivity: Activity
    get() {
        if (thisObject !is Activity) throw Exception("$thisObject unable to cast to Activity")
        return thisObject as Activity
    }

/**
 * 将被Hook的某个方法中的持有实例转为Context, 如果该实例对象不是Context则抛出异常
 */
val XC_MethodHook.MethodHookParam.thisContext: Context
    get() {
        if (thisObject !is Context) throw Exception("$thisObject unable to cast to Context!")
        return thisObject as Context
    }


//Other
private var mModulePath: String? = null
private var mModuleRes: XModuleResources? = null
fun KtXposedHelpers.Companion.initModule(modulePath: String, moduleRes: XModuleResources) {
    mModulePath = modulePath
    mModuleRes = moduleRes
}

fun KtXposedHelpers.Companion.getModulePath() = mModulePath!!
fun KtXposedHelpers.Companion.getModuleRes() = mModuleRes!!
