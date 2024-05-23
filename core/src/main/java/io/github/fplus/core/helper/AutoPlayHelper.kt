package io.github.fplus.core.helper

import android.content.Context
import com.freegang.extension.method
import com.freegang.extension.methodInvoke
import java.lang.reflect.Modifier

object AutoPlayHelper {

    fun openAutoPlay(context: Context) {
        val controllerGetFiled = DexkitBuilder.autoPlayControllerClazz?.fields?.firstOrNull { Modifier.isStatic(it.modifiers) }
        val controllerGet = controllerGetFiled?.get(null)
        val controller = controllerGet?.methodInvoke(args = arrayOf(context))
        // Open
        controller?.method(paramTypes = arrayOf(controller::class.java, String::class.java, Int::class.java, Any::class.java))
            ?.invoke(controller, controller, null, 1, null)
    }
}