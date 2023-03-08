package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Member

/// 实现类
open class MethodHookImpl(private var method: Member) : MethodHook {
    private var beforeBlock: ((param: XC_MethodHook.MethodHookParam) -> Unit)? = null
    private var afterBlock: ((param: XC_MethodHook.MethodHookParam) -> Unit)? = null
    private var replaceBlock: ((param: XC_MethodHook.MethodHookParam) -> Any)? = null

    constructor(clazz: Class<*>, methodName: String, vararg argsTypes: Any) :
            this(XposedHelpers.findMethodExact(clazz, methodName, *argsTypes))

    /**
     * 在进入某个方法第一行进行注入
     * @param block 注入的代码块
     */
    override fun onBefore(block: (param: XC_MethodHook.MethodHookParam) -> Unit) {
        this.beforeBlock = block
    }

    /**
     * 在结束某个方法最后一行(return之前)进行注入
     * @param block 注入的代码块
     */
    override fun onAfter(block: (param: XC_MethodHook.MethodHookParam) -> Unit) {
        this.afterBlock = block
    }

    /**
     * 对某个方法进行替换
     * @param block 注入的代码块
     */
    override fun onReplace(block: (param: XC_MethodHook.MethodHookParam) -> Any) {
        this.replaceBlock = block
    }

    /**
     * 开启Hook
     */
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