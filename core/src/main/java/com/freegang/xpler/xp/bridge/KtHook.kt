package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XC_MethodHook

/// 扩展接口
interface KtHook {

}

//对普通方法的Hook
interface MethodHook : KtHook {
    fun onBefore(block: (param: XC_MethodHook.MethodHookParam) -> Unit)
    fun onAfter(block: (param: XC_MethodHook.MethodHookParam) -> Unit)
    fun onReplace(block: (param: XC_MethodHook.MethodHookParam) -> Any)
}

//对构造方法的Hook
interface ConstructorHook : MethodHook {

}