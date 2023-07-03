package com.freegang.webdav

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class WebDav(
    private val config: Config
) {
    constructor(
        host: String = "",
        username: String = "",
        password: String = "",
    ) : this(
        Config(
            host,
            username,
            password,
        )
    )

    data class Config(
        val host: String = "",
        val username: String = "",
        val password: String = "",
    )

    private val mSardine = OkHttpSardine()

    init {
        mSardine.setCredentials(config.username, config.password)
    }

    /**
     * 某个文件(夹)是否存在
     * @param path 远程路径
     * @param name 文件(夹)名称
     * @param isDirectory 是否文件夹
     */
    suspend fun exists(name: String, path: String = "/", isDirectory: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                if (isDirectory) {
                    mSardine.exists(config.host.plus("/$path/$name/".format()))
                } else {
                    mSardine.exists(config.host.plus("/$path/$name".removeSuffix("/").format()))
                }
            } catch (e: Exception) {
                false //不存在会抛出异常
            }
        }
    }

    /**
     * 创建文件夹, 如果某个文件夹不存在
     * @param parentPath 远程路径
     * @param directoryName 文件夹名称
     */
    suspend fun createDirectory(directoryName: String, parentPath: String = "/", mkdirs: Boolean = false) {
        withContext(Dispatchers.IO) {
            if (!mkdirs) {
                if (exists(parentPath, directoryName, true)) return@withContext
                mSardine.createDirectory(config.host.plus("/$parentPath/$directoryName/".format()))
                return@withContext
            }
            val dirs = directoryName.format().split("/")
            var name = ""
            for (dir in dirs) {
                name = name.plus("/$dir")
                if (exists(name, parentPath, true)) continue
                mSardine.createDirectory(config.host.plus("/$parentPath/$name/".format()))
            }
        }
    }

    /**
     * 删除文件
     * @param path 远程路径
     */
    suspend fun delete(name: String, path: String = "/") {
        withContext(Dispatchers.IO) {
            if (!exists(name, path)) return@withContext
            mSardine.delete(config.host.plus("/$path/$name".format()))
        }
    }

    /**
     * 上传文件
     * @param path 远程路径
     * @param file 被上传的文件
     */
    suspend fun put(file: File, path: String = "/") {
        withContext(Dispatchers.IO) {
            mSardine.put(config.host.plus("/$path/${file.name}".format()), file, null, true)
        }
    }

    /**
     * 上传文件
     * @param path 远程路径
     * @param name 存储的文件名
     * @param bytes 被上传的文件
     */
    suspend fun put(name: String, path: String = "/", bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            mSardine.put(config.host.plus("/$path/${name}".format()), bytes)
        }
    }

    /**
     * 移动文件
     * @param fromFilename 被移动的文件名, 需要是完整的路径加文件名
     * @param toFilename 移动的目标文件名, 需要是完整的路径加文件名
     * @param overwrite 如果目标文件存在, 是否替换
     */
    suspend fun move(fromFilename: String, toFilename: String, overwrite: Boolean) {
        withContext(Dispatchers.IO) {
            mSardine.move(config.host.plus("/$fromFilename".format()), config.host.plus("/$toFilename".format()), overwrite)
        }
    }

    /**
     * 复制文件
     * @param fromFilename 被复制的文件名, 需要是完整的路径加文件名
     * @param toFilename 移动的目标文件名, 需要是完整的路径加文件名
     * @param overwrite 如果目标文件存在, 是否替换
     */
    suspend fun copy(fromFilename: String, toFilename: String, overwrite: Boolean) {
        withContext(Dispatchers.IO) {
            mSardine.copy(config.host.plus("/$fromFilename".format()), config.host.plus("/$toFilename".format()), overwrite)
        }
    }

    /**
     * 获取单个文件信息
     * @param path 远程路径
     * @param filename 文件名
     */
    suspend fun getFile(filename: String, path: String = "/"): DavResource? {
        return withContext(Dispatchers.IO) {
            val list = mSardine.list(config.host.plus("/$path/$filename".format()))
            if (list.isEmpty()) return@withContext null
            return@withContext list.first()
        }
    }

    private fun String.strictPath(): String {
        return this.replace("/{2,}".toRegex(), "/")
    }

    private fun String.urlString(): String {
        return this.replace("+", "%2B")
            .replace(" ", "%20")
            //.replace("/","%2F")
            .replace("?", "%3F")
            .replace("%", "%25")
            .replace("#", "%23")
            .replace("&", "%26")
            .replace("=", "%3D")
    }

    private fun String.format(): String {
        return this.urlString().strictPath()
    }
}