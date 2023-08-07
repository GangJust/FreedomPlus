package com.freegang.xpler

import android.app.Application
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.getStaticObjectField
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.core.thisApplication
import com.freegang.xpler.loader.injectClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

// Hook init entrance
class HookInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val hookMain: HookMain = HookMain()

    override fun initZygote(sparam: IXposedHookZygoteInit.StartupParam) {
        KtXposedHelpers.initModule(sparam.modulePath)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!HookPackages.packages.contains(lpparam.packageName)) return
        KtXposedHelpers.setLpparam(lpparam)

        // init module status
        if (lpparam.packageName == HookPackages.modulePackageName) {
            moduleInit(lpparam)
        }

        // compatible with TaiChi
        lpparam.hookClass(Application::class.java)
            .method("onCreate") {
                onBefore {
                    injectClassLoader(thisApplication.classLoader)
                    hookMain.handleLoadPackage(lpparam)
                    hookMain.handleLoadPackage(lpparam, thisApplication)
                }
            }
    }

    // module status hook!!
    private fun moduleInit(lpparam: XC_LoadPackage.LoadPackageParam) {
        lpparam.hookClass(HookStatus::class.java)
            .method("isEnabled") {
                onAfter {
                    result = true
                }
            }
            .method("getModuleState") {
                onAfter {
                    val bridgeTag = XposedBridge::class.java.getStaticObjectField<String>("TAG") ?: ""
                    result = if (bridgeTag.contains("LSPosed-Bridge")) {
                        "LSPosed"
                    } else if (bridgeTag.contains("Xposed")) {
                        "Xposed"
                    } else {
                        "Unknown"
                    }
                }
            }
    }
}