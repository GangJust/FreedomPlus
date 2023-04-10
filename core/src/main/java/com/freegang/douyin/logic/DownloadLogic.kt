package com.freegang.douyin.logic

import android.app.Activity
import android.widget.Toast
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.model.DeAweme
import com.freegang.douyin.model.UrlStruct
import com.freegang.webdav.WebDav
import com.freegang.xpler.utils.app.IProgressNotification
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
    private val hook: BaseHook<*>,
    private val activity: Activity,
    private val aweme: Any?,
) {

    companion object {
        private val config: Config = Config.get()
        private var downloadNotifyId = 1
        private var webdav: WebDav = WebDav(config.webDavHost, config.webDavUsername, config.webDavPassword)
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
        val imageUrlList = mutableListOf<UrlStruct>()
        val imagesStructList = aweme.getObjectField<List<Any>>("images")
        imagesStructList?.forEach { imagesStruct ->
            val urlList = imagesStruct.getObjectField<List<String>>("urlList")
            val imageUrlStruct = UrlStruct(urlList ?: listOf())
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
        val items = mutableListOf(if (deAweme.videoUrlList.isNotEmpty()) "视频" else "图片", "背景音乐")
        if (config.isWebDav) {
            if (deAweme.videoUrlList.isNotEmpty()) {
                items.add("视频(WebDav)")
            } else {
                items.add("图片(WebDav)")
            }
            items.add("背景音乐(WebDav)")
        }
        hook.showChoiceDialog(
            activity = activity,
            title = "Freedom+",
            items = items.toTypedArray(),
            onChoice = { _, item, _ ->
                when (item) {
                    "视频" -> downloadVideo(deAweme)
                    "图片" -> downloadImages(deAweme)
                    "背景音乐" -> downloadMusic(deAweme)
                    "视频(WebDav)" -> downloadVideo(deAweme, true)
                    "图片(WebDav)" -> downloadImages(deAweme, true)
                    "背景音乐(WebDav)" -> downloadMusic(deAweme, true)
                }
            }
        )
    }

    /**
     * 下载视频
     * @param deAweme
     */
    private fun downloadVideo(deAweme: DeAweme, isWebDav: Boolean = false) {
        //构建视频文件名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }.plus(".mp4")

        //默认下载路径: `/外置存储器/DCIM/Freedom/video`
        var parentPath = Config.getFreedomDir(activity).child("video")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/video/昵称(账号)`
        if (config.isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        if (config.isNotification) {
            //发送通知
            showDownloadByNotification(deAweme.videoUrlList, parentPath, pureFileName, isWebDav)
        } else {
            showDownloadByDialog(deAweme.videoUrlList, parentPath, pureFileName, isWebDav)
        }
    }

    /**
     * 下载背景音乐
     * @param deAweme
     */
    private fun downloadMusic(deAweme: DeAweme, isWebDav: Boolean = false) {
        //构建背景音乐文件名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }.plus(".mp3")

        //默认下载路径: `/外置存储器/DCIM/Freedom/music`
        var parentPath = Config.getFreedomDir(activity).child("music")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/music/昵称(账号)`
        if (config.isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        if (config.isNotification) {
            showDownloadByNotification(deAweme.musicUrlList, parentPath, pureFileName, isWebDav)
        } else {
            showDownloadByDialog(deAweme.musicUrlList, parentPath, pureFileName, isWebDav)
        }
    }

    /**
     * 下载图片
     * @param deAweme
     */
    private fun downloadImages(deAweme: DeAweme, isWebDav: Boolean = false) {
        //构建图片文件名
        val pureFileName = if (deAweme.desc.isBlank()) {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${System.currentTimeMillis() / 1000}"
        } else {
            "${deAweme.nickname.pureFileName}_${deAweme.shortId}_${deAweme.desc.pureFileName.subMax()}"
        }

        //默认下载路径: `/外置存储器/DCIM/Freedom/picture`
        var parentPath = Config.getFreedomDir(activity).child("picture")

        //如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/picture/昵称(账号)`
        if (config.isOwnerDir) parentPath = parentPath.child("${deAweme.nickname}(${deAweme.shortId})")

        if (config.isNotification) {
            //发送通知
            hook.showDownloadNotification(
                context = activity,
                notifyId = downloadNotifyId++,
                title = pureFileName,
                listener = {
                    //下载逻辑
                    hook.launch {
                        val imageFiles = mutableListOf<File>()
                        var downloadCount = 0 //下载计数器
                        deAweme.imageUrlStructList.forEachIndexed { index, urlStruct ->
                            val downloadFile = File(parentPath.need(), "${pureFileName}_${index + 1}.jpg")
                            val finished = download(urlStruct.urlList.first(), downloadFile, it, "$index/${deAweme.imageUrlStructList.size} %s%%")
                            if (finished) {
                                downloadCount += 1
                                imageFiles.add(downloadFile)
                                KAlbumUtils.refresh(activity, downloadFile.absolutePath)
                            }
                        }

                        if (downloadCount == deAweme.imageUrlStructList.size) {
                            val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                            it.setFinishedText(message)
                            hook.showToast(activity, message)
                        } else {
                            val failCount = deAweme.imageUrlStructList.size - downloadCount
                            it.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                            hook.showToast(activity, "下载成功${downloadCount}, 失败${failCount}!")
                            Toast.makeText(activity, "正在上传WebDav!", Toast.LENGTH_SHORT).show()
                        }

                        //上传WebDav
                        if (isWebDav) {
                            if (imageFiles.isEmpty()) {
                                hook.showToast(activity, "上传WebDav失败, 无法找到已下载的内容!")
                                return@launch
                            }
                            var uploadCount = 0
                            for (image in imageFiles) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            if (uploadCount == imageFiles.size) {
                                hook.showToast(activity, "上传WebDav成功!")
                            } else {
                                hook.showToast(activity, "上传WebDav成功${uploadCount}, 失败${imageFiles.size - uploadCount}!")
                            }
                        }
                    }
                }
            )
        } else {
            //进度条Dialog
            hook.showProgressDialog(
                activity = activity,
                title = "Freedom+",
                listener = { dialog, notify ->
                    //下载逻辑
                    hook.launch {
                        var downloadCount = 0 //下载计数器
                        deAweme.imageUrlStructList.forEachIndexed { index, urlStruct ->
                            val downloadFile = File(parentPath.need(), "${pureFileName}_${index + 1}.jpg")
                            val finished =
                                download(urlStruct.urlList.first(), downloadFile, notify, "$index/${deAweme.imageUrlStructList.size} %s%%")
                            if (finished) {
                                downloadCount += 1
                                KAlbumUtils.refresh(activity, downloadFile.absolutePath)
                            }
                        }

                        dialog.dismiss()
                        if (downloadCount == deAweme.imageUrlStructList.size) {
                            val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                            notify.setFinishedText(message)
                            hook.showToast(activity, message)
                        } else {
                            val failCount = deAweme.imageUrlStructList.size - downloadCount
                            notify.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                            hook.showToast(activity, "下载成功${downloadCount}, 失败${failCount}!")
                            Toast.makeText(activity, "正在上传WebDav!", Toast.LENGTH_SHORT).show()
                        }

                        //上传WebDav
                        if (isWebDav) {
                            val images = (parentPath.listFiles() ?: arrayOf<File>()).filter { it.isFile }
                            if (images.isEmpty()) {
                                hook.showToast(activity, "上传WebDav失败, 无法找到已下载的内容!")
                                return@launch
                            }
                            var uploadCount = 0
                            for (image in images) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            if (uploadCount == images.size) {
                                hook.showToast(activity, "上传WebDav成功!")
                            } else {
                                hook.showToast(activity, "上传WebDav成功${uploadCount}, 失败${images.size - uploadCount}!")
                            }
                        }
                    }
                }
            )
        }
    }

    // 显示下载 Notification
    private fun showDownloadByNotification(
        urlList: List<String>,
        parentPath: File,
        pureFileName: String,
        isWebDav: Boolean = false,
    ) {
        //发送通知
        hook.showDownloadNotification(
            context = activity,
            notifyId = downloadNotifyId++,
            title = pureFileName,
            listener = {
                //下载逻辑
                hook.launch {
                    val downloadFile = File(parentPath.need(), pureFileName)
                    val finished = download(urlList.first(), downloadFile, it, "下载中 %s%%")
                    if (finished) {
                        val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                        it.setFinishedText(message)
                        hook.showToast(activity, message)
                        KAlbumUtils.refresh(activity, downloadFile.absolutePath)

                        //上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(downloadFile)
                            hook.showToast(activity, "上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                        }
                    } else {
                        it.setFinishedText("下载失败!")
                        hook.showToast(activity, "下载失败!")
                    }
                }
            }
        )
    }

    // 显示下载 Dialog
    private fun showDownloadByDialog(
        urlList: List<String>,
        parentPath: File,
        pureFileName: String,
        isWebDav: Boolean = false,
    ) {
        //进度条Dialog
        hook.showProgressDialog(
            activity = activity,
            title = "Freedom+",
            listener = { dialog, notify ->
                //下载逻辑
                hook.launch {
                    val downloadFile = File(parentPath.need(), pureFileName)
                    val finished = download(urlList.first(), downloadFile, notify, "%s%%")
                    if (finished) {
                        val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                        dialog.dismiss()
                        notify.setFinishedText(message)
                        hook.showToast(activity, message)
                        KAlbumUtils.refresh(activity, downloadFile.absolutePath)

                        //上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(downloadFile)
                            hook.showToast(activity, "上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                        }
                    } else {
                        dialog.dismiss()
                        notify.setFinishedText("下载失败!")
                        hook.showToast(activity, "下载失败!")
                    }
                }
            }
        )
    }

    /**
     * 下载至本地
     */
    private suspend fun download(
        url: String,
        downloadFile: File,
        notify: IProgressNotification,
        progressText: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val outputStream = FileOutputStream(downloadFile)
            var finished = false
            KHttpUtils.download(url, outputStream) { real, total, isInterrupt ->
                activity.runOnUiThread { notify.notifyProgress(real * 100 / total, progressText) }
                if (isInterrupt) finished = false
                if (real >= total) finished = true
            }
            finished
        }
    }

    /**
     * 上传至WebDav
     */
    private suspend fun uploadToWebDav(file: File): Boolean {
        return try {
            val directoryName = file.parentFile!!.absolutePath.substringAfter("Freedom")
            webdav.createDirectory(directoryName = directoryName, parentPath = "Freedom", true)
            webdav.put(file = file, "Freedom".plus(directoryName))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}