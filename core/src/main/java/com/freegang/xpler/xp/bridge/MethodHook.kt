package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Member

/// 对普通方法的 Hook 封装
interface MethodHook : KtHook {
    fun onBefore(block: XC_MethodHook.MethodHookParam.() -> Unit)
    fun onAfter(block: XC_MethodHook.MethodHookParam.() -> Unit)
    fun onReplace(block: XC_MethodHook.MethodHookParam.() -> Any)
}

/// 实现类
open class MethodHookImpl(private var method: Member) : MethodHook {
    private var beforeBlock: (XC_MethodHook.MethodHookParam.() -> Unit)? = null
    private var afterBlock: (XC_MethodHook.MethodHookParam.() -> Unit)? = null
    private var replaceBlock: (XC_MethodHook.MethodHookParam.() -> Any)? = null

    constructor(clazz: Class<*>, methodName: String, vararg argsTypes: Any) :
            this(XposedHelpers.findMethodExact(clazz, methodName, *argsTypes))


    /**
     * [onBefore]会在某个Hook方法执行 之前 被调用
     *
     * 等价于[XC_MethodHook#beforeHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onBefore(block: XC_MethodHook.MethodHookParam.() -> Unit) {
        this.beforeBlock = block
    }

    /**
     * [onAfter]会在某个Hook方法执行 之后 被调用
     *
     * 等价于[XC_MethodHook#afterHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onAfter(block: XC_MethodHook.MethodHookParam.() -> Unit) {
        this.afterBlock = block
    }

    /**
     * 该方法会直接 替换 某个Hook方法, 一旦[onReplace]被显示调用, [onBefore]和[onAfter]将不会被响应
     *
     * 等价于[XC_MethodHook#replaceHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onReplace(block: XC_MethodHook.MethodHookParam.() -> Any) {
        this.replaceBlock = block
    }

    // 开启hook, 统一执行Hook逻辑
    fun start() {
        if (replaceBlock != null) {
            XposedBridge.hookMethod(method, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any {
                    return this@MethodHookImpl.replaceBlock!!.invoke(param)
                }
            })
        } else {
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    this@MethodHookImpl.beforeBlock?.invoke(param)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    this@MethodHookImpl.afterBlock?.invoke(param)
                }
            })
        }
    }
}