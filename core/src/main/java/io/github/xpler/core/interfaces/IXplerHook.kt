package io.github.xpler.core.interfaces

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IXplerHook {

    fun onBefore(
        lpparam: XC_LoadPackage.LoadPackageParam,
        application: Application
    )

    fun onAfter(
        lpparam: XC_LoadPackage.LoadPackageParam,
        application: Application
    )
}