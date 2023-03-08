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
     */
    fun getStoragePath(context: Context): String {
        var externalFilesDir = context.getExternalFilesDir(null) ?: return ""
        do {
            externalFilesDir = externalFilesDir.parentFile ?: return ""
        } while (externalFilesDir.absolutePath.contains("/Android"))

        return externalFilesDir.absolutePath.plus("/")
    }

    val Context.storageRootPath: String
        get() = getStoragePath(this)

    val Context.storageRootFile: File
        get() = getStoragePath(this).toFile()

    val Context.hasOperationStorage: Boolean
        get() {
            try {
                val test = storageRootFile.child(".hasOperationStorage")
                val created = test.createNewFile()
                if (created) return test.delete()
                return true
            } catch (e: Exception) {
                return false
            }
        }
}