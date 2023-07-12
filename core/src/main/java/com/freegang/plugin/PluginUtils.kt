package com.freegang.plugin

import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * 单个类的代理处理
 */
fun proxySingle(
    instance: Any,
    loader: ClassLoader,
    clazz: Class<*>,
    block: (proxy: Any, method: Method, args: Array<Any>?) -> Unit,
): Any {
    if (!clazz.isInstance(instance)) {
        throw IllegalArgumentException("`$instance` should be the implementation class of `$clazz`.")
    }

    return Proxy.newProxyInstance(loader, arrayOf(clazz)) { proxy, method, args ->
        block.invoke(proxy, method, args)
        val result = method.invoke(instance, *(args ?: emptyArray()))
        if (method.genericReturnType == Void.TYPE) Unit else result
    }
}