package io.github.fplus.core.hook

import android.app.Activity
import android.view.ViewGroup
import com.freegang.ktutils.log.KLogCat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.EmptyHook
import io.github.xpler.core.KeepParam
import io.github.xpler.core.NoneHook
import io.github.xpler.core.OnBefore
import io.github.xpler.core.Param
import io.github.xpler.core.hookBlockRunning
import org.json.JSONObject

class HPoiCreateInstanceImpl(lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook<EmptyHook>(lpparam) {
    companion object {
        const val TAG = "HPoiCreateInstanceImpl"
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.poiCreateInstanceImplClazz ?: NoneHook::class.java
    }

    @OnBefore
    fun testBefore(
        params: XC_MethodHook.MethodHookParam,
        @KeepParam obj: Any?,
        @Param("com.ss.android.ugc.aweme.poi.anchor.AnchorType") anchorType: Any?,
        parent: ViewGroup?,
        eventType: String?,
        activity: Activity?,
        @KeepParam obj1: Any?,
        json: JSONObject?,
    ) {
        hookBlockRunning(params) {
            result = null
        }.onFailure {
            KLogCat.tagE(TAG, it)
        }
    }
}