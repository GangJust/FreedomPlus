package com.freegang.douyin.logic

import android.app.Activity
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.model.DeAweme
import com.freegang.douyin.model.ImageUrlStruct
import com.freegang.xpler.utils.app.KAlbumUtils
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KFileUtils.need
import com.freegang.xpler.utils.io.KFileUtils.pureFileName
import com.freegang.xpler.utils.io.KFileUtils.subMax
import com.freegang.xpler.utils.net.KHttpUtils
import com.freegang.xpler.xp.getObjectField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/// 下载(视频/图文/音乐)逻辑
class DownloadLogic(
    private val hook: BaseHook,
    private val activity: Activity,
    private val aweme: Any?,
    private val isOwnerDir: Boolean = false,
) {

    companion object {
        private var downloadNotifyId = 1
    }

    init {
        if (aweme is DeAweme) {
            showChoiceDialog(aweme)
        } else {
            deAweme(activity)
        }
    }

    /**
     * 解析Aweme类
     */
    private fun deAweme(activity: Activity) {
        if (aweme == null) {
            hook.showToast(activity, "未获取到基本信息")
            return
        }
        val deAweme = DeAweme()

        //视频描述
        deAweme.desc = aweme.getObjectField<String>("desc") ?: ""

        //用户信息
        val user = aweme.getObjectField<Any>("author")
        deAweme.nickname = user?.getObjectField<String>("nickname") ?: ""
        deAweme.shortId = user?.getObjectField<String>("uniqueId") ?: ""
        if (deAweme.shortId.isEmpty()) deAweme.shortId = user?.getObjectField<String>("shortId") ?: "" //如果uniqueId为空, shortId为账号

        //视频
        deAweme.videoUrlList = aweme
            .getObjectField<Any>("video")
            ?.getObjectField<Any>("h264PlayAddr")
            ?.getObjectField<List<String>>("urlList")
            ?: listOf()

        //图文列表
        val imageUrlList = mutableListOf<ImageUrlStruct>()
        val imagesStructList = aweme.getObjectField<List<Any>>("images")
        imagesStructList?.forEach { imagesStruct ->
            val urlList = imagesStruct.getObjectField<List<String>>("urlList")
            val imageUrlStruct = ImageUrlStruct(urlList ?: listOf())
            imageUrlList.add(imageUrlStruct)
        }
        deAweme.imageUrlStructList = imageUrlList

        //背景音乐
        deAweme.musicUrlList = aweme
            .getObjectField<Any>("music")
            ?.getObjectField<Any>("playUrl")
            ?.getObjectField<List<String>>("urlList")
            ?: listOf()


        if (deAweme.isEmpty()) {
            hook.showToast(activity, "未获取到视频信息!")
            return
        }

        showChoiceDialog(deAweme)
    }

    /**
     * 显示下载选择弹层
     * @param deAweme
     */
    private fun showChoiceDialog(deAweme: DeAweme) {
        val items = arrayOf(if (deAweme.videoUrlList.isNotEmpty()) "视频" else "图片", "背景音乐")
        hook.showChoiceDialog(
            activity = activity,
            title = "Freedom+",
            items = items,
            onChoice = { _, item, _ ->
                when (item) {
                    "视频" -> downloadVideo(activity, deAweme)
                    "图片" -> downloadImages(activity, deAweme)
                    "背景音乐" -> downloadMusic(activity, deAweme)
                }
            }
        )
    }

    /**
     * 下载视频
     * @param deAweme
     */
    private fun downloadVideo(activity: Activity, deAweme: DeAweme) {
        //构建视频文件名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }.plus(".mp4")

        //默认下载路径: `/外置存储器/DCIM/Freedom/video`
        var parentPath = Config.getFreedomDir(activity).child("video")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/video/昵称(账号)`
        if (isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        //构建下载文件名
        val videoFile = File(parentPath.need(), pureFileName)

        //发送通知
        hook.showDownloadNotification(
            context = activity,
            notifyId = downloadNotifyId++,
            title = pureFileName,
            listener = {
                //下载逻辑
                hook.launch {
                    withContext(Dispatchers.IO) {
                        //KLogCat.d("RealDownloadLogic#video: ${deAweme.videoUrlList}")
                        val outputStream = FileOutputStream(videoFile)
                        KHttpUtils.download(deAweme.videoUrlList.first(), outputStream) { real, total ->
                            it.notifyProgress(real * 100 / total)
                            if (real == total) {
                                KAlbumUtils.refresh(activity, videoFile.absolutePath) { _, _ ->
                                    it.setFinishedText("下载成功!")
                                    hook.showToast(activity, "下载成功!")
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    /**
     * 下载图片 (标题作为文件夹)
     * @param deAweme
     */
    private fun downloadImages(activity: Activity, deAweme: DeAweme) {
        //构建图片文件夹名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }

        //默认下载路径: `/外置存储器/DCIM/Freedom/picture`
        var parentPath = Config.getFreedomDir(activity).child("picture")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/picture/昵称(账号)`
        if (isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        //构建下载文件名(文件夹)
        val pictureDir = File(parentPath.need(), pureFileName).need()

        //发送通知
        hook.showDownloadNotification(
            context = activity,
            notifyId = downloadNotifyId++,
            title = pureFileName,
            listener = {
                //下载逻辑
                hook.launch {
                    var downloadCount = 0 //下载计数器
                    deAweme.imageUrlStructList.forEachIndexed { index, url ->
                        downloadCount++ //进循环即开始计数, 如果中途有失败, 直接跳过编号, 方便定位
                        withContext(Dispatchers.IO) {
                            //KLogCat.d("RealDownloadLogic#images: ${url.imageUrlList}")
                            val file = File(pictureDir, "${index + 1}.jpg")
                            val outputStream = FileOutputStream(file)
                            KHttpUtils.download(url.imageUrlList.first(), outputStream) { real, total ->
                                it.notifyProgress(real * 100 / total, "$index/${deAweme.imageUrlStructList.size} %s%%")
                                if (real == total) {
                                    KAlbumUtils.refresh(activity, file.absolutePath)
                                }
                            }
                        }
                    }
                    //结束提示
                    if (downloadCount == deAweme.imageUrlStructList.size) {
                        it.setFinishedText("下载成功!")
                        hook.showToast(activity, "下载成功!")
                    } else {
                        it.setFinishedText("成功${downloadCount}, 失败${deAweme.imageUrlStructList.size - downloadCount}!")
                        hook.showToast(activity, "成功${downloadCount}, 失败${deAweme.imageUrlStructList.size - downloadCount}!")
                    }
                }
            }
        )

    }

    /**
     * 下载背景音乐
     * @param deAweme
     */
    private fun downloadMusic(activity: Activity, deAweme: DeAweme) {
        //构建背景音乐文件名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }.plus(".mp3")

        //默认下载路径: `/外置存储器/DCIM/Freedom/music`
        var parentPath = Config.getFreedomDir(activity).child("music")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/music/昵称(账号)`
        if (isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        //构建下载文件名
        val musicFile = File(parentPath.need(), pureFileName)

        //发送通知
        hook.showDownloadNotification(
            context = activity,
            notifyId = downloadNotifyId++,
            title = pureFileName,
            listener = {
                //下载逻辑
                hook.launch {
                    withContext(Dispatchers.IO) {
                        //KLogCat.d("RealDownloadLogic#music: ${deAweme.videoUrlList}")
                        val outputStream = FileOutputStream(musicFile)
                        KHttpUtils.download(deAweme.musicUrlList.first(), outputStream) { real, total ->
                            it.notifyProgress(real * 100 / total)
                            if (real == total) {
                                it.setFinishedText("下载成功!")
                                hook.showToast(activity, "下载成功!")
                            }
                        }
                    }
                }
            }
        )
    }
}