package com.freegang.xpler

import android.app.Application
import android.content.Context
import android.content.res.XModuleResources
import android.os.Bundle
import com.freegang.xpler.loader.HybridClassLoader
import com.freegang.xpler.xp.KtXposedHelpers
import com.freegang.xpler.xp.hookClass
import com.freegang.xpler.xp.initModule
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

// Hook init entrance
class HookInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val hookMain: HookMain = HookMain()

    override fun initZygote(sparam: IXposedHookZygoteInit.StartupParam) {
        val modulePath = sparam.modulePath
        val moduleRes = XModuleResources.createInstance(sparam.modulePath, null)
        KtXposedHelpers.initModule(modulePath, moduleRes)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!HookPackages.packages.contains(lpparam.packageName)) return

        val kXposedBridge: Class<*> = XposedBridge::class.java
        if ("de.robv.android.xposed.XposedBridge" != kXposedBridge.name) {
            val className = kXposedBridge.name
            val pkgName = className.substring(0, className.lastIndexOf('.'))
            HybridClassLoader.setObfuscatedXposedApiPackage(pkgName)
        }

        KtXposedHelpers
            .hookClass(Application::class.java)
            .method("attach", Context::class.java) {
                onAfter {
                    val context = args[0] as Context
                    val targetLoader = context.classLoader
                    injectClassLoader(lpparam, targetLoader)

                    // starter hook main
                    if (lpparam.packageName == HookPackages.appPackageName) {
                        moduleInit(lpparam)
                    } else {
                        pluginInit(lpparam, thisObject as Application)
                    }
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

    ///
    // module hint hook!!
    private fun moduleInit(lpparam: XC_LoadPackage.LoadPackageParam) {
        val moduleMainActivity = HookPackages.appPackageName.plus(".activity.HomeActivity")
        lpparam.hookClass(moduleMainActivity)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    // 反射调用, 模块加载成功
                    val hookHintMethod = thisObject::class.java.getDeclaredMethod("hookHint")
                    hookHintMethod.isAccessible = true
                    hookHintMethod.invoke(thisObject)
                }
            }
    }

    // starter main hook!!
    private fun pluginInit(lpparam: XC_LoadPackage.LoadPackageParam, application: Application) {
        hookMain.handleLoadPackage(lpparam)
        hookMain.handleLoadPackage(lpparam, application)
    }
}