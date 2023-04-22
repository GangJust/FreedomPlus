package com.freegang.xpler.core.bridge

import de.robv.android.xposed.XposedHelpers


/// 实现类
class ConstructorHookImpl(clazz: Class<*>, vararg argsTypes: Any) :
    MethodHookImpl(XposedHelpers.findConstructorExact(clazz, *argsTypes)), ConstructorHook {

}