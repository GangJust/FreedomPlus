package io.github.fplus.core.hook.logic

import android.content.Context
import com.freegang.extension.child
import com.freegang.extension.need
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.net.KHttpUtils
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import java.io.File

class SaveAudioLogic(
    private val hook: BaseHook,
    private val context: Context,
    private val url: String,
    private val filename: String,
) {
    companion object {
        private val config get() = ConfigV1.get()
    }

    init {
        hook.singleLaunchIO("SaveAudio") {
            // 默认保存路径: `/外置存储器/Download/Freedom/audio`
            val parentPath = ConfigV1.getFreedomDir(context).child("audio").need()

            // 构建保存文件名
            hook.showToast(context, "保存语音, 请稍后..")
            val resultFile = KHttpUtils.download(
                sourceUrl = url,
                file = File(parentPath, "${filename}.mp3"),
            )
            if (resultFile != null) {
                hook.showToast(context, "保存成功!")
                KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)
                if (config.vibrate) hook.vibrate(context, 5L)
            } else {
                hook.showToast(context, "保存失败!")
            }
        }
    }
}