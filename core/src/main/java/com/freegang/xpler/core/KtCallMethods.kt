package com.freegang.xpler.core

import de.robv.android.xposed.XC_MethodHook

interface KtCallMethods {

    /**
     * 该方法会在Hook目标类所有成员方法调用之前，都被执行
     * @param param XC_MethodHook.MethodHookParam
     */
    fun callOnBeforeMethods(param: XC_MethodHook.MethodHookParam)

    /**
     * 该方法会在Hook目标类所有成员方法调用之后，都被执行
     * @param param XC_MethodHook.MethodHookParam
     */
    fun callOnAfterMethods(param: XC_MethodHook.MethodHookParam)

    /**
     * 该方法会在Hook目标类所有构造方法调用之前，都被执行
     * @param param XC_MethodHook.MethodHookParam
     */
    fun callOnBeforeConstructors(param: XC_MethodHook.MethodHookParam)

    /**
     * 该方法会在Hook目标类所有构造方法调用之后，都被执行
     * @param param XC_MethodHook.MethodHookParam
     */
    fun callOnAfterConstructors(param: XC_MethodHook.MethodHookParam)
}