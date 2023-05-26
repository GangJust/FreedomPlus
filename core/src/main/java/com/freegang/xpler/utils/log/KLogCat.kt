package com.freegang.xpler.utils.log

import android.app.Application
import android.util.Log
import com.freegang.xpler.utils.other.forCalc
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * KLogCat 是一个用于打印和存储日志的工具类。
 */
class KLogCat {
    private var application: Application? = null
    private var localPath: File? = null

    private var tag: String = "KLogCat"
    private var maxBorderSize = 64
    private var showTitle = false
    private var showDivider = false
    private var saveToStorage = false
    private var silence = false

    // 边框字符
    private var topBorderStart: Char = '╭'
    private var topBorderEnd: Char = '╮'
    private var bottomBorderStart: Char = '╰'
    private var bottomBorderEnd: Char = '╯'

    private var borderBar: Char = '│'
    private var borderStart: Char = '├'
    private var borderEnd: Char = '┤'
    private var borderSolid: Char = '─'
    private var borderDotted: Char = '┄'

    // 符号字符
    private var aggravateChar: Char = '•'
    private var filledCircularChar: Char = '●'
    private var outlineCircularChar: Char = '○'

    /**
     * 打印日志信息。
     *
     * @param priority 日志优先级
     * @param tag 日志标签
     * @param msg 日志内容
     */
    private fun println(priority: Int, tag: String, vararg msg: String) {
        /// 静默模式
        if (silence) return

        // 最长的字符串
        val maxReduce = msg.reduce { acc, s -> if (acc.length > s.length) acc else s }

        // 边框构建器
        val border = if (maxReduce.length >= maxBorderSize) {
            maxBorderSize.forCalc(0, "") { "$it$borderDotted" }
        } else {
            maxReduce.map { borderSolid }.joinToString("")
        }
        val divider = if (maxReduce.length >= 64) {
            maxBorderSize.forCalc(0, "") { "$it$borderDotted" }
        } else {
            maxReduce.map { borderDotted }.joinToString("")
        }

        // 最终边框
        val topBorder = "$topBorderStart$border"
        val contentLeftBorder = "$borderStart$borderSolid"
        val bottomBorder = "$bottomBorderStart$border"

        /// 打印日志
        // 顶部边框
        Log.println(priority, tag, topBorder)
        // 标题
        if (showTitle) {
            Log.println(priority, tag, "$borderBar $tag $borderSolid Level[${getLevelString(priority)}]")
            Log.println(priority, tag, "$borderStart$border")
        }
        // 内容
        msg.forEach {
            writeStorage(priority, tag, it)
            Log.println(priority, tag, "$contentLeftBorder$it")
            // 中间分隔线
            if (msg[msg.lastIndex] != it && showDivider) {
                Log.println(priority, tag, "$contentLeftBorder$divider")
            }
        }
        // 底部边框
        Log.println(priority, tag, bottomBorder)
    }

    /**
     * 将日志写入存储设备。
     *
     * @param priority 日志优先级
     * @param tag 日志标签
     * @param msg 日志内容
     */
    private fun writeStorage(priority: Int, tag: String, msg: String) {
        try {
            if (!saveToStorage) return // 如果不需要保存到存储设备，则直接返回
            val logFile = buildStorageFile(Calendar.getInstance().time) ?: return // 构建日志文件路径，若为空则返回
            if (!logFile.exists()) logFile.createNewFile() // 如果日志文件不存在，则创建新文件

            FileWriter(logFile, true).use { writer ->
                writer.append("[")
                writer.append("tag=$tag, ")
                writer.append("level=${getLevelString(priority)}, ")
                writer.append("time=")
                writer.append(dateTimeFormat.format(Calendar.getInstance().time))
                writer.append("]: ")
                writer.append(msg)
                writer.append("\n")
            }
        } catch (e: Exception) {
            Log.e(tag, "$tag Error: ${e.stackTraceToString()}")
        }
    }

    /**
     * 读取文件中的日志信息。
     *
     * @param date 需要的日期
     * @return 读取到的日志信息
     */
    private fun _readStorage(date: Date): String {
        val logFile = buildStorageFile(date) ?: return "读取失败，application 为空。"
        if (!logFile.exists()) return "读取失败，日志文件 `${logFile.name}` 不存在。"
        return logFile.readText()
    }

