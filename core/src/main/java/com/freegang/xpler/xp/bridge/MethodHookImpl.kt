package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Member

/// 实现类
open class MethodHookImpl(private var method: Member) : MethodHook {
    private var beforeBlock: (XC_MethodHook.MethodHookParam.() -> Unit)? = null
    private var afterBlock: (XC_MethodHook.MethodHookParam.() -> Unit)? = null
    private var replaceBlock: (XC_MethodHook.MethodHookParam.() -> Any)? = null

    private var unhookMap: MutableMap<Member, XC_MethodHook.Unhook> = mutableMapOf()
    private var unHookBlock: ((hookMethod: Member, callback: XC_MethodHook) -> Unit)? = null

    constructor(clazz: Class<*>, methodName: String, vararg argsTypes: Any) :
            this(XposedHelpers.findMethodExact(clazz, methodName, *argsTypes))

    /**
     * [onBefore]会在某个Hook方法执行 之前 被调用
     * 等价于[XC_MethodHook#beforeHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onBefore(block: XC_MethodHook.MethodHookParam.() -> Unit) {
        this.beforeBlock = block
    }

    /**
     * [onAfter]会在某个Hook方法执行 之后 被调用
     * 等价于[XC_MethodHook#afterHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onAfter(block: XC_MethodHook.MethodHookParam.() -> Unit) {
        this.afterBlock = block
    }

    /**
     * 该方法会直接 替换 某个Hook方法, 一旦[onReplace]被显示调用, [onBefore]和[onAfter]将不会被响应
     * 等价于[XC_MethodHook#replaceHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onReplace(block: XC_MethodHook.MethodHookParam.() -> Any) {
        this.replaceBlock = block
    }

    /**
     * 该方法会解开某个Hook方法, 如果被书写即 [unHookBlock != null] 某个hook方法则会被解开
     * 该方法在每次[onReplace]、[onBefore]、[onAfter]执行后, 都会被调用
     * 如果不需要在某个Hook方法执行之后解Hook (即表示当前进程下, hook逻辑只被执行一次), 请不要书写该方法
     *
     * @param block deHook代码块
     */
    override fun onUnhook(block: (hookMethod: Member, callback: XC_MethodHook) -> Unit) {
        this.unHookBlock = block
    }

    // 开启hook, 统一执行Hook逻辑
    fun startHook() {
        if (replaceBlock != null) {
            val unhook = XposedBridge.hookMethod(method, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any {
                    val invoke = replaceBlock!!.invoke(param)
                    maybeUnhook(param.method)
                    return invoke
                }
            })
            unhookMap[method] = unhook
        } else {
            val unhook = XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    beforeBlock?.invoke(param)
                    if (afterBlock != null) return
                    maybeUnhook(param.method)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    afterBlock?.invoke(param)
                    maybeUnhook(param.method)
                }
            })
            unhookMap[method] = unhook
        }
    }

    // 解开hook
    fun maybeUnhook(method: Member) {
        if (unHookBlock == null) return
        if (!unhookMap.containsKey(method)) return

        val unhook = unhookMap[method]!!
        unHookBlock!!.invoke(unhook.hookedMethod, unhook.callback)
        unhook.unhook()
        unhookMap.remove(method)
    }
}