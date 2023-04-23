package com.freegang.xpler.utils.net

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object KNetUtils {

    private const val TAG = "NetUtils"

    /**
     * 发送一个 HTTP GET 请求
     *
     * @param url 请求的 URL
     * @return 响应结果字符串
     */
    fun sendHttpGetRequest(url: String): String? {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            connection.doInput = true

            // 如果是 HTTPS 请求，信任所有证书
            if (connection is HttpsURLConnection) {
                trustAllHosts()
            }

            connection.connect()

            // 读取响应结果
            val inputStream = connection.inputStream
            reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            return response.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            connection?.disconnect()
        }
        return null
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
