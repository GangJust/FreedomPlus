package io.github.fplus.core.hook.logic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.freegang.extension.child
import com.freegang.extension.need
import com.freegang.extension.pureFileName
import com.freegang.extension.pureName
import com.freegang.extension.secureFilename
import com.freegang.ktutils.app.IProgressNotification
import com.freegang.ktutils.app.KNotifiUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.media.KMediaUtils
import com.freegang.ktutils.net.KHttpUtils
import com.freegang.ktutils.text.KTextUtils
import com.ss.android.ugc.aweme.feed.model.Aweme
import de.robv.android.xposed.XposedBridge
import io.github.fplus.core.base.BaseHook
import io.github.fplus.core.config.ConfigV1
import io.github.webdav.WebDav
import io.github.xpler.core.log.XplerLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/// 下载(视频/图文/音乐)逻辑
class DownloadLogic(
    val hook: BaseHook,
    val context: Context,
    val aweme: Aweme?,
) {

    companion object {
        private val config get() = ConfigV1.get()
        private val webdav: WebDav get() = WebDav(config.webDavConfig)
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
        runCatching {
            if (aweme != null) {
                // 整理内容
                // 如果uniqueId为空, shortId为账号
                mShortId = if (aweme.author.uniqueId.isNullOrEmpty()) {
                    aweme.author.shortId
                } else {
                    aweme.author.uniqueId
                }
                mPureNickname = aweme.author.nickname.pureFileName

                // mOwnerDir: 如果需要按视频创作者单独创建文件夹: `/外置存储器/Download/Freedom/${video|music|picture}/昵称(账号)`
                mOwnerDir = if (config.ownerDir) "${mPureNickname}(${mShortId})" else ""

                // 默认下载路径: `/外置存储器/Download/Freedom/video`
                mVideoParent = ConfigV1.getFreedomDir(context).child("video")

                // 默认下载路径: `/外置存储器/Download/Freedom/music`
                mMusicParent = ConfigV1.getFreedomDir(context).child("music")

                // 默认下载路径: `/外置存储器/Download/Freedom/picture`
                mImageParent = ConfigV1.getFreedomDir(context).child("picture")

                // 构建文件名
                mPureFileName = if (aweme.desc.isNullOrBlank()) {
                    "${mPureNickname}_${mShortId}_${System.currentTimeMillis() / 1000}"
                } else {
                    "${mPureNickname}_${mShortId}_${aweme.desc.pureFileName}"
                }
                showChoiceDialog(aweme)
            } else {
                hook.showToast(context, "未获取到基本信息")
            }
        }.onFailure {
            XplerLog.e(it)
        }
    }

    /**
     * 显示下载选择弹层
     * @param aweme
     */
    private fun showChoiceDialog(aweme: Aweme) {
        val urlList = aweme.getImageUrlList()
        val items = mutableListOf("文案", if (urlList.isEmpty()) "视频" else "图片", "背景音乐")
        if (config.isWebDav) {
            items.add(if (urlList.isEmpty()) "视频(WebDav)" else "图片(WebDav)")
            items.add("背景音乐(WebDav)")
        }
        hook.showInputChoiceDialog(
            context = context,
            title = "Freedom+",
            showInput1 = config.ownerDir,
            input1Hint = "创作者: $mOwnerDir",
            input1DefaultValue = mOwnerDir,
            input2Hint = "文件名: $mPureFileName",
            input2DefaultValue = mPureFileName,
            items = items.toTypedArray(),
            onChoice = { _, owner, filename, item, _ ->
                // 如果有重命名
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
                    "文案" -> copyDesc(aweme)
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
     * 复制文案
     * @param aweme
     */
    private fun copyDesc(aweme: Aweme) {
        if (KTextUtils.isEmpty(aweme.desc)) {
            KToastUtils.show(context, "文案为空或获取失败")
            return
        }
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("视频文案", aweme.desc))
        KToastUtils.show(context, "文案复制成功")
    }

    /**
     * 下载视频
     * @param aweme
     */
    private fun downloadVideo(aweme: Aweme, isWebDav: Boolean = false) {
        val videoUrlList = when (config.videoCoding) {
            "H265" -> aweme.getH265VideoUrlList()
            "H264" -> aweme.getH264VideoUrlList()
            "Auto" -> aweme.getAutoVideoUrlList()
            else -> emptyList()
        }

        if (videoUrlList.isEmpty()) {
            hook.showToast(context, "未获取到视频信息")
            return
        }
        // 构建视频文件名
        mPureFileName = mPureFileName.secureFilename(".mp4")
        if (config.notificationDownload) {
            showDownloadByNotification(videoUrlList, mVideoParent, mPureFileName, isWebDav)
        } else {
            showDownloadByDialog(videoUrlList, mVideoParent, mPureFileName, isWebDav)
        }
    }

    /**
     * 下载背景音乐
     * @param aweme
     */
    private fun downloadMusic(aweme: Aweme, isWebDav: Boolean = false) {
        val musicUrlList = aweme.getMusicUrlList()
        if (musicUrlList.isEmpty()) {
            hook.showToast(context, "未获取到背景音乐")
            return
        }
        // 构建视频文件名
        mPureFileName = mPureFileName.secureFilename(".mp3")
        if (config.notificationDownload) {
            showDownloadByNotification(musicUrlList, mMusicParent, mPureFileName, isWebDav)
        } else {
            showDownloadByDialog(musicUrlList, mMusicParent, mPureFileName, isWebDav)
        }
    }

    /**
     * 下载图片
     * @param aweme
     */
    private fun downloadImages(aweme: Aweme, isWebDav: Boolean = false) {
        val structList = aweme.getImageUrlList()
        if (structList.isEmpty()) {
            hook.showToast(context, "未获取到图片信息")
            return
        }

        if (config.notificationDownload) {
            // 发送通知
            hook.showDownloadNotification(
                context = context,
                notifyId = downloadNotifyId++,
                title = mPureFileName,
                listener = {
                    // 下载逻辑
                    hook.singleLaunchIO(mPureFileName) {
                        val imageFiles = mutableListOf<File>()
                        var downloadCount = 0 // 下载计数器
                        structList.forEachIndexed { index, urlStruct ->
                            val resultFile = downloadFile(
                                url = urlStruct.urlList.first(),
                                downloadFile = File(mImageParent.need(), mPureFileName.secureFilename("_${index + 1}.jpg")),
                                notify = it,
                                progressText = "$index/${aweme.images.size} %s%%",
                            )
                            if (resultFile != null) {
                                downloadCount += 1
                                imageFiles.add(resultFile)
                                KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)
                            }
                        }

                        hook.refresh {
                            if (downloadCount == aweme.images.size) {
                                val message =
                                    if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功, 共${downloadCount}个文件!"
                                it.setFinishedText(message)
                                hook.showToast(context, message)
                            } else {
                                val failCount = aweme.images.size - downloadCount
                                it.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                                hook.showToast(context, "下载成功${downloadCount}, 失败${failCount}!")
                                Toast.makeText(context, "正在上传WebDav!", Toast.LENGTH_SHORT).show()

                            }
                        }


                        // 上传WebDav
                        if (isWebDav) {
                            if (imageFiles.isEmpty()) {
                                hook.refresh {
                                    it.setFinishedText("上传WebDav失败, 无法找到已下载的内容!")
                                    hook.showToast(context, "上传WebDav失败, 无法找到已下载的内容!")
                                }
                                return@singleLaunchIO
                            }
                            var uploadCount = 0
                            for (image in imageFiles) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            hook.refresh {
                                if (uploadCount == imageFiles.size) {
                                    it.setFinishedText("上传WebDav成功!")
                                    hook.showToast(context, "上传WebDav成功!")
                                } else {
                                    it.setFinishedText("上传WebDav成功${uploadCount}, 失败${imageFiles.size - uploadCount}!")
                                    hook.showToast(
                                        context,
                                        "上传WebDav成功${uploadCount}, 失败${imageFiles.size - uploadCount}!"
                                    )
                                }
                            }
                        }
                    }
                }
            )
        } else {
            // 进度条Dialog
            hook.showProgressDialog(
                context = context,
                title = "Freedom+",
                listener = { dialog, notify ->
                    // 下载逻辑
                    hook.singleLaunchIO(mPureFileName) {
                        var downloadCount = 0 // 下载计数器
                        structList.forEachIndexed { index, urlStruct ->
                            val resultFile = downloadFile(
                                url = urlStruct.urlList.first(),
                                downloadFile = File(mImageParent.need(), mPureFileName.secureFilename("_${index + 1}.jpg")),
                                notify = notify,
                                progressText = "$index/${aweme.images.size} %s%%",
                            )
                            if (resultFile != null) {
                                downloadCount += 1
                                KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)
                            }
                        }

                        hook.refresh {
                            dialog.dismiss()
                            if (downloadCount == aweme.images.size) {
                                val message =
                                    if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功, 共${downloadCount}个文件!"
                                notify.setFinishedText(message)
                                hook.showToast(context, message)
                            } else {
                                val failCount = aweme.images.size - downloadCount
                                notify.setFinishedText("下载成功${downloadCount}, 失败${failCount}!")
                                hook.showToast(context, "下载成功${downloadCount}, 失败${failCount}!")
                                Toast.makeText(context, "正在上传WebDav!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // 上传WebDav
                        if (isWebDav) {
                            val images = (mImageParent.listFiles() ?: arrayOf<File>()).filter { it.isFile }
                            if (images.isEmpty()) {
                                hook.showToast(context, "上传WebDav失败, 无法找到已下载的内容!")
                                return@singleLaunchIO
                            }
                            var uploadCount = 0
                            for (image in images) {
                                val uploadStatus = uploadToWebDav(image)
                                if (uploadStatus) uploadCount += 1
                            }
                            if (uploadCount == images.size) {
                                hook.showToast(context, "上传WebDav成功!")
                            } else {
                                hook.showToast(
                                    context,
                                    "上传WebDav成功${uploadCount}, 失败${images.size - uploadCount}!"
                                )
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
        // 发送通知
        hook.showDownloadNotification(
            context = context,
            notifyId = downloadNotifyId++,
            title = pureFileName,
            listener = {
                // 下载逻辑
                hook.singleLaunchIO(pureFileName) {
                    val resultFile = downloadFile(
                        url = urlList.first(),
                        downloadFile = File(parentPath.need(), pureFileName),
                        notify = it,
                        progressText = "下载中 %s%%",
                    )
                    if (resultFile != null) {
                        val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                        it.setFinishedText(message)
                        hook.showToast(context, message)
                        KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)

                        // 上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(resultFile)
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
        // 进度条Dialog
        hook.showProgressDialog(
            context = context,
            title = "Freedom+",
            listener = { dialog, notify ->
                // 下载逻辑
                hook.singleLaunchIO(pureFileName) {
                    val resultFile = downloadFile(
                        url = urlList.first(),
                        downloadFile = File(parentPath.need(), pureFileName),
                        notify = notify,
                        progressText = "%s%%",
                    )
                    if (resultFile != null) {
                        hook.refresh { dialog.dismiss() }
                        val message = if (isWebDav) "下载成功, 正在上传WebDav!" else "下载成功!"
                        notify.setFinishedText(message)
                        hook.showToast(context, message)
                        KMediaUtils.notifyMediaUpdate(context, resultFile.absolutePath)

                        // 上传WebDav
                        if (isWebDav) {
                            val uploadStatus = uploadToWebDav(resultFile)
                            hook.showToast(context, "上传WebDav${if (uploadStatus) "成功!" else "失败!"}")
                        }
                    } else {
                        hook.refresh { dialog.dismiss() }
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
    private suspend fun downloadFile(
        url: String,
        downloadFile: File,
        notify: IProgressNotification,
        progressText: String,
    ): File? {
        return withContext(Dispatchers.IO) {
            KHttpUtils.download(
                sourceUrl = url,
                file = downloadFile,
            ) { real, total, e ->
                if (e != null)
                    XplerLog.e(e)

                if (notify is KNotifiUtils.ProgressNotification) { // 通知栏在ui线程中刷新会造成ui卡顿, 排查了一下午, 麻了
                    notify.notifyProgress((real * 100 / total).toInt(), progressText)
                } else {
                    hook.refresh { notify.notifyProgress((real * 100 / total).toInt(), progressText) }
                }
            }
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
            XposedBridge.log(e)
            false
        }
    }
}