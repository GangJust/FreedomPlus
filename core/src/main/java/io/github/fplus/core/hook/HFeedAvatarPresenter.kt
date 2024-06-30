package io.github.fplus.core.hook

import android.view.View
import android.view.ViewGroup
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.findMethodInvoke
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.profile.model.User
import de.robv.android.xposed.XC_MethodHook
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.entity.OnBefore
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.log.XplerLog

class HFeedAvatarPresenter : BaseHook() {
    companion object {
        const val TAG = "HFeedAvatarPresenter"
    }

    private val config
        get() = ConfigV1.get()

    private var clickAgain: Boolean = false

    override fun setTargetClass(): Class<*> {
        return DexkitBuilder.feedAvatarPresenterClazz ?: NoneHook::class.java
    }

    @OnBefore
    fun onClickBefore(
        params: XC_MethodHook.MethodHookParam,
        view: View?,
    ) {
        hookBlockRunning(params) {
            if (!config.isPreventAccidentalTouch)
                return

            if (view !is ViewGroup)
                return

            val aweme = thisObject.findFieldGetValue<Aweme> { type(Aweme::class.java) } ?: return

            val user = thisObject.findMethodInvoke<User>(aweme) {
                returnType(User::class.java)
                parameterTypes(listOf(Aweme::class.java))
            } ?: return

            // KLogCat.d("followStatus: ${user.followStatus}")
            if (user.followStatus == 1) // 已关注
                return

            val nodeInfo = view.createAccessibilityNodeInfo()
            if (!clickAgain && "${nodeInfo.contentDescription}".contains("关注")) {
                showMessageDialog(
                    context = view.context,
                    title = "温馨提示",
                    content = "你点击了关注，是否关注该博主？",
                    cancel = "取消",
                    confirm = "确定",
                    onConfirm = {
                        clickAgain = true
                        view.performClick()
                    },
                )

                result = Void.TYPE
            }

            clickAgain = false
        }.onFailure {
            XplerLog.tagE(TAG, it)
        }
    }
}