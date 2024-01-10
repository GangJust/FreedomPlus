package io.github.fplus

import android.content.Context
import io.github.xpler.HookState

object HookStatus {

    val isEnabled
        get() = HookState.isEnabled

    val framework
        get() = HookState.framework

    fun isExpActive(context: Context): Boolean {
        return HookState.isExpActive(context)
    }

    fun isLSPatchActive(context: Context, packageName: String): Array<String> {
        return HookState.isLSPatchActive(context, packageName)
    }
}