    /**
     * 清除本地的日志文件。
     */
    private fun _clearStorage(): Boolean {
        val storageFolder = getStorageFolder() ?: return false
        if (!storageFolder.exists()) return true

        val files = storageFolder.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }

        return true
    }

    /**
     * 获取本地日志文件夹。
     *
     * @return 本地日志文件夹
     */
    private fun getStorageFolder(): File? {
        return localPath ?: application?.getExternalFilesDir("logs")
    }

    /**
     * 构建日志文件：appName_version_data.log。
     * 例如：QQ_7.9.9_2022-12-12.log
     *
     * @param date 日期
     * @return 构建的日志文件
     */
    private fun buildStorageFile(date: Date): File? {
        val application = application ?: return null
        try {
            val appName = application.resources.getString(application.applicationInfo.labelRes)
            val versionName = application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
            return File(getStorageFolder(), "${appName}_${versionName}_".plus(dateFormat.format(date)).plus(".log"))
        } catch (e: Exception) {
            return File(getStorageFolder(), dateFormat.format(date).plus(".log"))
        }
    }

    /**
     * 获取日志优先级的字符串表示。
     * 例如：d 对应 Debug，i 对应 Info
     *
     * @param priority 日志优先级
     * @return 优先级的字符串表示
     */
    private fun getLevelString(priority: Int): String {
        return when (priority) {
            Log.VERBOSE -> "Verbose"
            Log.DEBUG -> "Debug"
            Log.INFO -> "Info"
            Log.WARN -> "Warn"
            Log.ERROR -> "Error"
            Log.ASSERT -> "Assert"
            8 -> "CRASH"
            else -> "Unknown"
        }
    }

    /**
     * 静态成员
     */
    companion object {
        private val instance = KLogCat()

        @JvmStatic
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

        @JvmStatic
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.CHINA)

        /**
         * 初始化 KLogCat。
         *
         * @param application 应用程序实例
         * @param path 本地路径
         */
        @JvmStatic
        @JvmOverloads
        fun init(application: Application, path: File? = null) {
            instance.application = application
            instance.localPath = path
        }

        /**
         * 设置日志的标签。
         *
         * @param tag 日志标签
         */
        @JvmStatic
        fun setTag(tag: String) {
            instance.tag = tag
        }

        /**
         * 设置最大边框大小。
         *
         * @param size 最大边框大小
         */
        @JvmStatic
        fun setMaxBorderSize(size: Int) {
            instance.maxBorderSize = size
        }

        /**
         * 开启日志标题。
         * 将会打印 'tag' 和 'level'
         */
        @JvmStatic
        fun showTitle() {
            instance.showTitle = true
        }

        /**
         * 开启日志分隔线。
         * 可能被称为 'divider line'
         */
        @JvmStatic
        fun showDivider() {
            instance.showDivider = true
        }

        /**
         * 开启保存日志到存储设备。
         *
         * 将会保存到应用程序的私有目录下
         */
        @JvmStatic
        fun openStorage() {
            instance.saveToStorage = true
        }

        /**
         * 清除存储的日志。
         */
        @JvmStatic
        fun clearStorage() {
            instance._clearStorage()
        }

        /**
         * 读取指定日期的日志。
         *
         * @param date 需要读取的日期
         * @return 读取到的日志内容
         */
        @JvmStatic
        fun readStorage(date: Date): String {
            return instance._readStorage(date)
        }

        /**
         * 静默模式，不输出日志。
         */
        @JvmStatic
        fun silence() {
            instance.silence = true
        }

        /**
         * VERBOSE = 2
         */
        @JvmStatic
        fun v(vararg msg: String) {
            instance.println(Log.VERBOSE, instance.tag, *msg)
        }

        /**
         * DEBUG = 3
         */
        @JvmStatic
        fun d(vararg msg: String) {
            instance.println(Log.DEBUG, instance.tag, *msg)
        }

        /**
         * INFO = 4
         */
        @JvmStatic
        fun i(vararg msg: String) {
            instance.println(Log.INFO, instance.tag, *msg)
        }

        /**
         * WARN = 5
         */
        @JvmStatic
        fun w(vararg msg: String) {
            instance.println(Log.WARN, instance.tag, *msg)
        }

        /**
         * ERROR = 6
         */
        @JvmStatic
        fun e(vararg msg: String) {
            instance.println(Log.ERROR, instance.tag, *msg)
        }
    }
}
