package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import com.freegang.extension.asOrNull
import com.freegang.extension.findField
import com.freegang.extension.isDarkMode
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.helper.DexkitBuilder
import io.github.fplus.drawable.shapeDrawable
import io.github.xpler.core.XplerLog
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

class HConversationFragment : BaseHook() {
    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.conversationFragmentClazz ?: NoneHook::class.java
    }

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(
        params: MethodParam,
        view: View,
        bundle: Bundle?,
    ) {
        hookBlockRunning(params) {
            val views = thisObject?.findField { type(View::class.java) }
                ?.getValues(thisObject)
                ?.asOrNull<List<View?>>() ?: emptyList()

            if (view.context.isDarkMode) {
                views.firstOrNull {
                    it?.javaClass?.name?.contains("ConstraintLayout") == true
                }?.background = shapeDrawable {
                    solid("#ff161616")
                    cornerOnly(topLeft = 8f, topRight = 8f)
                }
            }

        }.onFailure {
            XplerLog.e(it)
        }
    }
}