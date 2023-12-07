package io.github.xpler

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.hook.DouYinMain
import io.github.xpler.core.interfaces.IXplerHook

open class HookMain : IXplerHook {
    override fun onBefore(
        lpparam: XC_LoadPackage.LoadPackageParam,
        application: Application,
    ) {
        // Do not write or call the same Hook logic in onBefore and onAfter, as this is meaningless
    }

    override fun onAfter(
        lpparam: XC_LoadPackage.LoadPackageParam,
        application: Application,
    ) {
        DouYinMain(application)
    }
}