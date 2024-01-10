package io.github.fplus

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.hook.DouYinMain
import io.github.xpler.HookEntrance
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.wrapper.ApplicationHookStart
import io.github.xpler.core.wrapper.DefaultHookStart

class HookInit : HookEntrance<HookInit>(), ApplicationHookStart {
    override val modulePackage: String
        get() = Constant.modulePackage

    override val scopes: Array<ApplicationHookStart.Scope>
        get() = Constant.scopes

    override fun onCreateBefore(lpparam: XC_LoadPackage.LoadPackageParam, hostApp: Application) {
        //
    }

    override fun onCreateAfter(lpparam: XC_LoadPackage.LoadPackageParam, hostApp: Application) {
        XplerLog.d("Freedom+: starting!!")
        DouYinMain(hostApp)
    }
}