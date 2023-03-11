package com.freegang.xpler.utils.app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/// 相册工具类
object KAlbumUtils {

    /**
     * 刷新相册, 通知相册更新部分照片文件
     * @param context context
     * @param path 保存路径, 可能会限制: Environment.DIRECTORY_DCIM、Environment.DIRECTORY_PICTURES 等文件夹(作为父文件夹)
     * @param callback 回调方法, 刷新成功才进行回调
     */
    @JvmStatic
    fun refresh(context: Context, path: String, callback: ((path: String, uri: Uri) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(context, arrayOf(path), null) { resultPath, uri ->
                callback?.invoke(resultPath, uri)
            }
        } else {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, path)
            values.put(MediaStore.Images.Media.MIME_TYPE, "*/*")
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

            if (uri != null) callback?.invoke(path, uri) //插入成功才回调方法
        }
    }
}