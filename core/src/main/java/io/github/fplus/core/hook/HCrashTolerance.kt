package io.github.fplus.core.hook

import com.ss.android.ugc.aweme.feed.model.VideoItemParams
import com.ss.android.ugc.aweme.kiwi.model.QModel
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.helper.DexkitBuilder
import io.github.xpler.core.entity.CallMethods
import io.github.xpler.core.entity.NoneHook
import io.github.xpler.core.hookBlockRunning
import io.github.xpler.core.proxy.MethodParam

/// 崩溃容错，处理官方可能造成的系列崩溃问题
class HCrashTolerance {
    companion object {
        const val TAG = "HCrashTolerance"
    }

    val config get() = ConfigV1.get()

    init {
        HPoiFeed()
        HLivePhoto()
        HTabLanding()
    }

    inner class HPoiFeed : BaseHook(), CallMethods {
        override fun setTargetClass(): Class<*> {
            return findClass("com.ss.android.ugc.aweme.poi.anchor.poi.flavor.PoiFeedAnchor")
        }

        override fun callOnBeforeMethods(params: MethodParam) {

        }

        override fun callOnAfterMethods(params: MethodParam) {
            hookBlockRunning(params) {
                resultOrThrowable
            }.onFailure {
                params.setThrowable(null)
                // KToastUtils.show(KAppUtils.getApplication, "尝试崩溃拦截:${it.message}")
            }
        }
    }

    inner class HLivePhoto : BaseHook() {

        override fun setTargetClass(): Class<*> {
            return DexkitBuilder.livePhotoClazz ?: NoneHook::class.java
        }

        @OnAfter
        fun methodAfter(params: MethodParam, qModel: QModel?) {
            hookBlockRunning(params) {
                resultOrThrowable
            }.onFailure {
                params.setThrowable(null)
                // KToastUtils.show(KAppUtils.getApplication, "尝试崩溃拦截:${it.message}")
            }
        }
    }

    inner class HTabLanding : BaseHook() {

        override fun setTargetClass(): Class<*> {
            return DexkitBuilder.tabLandingClazz ?: NoneHook::class.java
        }

        @OnAfter
        fun methodAfter(params: MethodParam, item: VideoItemParams?) {
            hookBlockRunning(params) {
                resultOrThrowable
            }.onFailure {
                params.setThrowable(null)
                // KToastUtils.show(KAppUtils.getApplication, "尝试崩溃拦截:${it.message}")
            }
        }
    }
}