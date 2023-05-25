package com.freegang.xpler.utils.app

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object KAssetsUtils {

    /**
     * 以文本形式打开Assets中的文件并返回内容
     *
     * @param context 上下文对象
     * @param fileName 文件名
     * @return 文件内容的字符串形式
     */
    @JvmStatic
    fun readAsText(context: Context, fileName: String): String {
        return context.assets.open(fileName).readBytes().decodeToString()
    }

    /**
     * 以字节数组形式打开Assets中的文件并返回内容
     *
     * @param context 上下文对象
     * @param fileName 文件名
     * @return 文件内容的字节数组
     */
    @JvmStatic
    fun readAsBytes(context: Context, fileName: String): ByteArray {
        return context.assets.open(fileName).readBytes()
    }

    /**
     * 将 Assets 中的文件解压到临时文件并返回该临时文件对象。
     *
     * @param context 上下文对象
     * @param fileName 文件名
     * @return 解压后的临时文件
     */
    @JvmStatic
    fun extractAssetToFile(context: Context, fileName: String): File {
        val tempDir = context.getExternalFilesDir("assets_temp")
        val tempFile = File(tempDir, fileName)

        try {
            val inputStream = context.assets.open(fileName)
            val outputStream = FileOutputStream(tempFile)

            inputStream.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return tempFile
    }

    /**
     * Assets文件的绝对路径
     */
    @JvmStatic
    val assetsAbsolutePath: String get() = "file:///android_asset/"
}

fun Context.readAssetsAsText(fileName: String): String {
    return KAssetsUtils.readAsText(this, fileName)
}

fun Context.readAssetsAsBytes(fileName: String): ByteArray {
    return KAssetsUtils.readAsBytes(this, fileName)
}