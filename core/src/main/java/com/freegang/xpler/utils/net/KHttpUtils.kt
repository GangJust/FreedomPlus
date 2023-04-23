package com.freegang.xpler.utils.net

import com.freegang.xpler.utils.log.KLogCat
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object KHttpUtils {
    val ANDROUD_UA get() = System.getProperty("http.agent")

    private fun commonConnection(url: URL): HttpURLConnection {
        val connect: HttpURLConnection = url.openConnection() as HttpURLConnection
        connect.requestMethod = "GET"
        connect.connectTimeout = 5000
        connect.readTimeout = 5000
        connect.instanceFollowRedirects = true
        connect.useCaches = false
        connect.doInput = true
        connect.setRequestProperty("User-Agent", ANDROUD_UA)
        connect.setRequestProperty("Accept", "*/*")
        // 如果是 HTTPS 请求，信任所有证书
        if (connect is HttpsURLConnection) {
            trustAllHosts()
        }
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
            val inputStream = if (connect.responseCode == HttpURLConnection.HTTP_OK || connect.responseCode == HttpURLConnection.HTTP_CREATED) {
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
     * 下载文件
     * @param sourceUrl 目标URL地址
     * @param output 输出流
     * @param listener 下载监听器
     */
    @JvmStatic
    fun download(sourceUrl: String, output: OutputStream, listener: DownloadListener) {
        var connect: HttpURLConnection? = null
        var total = 0
        var realCount = 0
        try {
            connect = commonConnection(URL(sourceUrl))
            total = connect.contentLength
            val input = connect.inputStream
            input.use {
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    realCount += count
                    listener.downloading(realCount, total, false)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            listener.downloading(realCount, total, true)
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
    fun download(sourceUrl: String, output: OutputStream, listener: (real: Int, total: Int, isInterrupt: Boolean) -> Unit) {
        download(sourceUrl, output, object : DownloadListener {
            override fun downloading(real: Int, total: Int, isInterrupt: Boolean) {
                listener.invoke(real, total, isInterrupt)
            }
        })
    }

    @FunctionalInterface
    interface DownloadListener {
        fun downloading(real: Int, total: Int, isInterrupt: Boolean)
    }

    /**
     * 信任所有证书
     */
    private fun trustAllHosts() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(TrustAllCerts()), null)
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 自定义的 TrustManager 实现，信任所有证书
     */
    private class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
}