package com.freegang.xpler.core.bridge

import com.freegang.ktutils.log.KLogCat
import com.freegang.xpler.core.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Member

/// 实现类
open class MethodHookImpl(private var method: Member) : MethodHook {
    private var beforeBlock: OnBeforeBlock? = null
    private var afterBlock: OnAfterBlock? = null
    private var replaceBlock: OnReplaceBlock? = null

    private var unhookMap: MutableMap<Member, XC_MethodHook.Unhook> = mutableMapOf()
    private var unHookBlock: OnUnhookBlock? = null

    constructor(clazz: Class<*>, methodName: String, vararg argsTypes: Any) :
            this(XposedHelpers.findMethodExact(clazz, methodName, *argsTypes))

    /**
     * [onBefore]会在某个Hook方法执行 之前 被调用
     * 等价于[XC_MethodHook#beforeHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onBefore(block: OnBeforeBlock) {
        this.beforeBlock = block
    }

    /**
     * [onAfter]会在某个Hook方法执行 之后 被调用
     * 等价于[XC_MethodHook#afterHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onAfter(block: OnAfterBlock) {
        this.afterBlock = block
    }

    /**
     * 该方法会直接 替换 某个Hook方法, 一旦[onReplace]被显示调用, [onBefore]和[onAfter]将不会被响应
     * 等价于[XC_MethodHook#replaceHookedMethod]方法
     *
     * @param block hook代码块, 可在内部书写hook逻辑
     */
    override fun onReplace(block: OnReplaceBlock) {
        this.replaceBlock = block
    }

    /**
     * 该方法会解开某个Hook方法, 如果被书写即 [unHookBlock != null] 某个hook方法则会被解开
     * 该方法在每次[onReplace]、[onBefore]、[onAfter]执行后, 都会被调用
     * 如果不需要在某个Hook方法执行之后解Hook (即表示当前进程下, hook逻辑只被执行一次), 请不要书写该方法
     *
     * @param block deHook代码块
     */
    override fun onUnhook(block: OnUnhookBlock) {
        this.unHookBlock = block
    }

    // 开启hook, 统一执行Hook逻辑
    fun startHook() {
        if (replaceBlock != null) {
            val unhook = XposedBridge.hookMethod(method, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any {
                    runCatching {
                        val invoke = replaceBlock!!.invoke(param)
                        maybeUnhook(param.method)
                        return invoke
                    }.onFailure {
                        KLogCat.xposedLog("报错方法: ${param.method}\n错误堆栈: ${it.stackTraceToString()}")
                    }
                    return param.resultOrThrowable
                }
            })
            unhookMap[method] = unhook
        } else {
            val unhook = XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        beforeBlock?.invoke(param)
                        if (afterBlock != null) return
                        maybeUnhook(param.method)
                    }.onFailure {
                        KLogCat.xposedLog("报错方法: ${param.method}\n错误堆栈: ${it.stackTraceToString()}")
                    }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    runCatching {
                        afterBlock?.invoke(param)
                        maybeUnhook(param.method)
                    }.onFailure {
                        KLogCat.xposedLog("报错方法: ${param.method}\n错误堆栈: ${it.stackTraceToString()}")
                    }
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