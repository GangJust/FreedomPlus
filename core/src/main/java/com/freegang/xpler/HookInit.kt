package com.freegang.xpler

import android.app.Application
import android.app.Instrumentation
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.getStaticObjectField
import com.freegang.xpler.core.hookClass
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

        lpparam.hookClass(Instrumentation::class.java)
            .method("callApplicationOnCreate", Application::class.java) {
                onAfter {
                    val application = args[0] as Application
                    hookMain.handleLoadPackage(lpparam)
                    hookMain.handleLoadPackage(lpparam, application)
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
                    result = if (bridgeTag.startsWith("LSPosed")) {
                        "LSPosed"
                    } else if (bridgeTag.startsWith("EdXposed")) {
                        "EdXposed"
                    } else if (bridgeTag.startsWith("Xposed")) {
                        "Xposed"
                    } else {
                        "Unknown"
                    }
                }
            }
    }
}