package com.freegang.xpler.utils.io

import android.content.Context
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KFileUtils.toFile
import java.io.File

object KStorageUtils {

    /**
     * 需要部分权限
     * 获取外置存储器的根地址, 通常是: /storage/emulated/0/
     * @param context context
     * @return String
     */
    @JvmStatic
    fun getStoragePath(context: Context): String {
        var externalFilesDir = context.getExternalFilesDir(null) ?: return ""
        do {
            externalFilesDir = externalFilesDir.parentFile ?: return ""
        } while (externalFilesDir.absolutePath.contains("/Android"))

        return externalFilesDir.absolutePath.plus("/")
    }

    /**
     * 需要部分权限
     * 获取外置存储器的根地址, 通常是: /storage/emulated/0/
     * @param context context
     * @return File
     */
    @JvmStatic
    fun getStorageFile(context: Context): File {
        return getStoragePath(context).toFile()
    }

    /**
     * 需要部分权限
     * 在外置存储器的根地址, 通常是: /storage/emulated/0/ 尝试创建和删除一个`.temp`文件, 根据是否创建和删除成功来判断外置存储读写权限
     * @param context context
     * @return File
     */
    @JvmStatic
    fun hasOperationStorage(context: Context): Boolean {
        try {
            val test = getStorageFile(context).child(".temp")
            val created = test.createNewFile()
            if (created || test.exists()) return test.delete()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    val Context.storageRootPath: String
        get() = getStoragePath(this)

    val Context.storageRootFile: File
        get() = getStoragePath(this).toFile()

    val Context.hasOperationStorage: Boolean
        get() = hasOperationStorage(this)
}