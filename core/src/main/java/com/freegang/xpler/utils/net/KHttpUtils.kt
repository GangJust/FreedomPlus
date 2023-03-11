package com.freegang.xpler.utils.net

import com.freegang.xpler.utils.log.KLogCat
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object KHttpUtils {
    private const val DEFAULT_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"

    private fun commonConnection(url: URL): HttpURLConnection {
        val connect: HttpURLConnection = url.openConnection() as HttpURLConnection
        connect.requestMethod = "GET"
        connect.setRequestProperty("User-Agent", DEFAULT_UA)
        connect.setRequestProperty("Accept", "*/*")
        return connect
    }

    /**
     * GET请求
     *
     * @param sourceUrl 目标URL地址
     * @param params    参数
     * @return 文本内容
     */
    @JvmStatic
    fun get(sourceUrl: String, params: String = ""): String {
        val sourceUrl = if (params.isBlank()) {
            sourceUrl
        } else {
            if (sourceUrl.contains("?")) "$sourceUrl&$params" else "$sourceUrl?$params"
        }

        var body = ""
        var connect: HttpURLConnection? = null
        try {
            connect = commonConnection(URL(sourceUrl))
            val inputStream = if (connect.responseCode == HttpURLConnection.HTTP_OK
                || connect.responseCode == HttpURLConnection.HTTP_CREATED
            ) {
                InputStreamReader(connect.inputStream, StandardCharsets.UTF_8)
            } else {
                InputStreamReader(connect.errorStream, StandardCharsets.UTF_8)
            }
            body = inputStream.readText()
            inputStream.close()
            connect.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            KLogCat.e("发生异常:\n${e.stackTraceToString()}")
        } finally {
            try {
                connect?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
                KLogCat.e("发生异常:\n${e.stackTraceToString()}")
            }
        }
        return body
    }

    /**
     * 获取重定向地址
     *
     * @param sourceUrl 目标URL地址
     * @return 重定向后的地址
     */
    @JvmStatic
    fun getRedirectsUrl(sourceUrl: String): String {
        var redirectUrl = ""
        var connect: HttpURLConnection? = null
        try {
            connect = commonConnection(URL(sourceUrl))
            connect.instanceFollowRedirects = false
            redirectUrl = if (connect.responseCode == 302) {
                connect.getHeaderField("Location")
            } else {
                connect.url.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KLogCat.e("发生异常:\n${e.stackTraceToString()}")
        } finally {
            try {
                connect?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
                KLogCat.e("发生异常:\n${e.stackTraceToString()}")
            }
        }
        return redirectUrl
    }

    /**
     * 下载文件
     * @param sourceUrl 目标URL地址
     * @param output 输出流
     * @param listener 下载监听器
     */
    @JvmStatic
    fun download(sourceUrl: String, output: OutputStream, listener: DownloadListener) {
        var connect: HttpURLConnection? = null
        try {
            connect = commonConnection(URL(sourceUrl))
            val input = connect.inputStream
            val total = connect.contentLength
            input.use {
                var realCount = 0
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    realCount += count
                    listener.downloading(realCount, total)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            KLogCat.e("发生异常:\n${e.stackTraceToString()}")
        } finally {
            try {
                output.flush()
                output.close()
                connect?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
                KLogCat.e("发生异常:\n${e.stackTraceToString()}")
            }
        }
    }

    // kotlin
    fun download(sourceUrl: String, output: OutputStream, listener: (real: Int, total: Int) -> Unit) {
        download(sourceUrl, output, object : DownloadListener {
            override fun downloading(real: Int, total: Int) {
                listener.invoke(real, total)
            }
        })
    }

    @FunctionalInterface
    interface DownloadListener {
        fun downloading(real: Int, total: Int)
    }
}