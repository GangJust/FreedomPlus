package com.freegang.xpler.xp

import com.freegang.xpler.xp.bridge.ConstructorHook
import com.freegang.xpler.xp.bridge.ConstructorHookImpl
import com.freegang.xpler.xp.bridge.MethodHook
import com.freegang.xpler.xp.bridge.MethodHookImpl
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

/// Xposed hook 的基础封装
class KtXposedHelpers {
    private var clazz: Class<*>? = null

    companion object {
        private val instances = KtXposedHelpers()

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
        constructorHookImpl.start()
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
        methodHookImpl.start()
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
        methodHookImpl.start()
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
            constructorHookImpl.start()
        }
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
            methodHookImpl.start()
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
                methodHookImpl.start()
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
                methodHookImpl.start()
            }
        }
        return this
    }
}