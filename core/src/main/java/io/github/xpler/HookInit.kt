package io.github.xpler

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.getStaticObjectField
import io.github.xpler.core.hookClass
import io.github.xpler.core.interfaces.IXplerHook
import io.github.xpler.core.thisApplication

// Hook init entrance
class HookInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val hookMain: IXplerHook = HookMain()

    override fun initZygote(sparam: IXposedHookZygoteInit.StartupParam) {
        KtXposedHelpers.initModule(sparam.modulePath)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // compare package name
        if (!HookConfig.allPackageNames.contains(lpparam.packageName)) {
            return
        }

        // set global lpparam
        KtXposedHelpers.setLpparam(lpparam)

        // init module status
        if (lpparam.packageName == HookConfig.modulePackageName) {
            moduleInit(lpparam)
            return
        }

        // all application
        // lpparam.hookClass(Application::class.java)
        //     .method("onCreate") {
        //         onBefore {
        //             hookMain.handleLoadPackage(lpparam, thisApplication)
        //         }
        //     }

        // only host application
        lpparam.hookClass(HookConfig.hostApplicationName)
            .method("onCreate") {
                onBefore {
                    hookMain.onBefore(lpparam, thisApplication)
                }
                onAfter {
                    hookMain.onAfter(lpparam, thisApplication)
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