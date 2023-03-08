package com.freegang.xpler.utils.log

import android.app.Application
import android.util.Log
import com.freegang.xpler.utils.io.KFileUtils.forceDelete
import com.freegang.xpler.utils.other.forCalc
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/// 改由Kotlin实现
class KLogCat {
    private var application: Application? = null
    private var localPath: File? = null

    private var tag: String = "GLogCat"
    private var maxBorderSize = 64
    private var showTitle = false
    private var showDivider = false
    private var saveToStorage = false
    private var silence = false

    // border
    private var topBorderStart: Char = '╭'
    private var topBorderEnd: Char = '╮'
    private var bottomBorderStart: Char = '╰'
    private var bottomBorderEnd: Char = '╯'

    private var borderBar: Char = '│'
    private var borderStart: Char = '├'
    private var borderEnd: Char = '┤'
    private var borderSolid: Char = '─'
    private var borderDotted: Char = '┄'

    // symbol char
    private var aggravateChar: Char = '•'
    private var filledCircularChar: Char = '●'
    private var outlineCircularChar: Char = '○'

    /**
     * print logcat
     */
    private fun println(priority: Int, tag: String, vararg msg: String) {
        /// silence mode
        if (silence) return

        //max length string
        val maxReduce = msg.reduce { acc, s -> if (acc.length > s.length) acc else s }

        // border builder
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


        //final border
        val topBorder = "$topBorderStart$border"
        val contentLeftBorder = "$borderStart$borderSolid"
        val bottomBorder = "$bottomBorderStart$border"

        /// print Log
        //top border
        Log.println(priority, tag, topBorder)
        //title
        if (showTitle) {
            Log.println(priority, tag, "$borderBar $tag $borderSolid Level[${getLevelString(priority)}]")
            Log.println(priority, tag, "$borderStart$border")
        }
        //content
        msg.forEach {
            writeStorage(priority, tag, it)
            Log.println(priority, tag, "$contentLeftBorder$it")
            //middle border
            if (msg[msg.lastIndex] != it && showDivider) {
                Log.println(priority, tag, "$contentLeftBorder$divider")
            }
        }
        //bottom border
        Log.println(priority, tag, bottomBorder)
    }

    /**
     * print logcat to file (write)
     */
    private fun writeStorage(priority: Int, tag: String, msg: String) {
        try {
            if (!saveToStorage) return
            val logFile = buildStorageFile(Calendar.getInstance().time) ?: return
            if (!logFile.exists()) logFile.createNewFile()

            FileWriter(logFile, true).use {
                it.append("[")
                it.append("tag=${tag}, ")
                it.append("level=${getLevelString(priority)}, ")
                it.append("time=")
                it.append(dateTimeFormat.format(Calendar.getInstance().time))
                it.append("]: ")
                it.append(msg)
                it.append("\n")
            }
        } catch (e: Exception) {
            Log.e(tag, "GLogCat Error: ${e.message}")
        }
    }

    /**
     * read the logcat in the file
     * if the log file exists
     *
     * @param date need date
     */
    private fun _readStorage(date: Date): String {
        val logFile = buildStorageFile(date) ?: return "read fail, application is null."
        if (!logFile.exists()) return "read fail, file `${logFile.name}` non-existent."

        val reader = FileReader(logFile)
        return reader.readText()
    }

    /**
     * clear local logcat files
     */
    private fun _clearStorage() {
        getStoragePath()?.forceDelete()
    }

    /**
     * logcat local dir
     */
    private fun getStoragePath(): File? {
        return localPath ?: application?.getExternalFilesDir("logs")
    }

    /**
     *
     * build log file: appName_version_data.log
     * for example: QQ_7.9.9_2022-12-12.log
     */
    private fun buildStorageFile(date: Date): File? {
        val application = application ?: return null
        val appName = application.resources.getString(application.applicationInfo.labelRes)
        val versionName = application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
        return File(getStoragePath(), "${appName}_${versionName}_".plus(dateFormat.format(date)).plus(".log"))
    }

    /**
     * get logcat Level.
     * for example: d == Debug, i == Info
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
     * static
     */
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.CHINA)
        private val instance = KLogCat()

        /**
         * init
         */
        @JvmStatic
        @JvmOverloads
        fun init(application: Application, path: File? = null) {
            instance.application = application
            instance.localPath = path
        }

        /**
         * logcat tag
         */
        @JvmStatic
        fun setTag(tag: String) {
            instance.tag = tag
        }

        /**
         * max border size
         */
        @JvmStatic
        fun setMaxBorderSize(size: Int) {
            instance.maxBorderSize = size
        }

        /**
         * open logcat title
         * will print 'tag' and 'level`
         */
        @JvmStatic
        fun showTitle() {
            instance.showTitle = true
        }

        /**
         * open logcat middle border
         * it may be called `divider line`
         */
        @JvmStatic
        fun showDivider() {
            instance.showDivider = true
        }

        /**
         * open logcat sava to storage
         *
         * will be saved in an application private directory
         *
         * @param
         */
        @JvmStatic
        fun openStorage() {
            instance.saveToStorage = true
        }

        @JvmStatic
        fun clearStorage() {
            instance._clearStorage()
        }

        @JvmStatic
        fun readStorage(date: Date): String {
            return instance._readStorage(date)
        }

        /**
         * silent mode, no logcat output
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