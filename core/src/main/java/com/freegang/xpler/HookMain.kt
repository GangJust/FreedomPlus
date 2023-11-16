package com.freegang.xpler

import android.app.Application
import com.freegang.hook.DouYinMain
import com.freegang.xpler.core.interfaces.IXplerXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

open class HookMain : IXplerXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam, application: Application) {
        when (lpparam.packageName) {
            HookPackages.douYinPackageName -> {
                if (DouYinMain.awemeHostApplication.isInstance(application)) {
                    DouYinMain(application)
                }
            }

            HookPackages.douYinLitePackageName -> {
                if (DouYinMain.awemeHostApplication.isInstance(application)) {
                    DouYinMain(application)
                }
            }

            HookPackages.douYinLivePackageName -> {
                if (DouYinMain.awemeHostApplication.isInstance(application)) {
                    DouYinMain(application)
                }
            }

            HookPackages.douYinClonePackageName,
            HookPackages.douYinClone1PackageName -> {
                if (DouYinMain.awemeHostApplication.isInstance(application)) {
                    DouYinMain(application)
                }
            }
        }
    }
}