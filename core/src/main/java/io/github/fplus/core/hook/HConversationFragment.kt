package io.github.fplus.core.hook

import android.os.Bundle
import android.view.View
import com.freegang.extension.asOrNull
import com.freegang.extension.findField
import com.freegang.extension.isDarkMode
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.R
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.KtXposedHelpers
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnAfter
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HConversationFragment : BaseHook() {
    companion object {
        const val TAG = "HConversationFragment"
    }

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.conversationFragmentClazz ?: NoneHook::class.java
    }

    @OnAfter("onViewCreated")
    fun onViewCreatedAfter(params: XC_MethodHook.MethodHookParam, view: View, bundle: Bundle?) {
        hookBlockRunning(params) {
            val views = thisObject.findField { type(View::class.java) }
                .getValues(thisObject)
                .asOrNull<List<View?>>() ?: emptyList()

            if (view.context.isDarkMode) {
                views
                    .firstOrNull {
                        it?.javaClass?.name?.contains("ConstraintLayout") == true
                    }
                    ?.background = KtXposedHelpers.getDrawable(R.drawable.aweme_bottom_panel_night_background)
            }

        }.onFailure {
            XplerLog.e(it)
        }
    }
}