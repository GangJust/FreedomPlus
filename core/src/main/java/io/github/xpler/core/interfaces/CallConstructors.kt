package io.github.xpler.core.interfaces

import de.robv.android.xposed.XC_MethodHook

interface CallConstructors {
    /**
     * 该方法会在Hook目标类所有构造方法调用之前，都被执行
     * @param params XC_MethodHook.MethodHookParam
     */
    fun callOnBeforeConstructors(params: XC_MethodHook.MethodHookParam)

    /**
     * 该方法会在Hook目标类所有构造方法调用之后，都被执行
     * @param params XC_MethodHook.MethodHookParam
     */
    fun callOnAfterConstructors(params: XC_MethodHook.MethodHookParam)
}