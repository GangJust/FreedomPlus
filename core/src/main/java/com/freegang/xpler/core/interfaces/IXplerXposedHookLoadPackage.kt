package com.freegang.xpler.core.interfaces

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IXplerXposedHookLoadPackage /*: IXposedHookLoadPackage*/ {

    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam, application: Application)
}