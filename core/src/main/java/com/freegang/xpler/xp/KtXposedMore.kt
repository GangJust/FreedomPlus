package com.freegang.xpler.xp

import android.content.res.XModuleResources
import com.freegang.xpler.xp.bridge.ConstructorHook
import com.freegang.xpler.xp.bridge.MethodHook
import com.freegang.xpler.xp.bridge.MethodHookImpl
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Field
import java.lang.reflect.Method

//Method
fun Method.hook(block: MethodHook.() -> Unit): Unit {
    val methodHookImpl = MethodHookImpl(this)
    block.invoke(methodHookImpl)
    methodHookImpl.start()
}

fun Method.call(obj: Any, vararg args: Any): Any? {
    return XposedHelpers.callMethod(obj, this.name, *args)
}

//Object
fun Any.findMethod(methodName: String, vararg args: Any): Method? {
    return try {
        XposedHelpers.findMethodExact(this::class.java, methodName, *args)
    } catch (e: Exception) {
        null
    }
}

fun Any.findMethod(methodName: String, argsTypes: Array<Class<*>>): Method? {
    return try {
        XposedHelpers.findMethodExact(this::class.java, methodName, *argsTypes)
    } catch (e: Exception) {
        null
    }
}

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

fun <T> Any.callMethod(methodName: String, vararg args: Any): T? {
    return XposedHelpers.callMethod(this, methodName, *args) as T?
}

fun <T> Any.callMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    return XposedHelpers.callMethod(this, methodName, *argsTypes, *args) as T?
}

fun <T> Any.callStaticMethod(methodName: String, vararg args: Any): T? {
    return XposedHelpers.callStaticMethod(this::class.java, methodName, *args) as T?
}

fun <T> Any.callStaticMethod(methodName: String, argsTypes: Array<Class<*>>, vararg args: Any): T? {
    return XposedHelpers.callStaticMethod(this::class.java, methodName, *argsTypes, *args) as T?
}

fun <T> Any.getObjectField(fieldName: String): T? {
    return XposedHelpers.getObjectField(this, fieldName) as T?
}

fun Any.setObjectField(fieldName: String, value: Any) {
    XposedHelpers.setObjectField(this, fieldName, value)
}

fun <T> Any.getStaticObjectField(fieldName: String): T? {
    return XposedHelpers.getStaticObjectField(this::class.java, fieldName) as T?
}

fun Any.setStaticObjectField(fieldName: String, value: Any) {
    XposedHelpers.setStaticObjectField(this::class.java, fieldName, value)
}

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

//Class
fun Class<*>.hookConstructor(
    vararg argsTypes: Any,
    block: ConstructorHook.() -> Unit,
): KtXposedHelpers {
    return KtXposedHelpers
        .hookClass(this)
        .constructor(*argsTypes) { block.invoke(this) }
}

fun Class<*>.hookMethod(
    methodName: String,
    vararg argsTypes: Any,
    block: MethodHook.() -> Unit,
): KtXposedHelpers {
    return KtXposedHelpers
        .hookClass(this)
        .method(methodName, *argsTypes) { block.invoke(this) }
}


//ClassLoader
fun ClassLoader.hookClass(clazz: Class<*>): KtXposedHelpers {
    return KtXposedHelpers.hookClass(clazz.name, this)
}

fun ClassLoader.hookClass(className: String): KtXposedHelpers {
    return KtXposedHelpers.hookClass(className, this)
}

fun ClassLoader.findClass(className: String): Class<*> {
    return XposedHelpers.findClass(className, this)
}

//Xposed
fun XC_LoadPackage.LoadPackageParam.hookClass(clazz: Class<*>): KtXposedHelpers {
    return KtXposedHelpers.hookClass(clazz.name, this.classLoader)
}

fun XC_LoadPackage.LoadPackageParam.hookClass(className: String): KtXposedHelpers {
    return KtXposedHelpers.hookClass(className, this.classLoader)
}

fun XC_LoadPackage.LoadPackageParam.findClass(className: String): Class<*> {
    return XposedHelpers.findClass(className, this.classLoader)
}

//其他扩展
private var mModulePath: String? = null
private var mModuleRes: XModuleResources? = null
fun KtXposedHelpers.Companion.initModule(modulePath: String, moduleRes: XModuleResources) {
    mModulePath = modulePath
    mModuleRes = moduleRes
}

fun KtXposedHelpers.Companion.getModulePath() = mModulePath!!
fun KtXposedHelpers.Companion.getModuleRes() = mModuleRes!!
