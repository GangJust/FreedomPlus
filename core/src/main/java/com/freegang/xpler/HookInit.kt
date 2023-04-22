package com.freegang.xpler

import android.app.Application
import android.content.Context
import com.freegang.xpler.core.KtXposedHelpers
import com.freegang.xpler.core.hookClass
import com.freegang.xpler.loader.HybridClassLoader
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

        val kXposedBridge: Class<*> = XposedBridge::class.java
        if ("de.robv.android.xposed.XposedBridge" != kXposedBridge.name) {
            val className = kXposedBridge.name
            val pkgName = className.substring(0, className.lastIndexOf('.'))
            HybridClassLoader.setObfuscatedXposedApiPackage(pkgName)
        }

        lpparam
            .hookClass(Application::class.java)
            .method("attach", Context::class.java) {
                onAfter {
                    val context = args[0] as Context
                    val targetLoader = context.classLoader
                    injectClassLoader(lpparam, targetLoader)

                    // init module status
                    if (lpparam.packageName == HookPackages.modulePackageName) {
                        moduleInit(lpparam)
                    }

                    // starter hook main
                    hookMain.handleLoadPackage(lpparam)
                    hookMain.handleLoadPackage(lpparam, thisObject as Application)
                }
            }
    }

    private fun injectClassLoader(lpparam: XC_LoadPackage.LoadPackageParam, classLoader: ClassLoader) {
        val fParent = ClassLoader::class.java.declaredFields.first { it.name == "parent" }
        fParent.isAccessible = true
        val mine = HookInit::class.java.classLoader
        val curr = fParent.get(mine) as (ClassLoader?) ?: XposedBridge::class.java.classLoader
        if (!curr::class.java.name.equals(HybridClassLoader::class.java.name)) {
            lpparam.classLoader = HybridClassLoader(classLoader, curr)
            fParent.set(mine, lpparam.classLoader)
        }
    }

    // module status hook!!
    private fun moduleInit(lpparam: XC_LoadPackage.LoadPackageParam) {
        lpparam
            .hookClass(HookStatus::class.java)
            .method("isEnabled") {
                onAfter {
                    result = true
                }
            }
    }
}