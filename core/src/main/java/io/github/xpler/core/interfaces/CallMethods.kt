package io.github.xpler.core.interfaces

import de.robv.android.xposed.XC_MethodHook

interface CallMethods {
    /**
     * 该方法会在Hook目标类所有成员方法调用之前，都被执行
     * @param params XC_MethodHook.MethodHookParam
     */
    fun callOnBeforeMethods(params: XC_MethodHook.MethodHookParam)

    /**
     * 该方法会在Hook目标类所有成员方法调用之后，都被执行
     * @param params XC_MethodHook.MethodHookParam
     */
    fun callOnAfterMethods(params: XC_MethodHook.MethodHookParam)
}