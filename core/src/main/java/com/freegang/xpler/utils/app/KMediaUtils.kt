package com.freegang.xpler.utils.app

import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore

object KMediaUtils {
    /**
     * 获取图像真实路径
     *
     * @param context 上下文对象
     * @param uri     图像的 content:// URI
     * @return 图像的真实路径，如果无法获取或出现错误则返回 null
     */
    @JvmStatic
    fun getImageRealPath(context: Context, uri: Uri): String? {
        return getMediaRealPath(context, uri)
    }

    /**
     * 获取视频真实路径
     *
     * @param context 上下文对象
     * @param uri     视频的 content:// URI
     * @return 视频的真实路径，如果无法获取或出现错误则返回 null
     */
    @JvmStatic
    fun getVideoRealPath(context: Context, uri: Uri): String? {
        return getMediaRealPath(context, uri)
    }

    /**
     * 获取音频真实路径
     *
     * @param context 上下文对象
     * @param uri     音频的 content:// URI
     * @return 音频的真实路径，如果无法获取或出现错误则返回 null
     */
    @JvmStatic
    fun getAudioRealPath(context: Context, uri: Uri): String? {
        return getMediaRealPath(context, uri)
    }

    /**
     * 获取媒体真实路径
     *
     * @param context 上下文对象
     * @param uri     content:// URI
     * @return 媒体的真实路径，如果无法获取或出现错误则返回 null
     */
    @JvmStatic
    fun getMediaRealPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * 通知相册更新部分照片、视频等文件
     * @param context context
     * @param path 保存路径, 可能会限制: Environment.DIRECTORY_DCIM、Environment.DIRECTORY_PICTURES 等文件夹(作为父文件夹)
     * @param callback 回调方法, 刷新成功才进行回调
     */
    @JvmStatic
    @JvmOverloads
    fun notifyGallery(context: Context, path: String, callback: ((path: String, uri: Uri) -> Unit)? = null) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(path),
            null,
        ) { resultPath, uri ->
            callback?.invoke(resultPath, uri)
        }
    }
}

fun Context.getImageRealPath(uri: Uri): String? = KMediaUtils.getImageRealPath(this, uri)

fun Context.getVideoRealPath(uri: Uri): String? = KMediaUtils.getVideoRealPath(this, uri)

fun Context.getAudioRealPath(uri: Uri): String? = KMediaUtils.getAudioRealPath(this, uri)

fun Context.getMediaRealPath(uri: Uri): String? = KMediaUtils.getMediaRealPath(this, uri)