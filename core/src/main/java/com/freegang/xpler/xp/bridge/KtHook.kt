package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XC_MethodHook
import java.lang.reflect.Member

/// 扩展接口
interface KtHook {

}

/// 对普通方法的 Hook 封装
interface MethodHook : KtHook {
    fun onBefore(block: XC_MethodHook.MethodHookParam.() -> Unit)
    fun onAfter(block: XC_MethodHook.MethodHookParam.() -> Unit)
    fun onReplace(block: XC_MethodHook.MethodHookParam.() -> Any)
    fun onUnhook(block: (hookMethod: Member, callback: XC_MethodHook) -> Unit)
}

/// 对构造方法的Hook
interface ConstructorHook : MethodHook {

}