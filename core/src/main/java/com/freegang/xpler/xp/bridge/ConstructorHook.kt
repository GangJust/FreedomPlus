package com.freegang.xpler.xp.bridge

import de.robv.android.xposed.XposedHelpers

/// 对构造方法的Hook
interface ConstructorHook : MethodHook {

}

/// 实现类
class ConstructorHookImpl(clazz: Class<*>, vararg argsTypes: Any) :
    MethodHookImpl(XposedHelpers.findConstructorExact(clazz, *argsTypes)),
    ConstructorHook {

}