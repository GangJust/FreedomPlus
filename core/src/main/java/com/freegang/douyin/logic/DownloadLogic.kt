package com.freegang.douyin.logic

import android.content.Context
import android.widget.Toast
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.webdav.WebDav
import com.freegang.xpler.utils.app.IProgressNotification
import com.freegang.xpler.utils.app.KAlbumUtils
import com.freegang.xpler.utils.io.child
import com.freegang.xpler.utils.io.need
import com.freegang.xpler.utils.io.pureFileName
import com.freegang.xpler.utils.io.pureName
import com.freegang.xpler.utils.io.secureFilename
import com.freegang.xpler.utils.net.KHttpUtils
import com.ss.android.ugc.aweme.feed.model.Aweme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


/// 下载(视频/图文/音乐)逻辑
class DownloadLogic(
    private val hook: BaseHook<*>,
    private val context: Context,
    private val aweme: Aweme?,
) {

    companion object {
        private val config: Config get() = Config.get()
        private val webdav: WebDav by lazy { WebDav(config.webDavHost, config.webDavUsername, config.webDavPassword) }
        private var downloadNotifyId = 1
    }

    private lateinit var mShortId: String
    private lateinit var mPureNickname: String
    private lateinit var mOwnerDir: String

    private lateinit var mVideoParent: File
    private lateinit var mMusicParent: File
    private lateinit var mImageParent: File
    private lateinit var mPureFileName: String

    init {
        if (aweme != null) {
            //整理内容
            mShortId = if (aweme.author.uniqueId.isNullOrEmpty()) aweme.author.shortId else aweme.author.uniqueId //如果uniqueId为空, shortId为账号
            mPureNickname = aweme.author.nickname.pureFileName

            //mOwnerDir: 如果需要按视频创作者单独创建文件夹: `/外置存储器/DCIM/Freedom/${video|music|picture}/昵称(账号)`
            mOwnerDir = if (config.isOwnerDir) "${mPureNickname}(${mShortId})" else ""

            //默认下载路径: `/外置存储器/DCIM/Freedom/video`
            mVideoParent = Config.getFreedomDir(context).child("video")

            //默认下载路径: `/外置存储器/DCIM/Freedom/music`
            mMusicParent = Config.getFreedomDir(context).child("music")

            //默认下载路径: `/外置存储器/DCIM/Freedom/picture`
            mImageParent = Config.getFreedomDir(context).child("picture")

            //构建文件名
            mPureFileName = if (aweme.desc.isNullOrBlank()) {
                "${mPureNickname}_${mShortId}_${System.currentTimeMillis() / 1000}"
            } else {
                "${mPureNickname}_${mShortId}_${aweme.desc.pureFileName.secureFilename}"
            }

            showChoiceDialog(aweme)
        } else {
            hook.showToast(context, "未获取到基本信息")
        }
    }

    private fun getVideoUrlList(aweme: Aweme): List<String> {
        val video = aweme.video ?: return emptyList()
        return video.h264PlayAddr?.urlList ?: video.playAddrH265?.urlList ?: emptyList()
    }

    private fun getMusicUrlList(aweme: Aweme): List<String> {
        val music = aweme.music ?: return emptyList()
        return music.playUrl?.urlList ?: emptyList()
    }

    /**
     * 显示下载选择弹层
     * @param aweme
     */
    private fun showChoiceDialog(aweme: Aweme) {
        val urlList = getVideoUrlList(aweme)
        val items = mutableListOf(if (urlList.isNotEmpty()) "视频" else "图片", "背景音乐")
        if (config.isWebDav) {
            items.add(if (urlList.isNotEmpty()) "视频(WebDav)" else "图片(WebDav)")
            items.add("背景音乐(WebDav)")
        }
        hook.showChoiceDialog(
            context = context,
            title = "Freedom+",
            showInput1 = config.isOwnerDir,
            input1Hint = "创作者: $mOwnerDir",
            input1DefaultValue = mOwnerDir,
            input2Hint = "文件名: $mPureFileName",
            input2DefaultValue = mPureFileName,
            items = items.toTypedArray(),
            onChoice = { _, owner, filename, item, _ ->
                //如果有重命名
                if (owner.isNotBlank()) mOwnerDir = owner
                mVideoParent = mVideoParent.child(mOwnerDir.pureFileName)
                mMusicParent = mMusicParent.child(mOwnerDir.pureFileName)
                mImageParent = mImageParent.child(mOwnerDir.pureFileName)
                if (filename.isNotBlank()) {
                    val tempFile = File(filename)
                    mVideoParent = mVideoParent.child(tempFile.parentFile?.path ?: "")
                    mMusicParent = mMusicParent.child(tempFile.parentFile?.path ?: "")
                    mImageParent = mImageParent.child(tempFile.parentFile?.path ?: "")
                    mPureFileName = tempFile.pureName
                }
                when (item) {
                    "视频" -> downloadVideo(aweme)
                    "图片" -> downloadImages(aweme)
                    "背景音乐" -> downloadMusic(aweme)
                    "视频(WebDav)" -> downloadVideo(aweme, true)
                    "图片(WebDav)" -> downloadImages(aweme, true)
                    "背景音乐(WebDav)" -> downloadMusic(aweme, true)
                }
            }
        )
    }

    /**
     * 下载视频
     * @param aweme
     */
    private fun downloadVideo(aweme: Aweme, isWebDav: Boolean = false) {
        val videoUrlList = getVideoUrlList(aweme)
        if (videoUrlList.isEmpty()) {
            hook.showToast(context, "未获取到视频信息")
            return
        }
        //构建视频文件名
        mPureFileName = mPureFileName.plus(".mp4")
        if (config.isNotification) {
            showDownloadByNotification(videoUrlList, mVideoParent.need(), mPureFileName, isWebDav)
        } else {
            showDownloadByDialog(videoUrlList, mVideoParent.need(), mPureFileName, isWebDav)
        }
    }

    /**
     * 下载背景音乐
     * @param aweme
     */
    private fun downloadMusic(aweme: Aweme, isWebDav: Boolean = false) {
        val musicUrlList = getMusicUrlList(aweme)
        if (musicUrlList.isEmpty()) {
            hook.showToast(context, "未获取到背景音乐")
            return
        }
        //构建视频文件名
        mPureFileName = mPureFileName.plus(".mp3")
        if (config.isNotification) {
            showDownloadByNotification(musicUrlList, mMusicParent.need(), mPureFileName, isWebDav)
        } else {
            showDownloadByDialog(musicUrlList, mMusicParent.need(), mPureFileName, isWebDav)
        }
    }

    /**
     * 下载图片
     * @param aweme
     */
    private fun downloadImages(aweme: Aweme, isWebDav: Boolean = false) {
        val structList = aweme.images ?: emptyList()
        if (structList.isEmpty()) {
            hook.showToast(context, "未获取到图片信息")
            return
        }

        if (config.isNotification) {
            //发送通知
            hook.showDownloadNotification(
                context = context,
                notifyId = downloadNotifyId++,
                title = mPureFileName,
                listener = {
                    //下载逻辑
                    hook.launch {
                        val imageFiles = mutableListOf<File>()
                        var downloadCount = 0 //下载计数器
                        structList.forEachIndexed { index, urlStruct ->
                            val downloadFile = File(mImageParent.need(), "${mPureFileName}_${index + 1}.jpg")
                            val finished = download(urlStruct.urlList.first(), downloadFile, it, "$index/${aweme.images.size} %s%%")
                            if (finished) {
                                downloadCount += 1
                                imageFiles.add(downloadFile)
                                KAlbumUtils.refresh(context, downloadFile.absolutePath)
                            }
                        }

                        if (downloadCount == aweme.images.size) {
                            val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                            it.setFinishedText(message)
                            hook.showToast(context, message)
                        } else {
                            val failCount = aweme.images.size - downloadCount
                            it.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                            hook.showToast(context, "下载成功${downloadCount}, 失败${failCount}!")
                            Toast.makeText(context, "正在上传WebDav!", Toast.LENGTH_SHORT).show()
                        }

                        //上传WebDav
                        if (isWebDav) {
                            if (imageFiles.isEmpty()) {
                                it.setFinishedText("上传WebDav失败, 无法找到已下载的内容!")
                                hook.showToast(context, "上传WebDav失败, 无法找到已下载的内容!")
                                return@launch
                            }
                            var uploadCount = 0
                            for (image in imageFiles) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            if (uploadCount == imageFiles.size) {
                                it.setFinishedText("上传WebDav成功!")
                                hook.showToast(context, "上传WebDav成功!")
                            } else {
                                it.setFinishedText("上传WebDav成功${uploadCount}, 失败${imageFiles.size - uploadCount}!")
                                hook.showToast(context, "上传WebDav成功${uploadCount}, 失败${imageFiles.size - uploadCount}!")
                            }
                        }
                    }
                }
            )
        } else {
            //进度条Dialog
            hook.showProgressDialog(
                context = context,
                title = "Freedom+",
                needMultiple = true,
                listener = { dialog, notify ->
                    //下载逻辑
                    hook.launch {
                        var downloadCount = 0 //下载计数器
                        structList.forEachIndexed { index, urlStruct ->
                            val downloadFile = File(mImageParent.need(), "${mPureFileName}_${index + 1}.jpg")
                            val finished =
                                download(urlStruct.urlList.first(), downloadFile, notify, "$index/${aweme.images.size} %s%%")
                            if (finished) {
                                downloadCount += 1
                                KAlbumUtils.refresh(context, downloadFile.absolutePath)
                            }
                        }

                        dialog.dismiss()
                        if (downloadCount == aweme.images.size) {
                            val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                            notify.setFinishedText(message)
                            hook.showToast(context, message)
                        } else {
                            val failCount = aweme.images.size - downloadCount
                            notify.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                            hook.showToast(context, "下载成功${downloadCount}, 失败${failCount}!")
                            Toast.makeText(context, "正在上传WebDav!", Toast.LENGTH_SHORT).show()
                        }

                        //上传WebDav
                        if (isWebDav) {
                            val images = (mImageParent.listFiles() ?: arrayOf<File>()).filter { it.isFile }
                            if (images.isEmpty()) {
                                hook.showToast(context, "上传WebDav失败, 无法找到已下载的内容!")
                                return@launch
                            }
                            var uploadCount = 0
                            for (image in images) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            if (uploadCount == images.size) {
                                hook.showToast(context, "上传WebDav成功!")
                            } else {
                                hook.showToast(context, "上传WebDav成功${uploadCount}, 失败${images.size - uploadCount}!")
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
            context = context,
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
                        hook.showToast(context, message)
                        KAlbumUtils.refresh(context, downloadFile.absolutePath)

                        //上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(downloadFile)
                            it.setFinishedText("上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                            hook.showToast(context, "上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                        }
                    } else {
                        it.setFinishedText("下载失败!")
                        hook.showToast(context, "下载失败!")
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
            context = context,
            title = "Freedom+",
            needMultiple = true,
            listener = { dialog, notify ->
                //下载逻辑
                hook.launch {
                    val downloadFile = File(parentPath.need(), pureFileName)
                    val finished = download(urlList.first(), downloadFile, notify, "%s%%")
                    if (finished) {
                        val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                        dialog.dismiss()
                        notify.setFinishedText(message)
                        hook.showToast(context, message)
                        KAlbumUtils.refresh(context, downloadFile.absolutePath)

                        //上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(downloadFile)
                            hook.showToast(context, "上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                        }
                    } else {
                        dialog.dismiss()
                        notify.setFinishedText("下载失败!")
                        hook.showToast(context, "下载失败!")
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
                hook.refresh { notify.notifyProgress(real * 100 / total, progressText) }
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