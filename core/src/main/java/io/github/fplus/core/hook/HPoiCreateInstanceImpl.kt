package io.github.fplus.core.hook

import android.app.Activity
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.EmptyHook
import io.github.xpler.core.entity.KeepParam
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.entity.Param
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog
import org.json.JSONObject

class HPoiCreateInstanceImpl : BaseHook<EmptyHook>() {
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
            XplerLog.e(it)
        }
    }
}