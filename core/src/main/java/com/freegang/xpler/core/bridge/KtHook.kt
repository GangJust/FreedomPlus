package com.freegang.xpler.core.bridge

import de.robv.android.xposed.XC_MethodHook
import java.lang.reflect.Member

/// 扩展方法
typealias OnBeforeBlock = XC_MethodHook.MethodHookParam.() -> Unit
typealias OnAfterBlock = XC_MethodHook.MethodHookParam.() -> Unit
typealias OnReplaceBlock = XC_MethodHook.MethodHookParam.() -> Any
typealias OnUnhookBlock = (hookMethod: Member, callback: XC_MethodHook) -> Unit

/// 接口
interface KtHook {

}

/// 对普通方法的 Hook 封装
interface MethodHook : KtHook {
    fun onBefore(block: OnBeforeBlock)
    fun onAfter(block: OnAfterBlock)
    fun onReplace(block: OnReplaceBlock)
    fun onUnhook(block: OnUnhookBlock)
}

/// 对构造方法的Hook
interface ConstructorHook : MethodHook {

}