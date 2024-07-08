package io.github.fplus.core.helper

import android.app.Activity
import android.content.Context
import com.freegang.extension.findFieldGetValue
import com.freegang.extension.findMethodInvoke
import com.ss.android.ugc.aweme.video.simplayer.ISimPlayer
import io.github.xpler.core.findClass
import io.github.xpler.core.log.XplerLog
import io.github.xpler.core.lpparam
import io.github.xpler.loader.hostClassloader

// 暂存
@ExperimentalStdlibApi
object SpeedPlayHelper {

    // 30.5.0_300501
    @ExperimentalStdlibApi
    fun setSpeed(speed: Float) {
        runCatching {
            val helperClazz = lpparam.findClass("X.0BRk") // future
            val helper = helperClazz.findFieldGetValue<Any> { type(helperClazz) }
            val simPlayer = helper?.findMethodInvoke<ISimPlayer> { returnType(ISimPlayer::class.java) }
            simPlayer?.setSpeed(speed)
        }.onFailure {
            XplerLog.e(it)
        }
    }

    // 30.5.0_300501
    @ExperimentalStdlibApi
    fun setSpeed(context: Context, aid: String, fragmentTag: String, speed: Float) {
        XplerLog.i("aid: $aid", "fragmentTag: $fragmentTag", "speed: $speed")

        val speedViewModelClazz = lpparam.findClass("X.0BUf", hostClassloader) // future
        val activityUtilClazz = lpparam.findClass("com.bytedance.bdp.bdpbase.util.ActivityUtil", hostClassloader)
        val tripleClazz = lpparam.findClass("kotlin.Triple", hostClassloader)

        val activityIfNecessary = activityUtilClazz.findMethodInvoke<Activity>(context) { name("getActivityIfNecessary") }
        if (activityIfNecessary != null) {
            val viewModel = getViewModel(activityIfNecessary, speedViewModelClazz)
            val liveData = viewModel?.findFieldGetValue<Any> { name("LIZ") } // future

            val triple = tripleClazz
                .getConstructor(Any::class.java, Any::class.java, Any::class.java)
                .newInstance(aid, fragmentTag, speed)

            liveData?.findMethodInvoke<Any>(triple) { name("setValue") }
        } else {
            XplerLog.e("speed ViewModel not found!")
        }
    }

    // activity must be extends FragmentActivity
    private fun getViewModel(activity: Activity, vm: Class<*>): Any? {
        val viewModelProvidersClazz = lpparam.findClass("androidx.lifecycle.ViewModelProviders", hostClassloader)
        val fragmentActivityClazz = lpparam.findClass("androidx.fragment.app.FragmentActivity", hostClassloader)
        val viewModelProvider = viewModelProvidersClazz.findMethodInvoke<Any>(activity) {
            name("of")
            parameterTypes(listOf(fragmentActivityClazz))
        }
        return viewModelProvider?.findMethodInvoke<Any>(vm) { name("get") }
    }
}