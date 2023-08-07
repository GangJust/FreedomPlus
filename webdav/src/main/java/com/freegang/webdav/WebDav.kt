package com.freegang.webdav

import android.util.Log
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File


class WebDav(
    private val config: Config,
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

    private val mSardine by lazy { OkHttpSardine() }

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
                val url = "${config.host}/$path/$name/".formatUrl(isDirectory)
                //Log.d(TAG, "校验: $url")
                mSardine.exists(url)
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
                val url = "${config.host}/$parentPath/$directoryName/".formatUrl(true)
                Log.d(TAG, "创建: $url")
                mSardine.createDirectory(url)
                return@withContext
            }
            val dirs = directoryName.formatUrl(true).split("/")
            var name = ""
            for (dir in dirs) {
                name = name.plus("/$dir")
                if (exists(name, parentPath, true)) continue
                val url = "${config.host}/$parentPath/$name/".formatUrl(true)
                Log.d(TAG, "创建: $url")
                mSardine.createDirectory(url)
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
            val url = "${config.host}/$path/$name".formatUrl(false)
            Log.d(TAG, "删除: $url")
            mSardine.delete(url)
        }
    }

    /**
     * 上传文件
     * @param path 远程路径
     * @param file 被上传的文件
     */
    suspend fun put(file: File, path: String = "/") {
        withContext(Dispatchers.IO) {
            val url = "${config.host}/$path/${file.name}".formatUrl(false)
            Log.d(TAG, "上传: $url")
            mSardine.put(url, file, null, true)
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
            val url = "${config.host}/$path/$name".formatUrl(false)
            Log.d(TAG, "上传: $url")
            mSardine.put(url, bytes)
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
            val formUrl = "${config.host}/$fromFilename".formatUrl(false)
            val toUrl = "${config.host}/$toFilename".formatUrl(false)
            Log.d(TAG, "移动从: $formUrl")
            Log.d(TAG, "移动至: $toUrl")
            mSardine.move(formUrl, toUrl, overwrite)
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
            val formUrl = "${config.host}/$fromFilename".formatUrl(false)
            val toUrl = "${config.host}/$toFilename".formatUrl(false)
            Log.d(TAG, "复制从: $formUrl")
            Log.d(TAG, "复制至: $toUrl")
            mSardine.copy(formUrl, toUrl, overwrite)
        }
    }

    /**
     * 获取单个文件信息
     * @param path 远程路径
     * @param filename 文件名
     */
    suspend fun getFile(filename: String, path: String = "/"): DavResource? {
        return withContext(Dispatchers.IO) {
            val url = "${config.host}/$path/$filename".formatUrl(false)
            Log.d(TAG, "获取: $url")
            val list = mSardine.list(url)
            if (list.isEmpty()) return@withContext null
            return@withContext list.first()
        }
    }

    private fun String.urlEncoding(): String {
        return this.replace("+", "%2B")
            .replace(" ", "%20")
            //.replace("/","%2F")
            .replace("?", "%3F")
            .replace("%", "%25")
            .replace("#", "%23")
            .replace("&", "%26")
            .replace("=", "%3D")
    }

    private fun String.formatUrl(isDirectory: Boolean): String {
        val urlEncoding = this.urlEncoding().replace(Regex("/{2,}"), "/").replace(":/", "://")
        val finalUrl = if (isDirectory) {
            urlEncoding.removeSuffix("/").plus("/")
        } else {
            urlEncoding.removeSuffix("/")
        }
        return finalUrl
    }

    companion object {
        const val TAG = "WebDav"
    }


    data class Config(
        val host: String = "",
        val username: String = "",
        val password: String = "",
    ) {
        fun toJson(): String {
            return try {
                JSONObject().apply {
                    put("host", host)
                    put("username", username)
                    put("password", password)
                }.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}