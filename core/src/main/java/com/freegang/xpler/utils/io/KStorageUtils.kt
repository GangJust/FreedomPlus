package com.freegang.xpler.utils.io

import android.content.Context
import java.io.File

object KStorageUtils {

    /**
     * 获取外置存储器的根路径。
     * 通常为：/storage/emulated/0/。
     *
     * @param context 上下文
     * @return 外置存储器的根路径
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
     * 获取外置存储器的根文件对象。
     * 通常为：/storage/emulated/0/。
     *
     * @param context 上下文
     * @return 外置存储器的根文件对象
     */
    @JvmStatic
    fun getStorageFile(context: Context): File {
        return getStoragePath(context).toFile()
    }

    /**
     * 检查外置存储器的读写权限。
     * 在外置存储器的根路径下尝试创建和删除一个名为`.temp`的临时文件，
     * 根据创建和删除成功与否来判断外置存储器的读写权限是否可用。
     *
     * @param context 上下文
     * @return 外置存储器的读写权限是否可用
     */
    @JvmStatic
    @Synchronized
    fun hasOperationStorage(context: Context): Boolean {
        return try {
            val test = getStorageFile(context).child(".temp")
            val created = test.createNewFile()
            if (created || test.exists()) test.delete()
            true
        } catch (e: Exception) {
            false
        }
    }
}

///
/**
 * 获取外置存储器的根路径，通常是：/storage/emulated/0/
 */
val Context.storageRootPath: String
    get() = KStorageUtils.getStoragePath(this)

/**
 * 获取外置存储器的根文件对象，通常是：/storage/emulated/0/
 */
val Context.storageRootFile: File
    get() = KStorageUtils.getStoragePath(this).toFile()

/**
 * 检查外置存储器的读写权限
 */
val Context.hasOperationStorage: Boolean
    get() = KStorageUtils.hasOperationStorage(this)
