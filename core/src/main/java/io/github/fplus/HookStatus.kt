package io.github.fplus

import android.content.Context
import io.github.xpler.XplerState

object HookStatus {

    val isEnabled
        get() = XplerState.isEnabled

    val framework
        get() = XplerState.framework

    fun isExpActive(context: Context): Boolean {
        return XplerState.isExpActive(context)
    }

    fun isLSPatchActive(context: Context, packageName: String): Array<String> {
        return XplerState.isLSPatchActive(context, packageName)
    }
}