package io.github.fplus.core.helper

import android.content.Context
import com.freegang.extension.findMethodInvoke
import io.github.xpler.core.XplerLog
import java.lang.reflect.Modifier

object AutoPlayHelper {

    fun openAutoPlay(context: Context) {
        runCatching {
            val controllerGetFiled =
                DexkitBuilder.autoPlayControllerClazz?.fields?.firstOrNull { Modifier.isStatic(it.modifiers) }
            val controllerGet = controllerGetFiled?.get(null)
            val controller = controllerGet?.findMethodInvoke<Any>(context) {
                parameterTypes(listOf(Context::class.java))
            }

            // Open
            controller?.findMethodInvoke<Any>(controller, null, 1, null) {
                parameterTypes(listOf(controller::class.java, String::class.java, Int::class.java, Any::class.java))
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }
}