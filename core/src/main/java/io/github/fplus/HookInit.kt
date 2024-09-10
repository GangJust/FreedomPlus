package io.github.fplus

import android.app.Application
import io.github.fplus.core.hook.DouYinMain
import io.github.xpler.XplerEntrance
import io.github.xpler.core.entrance.ApplicationHookStart
import io.github.xpler.core.proxy.LoadParam

class HookInit : XplerEntrance(), ApplicationHookStart {
    override val modulePackage: String
        get() = Constant.modulePackage

    override val scopes: Set<ApplicationHookStart.Scope>
        get() = Constant.scopes

    override fun onCreateBefore(lparam: LoadParam, hostApp: Application) {
        //
        // injectClassLoader(lparam,hostApp.classLoader)
    }

    override fun onCreateAfter(lparam: LoadParam, hostApp: Application) {
        DouYinMain(hostApp)
    }
}