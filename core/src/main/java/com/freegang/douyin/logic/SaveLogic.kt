package com.freegang.douyin.logic

import android.content.Context
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KFileUtils.need
import com.freegang.xpler.utils.net.KHttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

//保存(图片/表情)逻辑
class SaveLogic(
    private val hook: BaseHook,
    private val context: Context,
    private val urlList: List<String>,
    private val isDCIM: Boolean = false,
) {

    init {
        onSaveCommentImageOrEmoji(context, urlList)
    }

    // 保存评论区图片
    private fun onSaveCommentImageOrEmoji(context: Context, urlList: List<String>) {
        hook.launch {
            //默认保存路径: `/外置存储器/DCIM/Freedom/emoji`
            val parentPath = Config.getFreedomDir(context)
                .child("emoji")
                .need()

            //构建保存文件名
            val file = File(parentPath, "${System.currentTimeMillis() / 1000}.gif")
            withContext(Dispatchers.IO) {
                KHttpUtils.download(urlList.first(), FileOutputStream(file)) { real, total ->
                    if (real == total) {
                        hook.showToast(context, "保存成功!")
                        hook.vibrate(context, 100L)
                    }
                }
            }
        }
    }
}