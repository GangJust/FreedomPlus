package com.freegang.xpler.core.log

import android.util.Log

class XplerLog {
    private var tag: String = "XplerLog"
    private var maxBorderSize = 64
    private var showTitle = false
    private var showDivider = false
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
            maxBorderSize.iterateAndTransform(0, "") { "$it$borderDotted" }
        } else {
            maxReduce.map { borderSolid }.joinToString("")
        }
        val divider = if (maxReduce.length >= 64) {
            maxBorderSize.iterateAndTransform(0, "") { "$it$borderDotted" }
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
     * 对范围内的每个整数执行变换操作，并返回最终结果。
     *
     * @param start 循环开始的整数。
     * @param initialValue 循环开始前的初始值。
     * @param inclusiveEnd 是否包含结束值在内。默认为false，即不包含。
     * @param transform 对每个整数执行的变换操作，接收上一次的结果，并返回新的结果。
     *
     * @return 经过所有变换操作后的最终结果。
     */
    private inline fun <T> Int.iterateAndTransform(
        start: Int,
        initialValue: T,
        inclusiveEnd: Boolean = false,
        transform: (previous: T) -> T,
    ): T {
        var result: T = initialValue

        if (inclusiveEnd) {
            for (i in start..this) {
                result = transform(result)
            }
            return result
        }

        for (i in start until this) {
            result = transform(result)
        }
        return result
    }


    /**
     * 静态成员
     */
    companion object {
        private val instance = XplerLog()

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

        @JvmStatic
        fun tagV(tag: String, msg: String) {
            instance.println(Log.VERBOSE, tag, msg)
        }

        @JvmStatic
        fun tagV(tag: String, msg: Array<String>) {
            instance.println(Log.VERBOSE, tag, *msg)
        }

        /**
         * DEBUG = 3
         */
        @JvmStatic
        fun d(vararg msg: String) {
            instance.println(Log.DEBUG, instance.tag, *msg)
        }

        @JvmStatic
        fun tagD(tag: String, msg: String) {
            instance.println(Log.DEBUG, tag, msg)
        }

        @JvmStatic
        fun tagD(tag: String, msg: Array<String>) {
            instance.println(Log.DEBUG, tag, *msg)
        }

        /**
         * INFO = 4
         */
        @JvmStatic
        fun i(vararg msg: String) {
            instance.println(Log.INFO, instance.tag, *msg)
        }

        @JvmStatic
        fun tagI(tag: String, msg: String) {
            instance.println(Log.INFO, tag, msg)
        }

        @JvmStatic
        fun tagI(tag: String, msg: Array<String>) {
            instance.println(Log.INFO, tag, *msg)
        }

        /**
         * WARN = 5
         */
        @JvmStatic
        fun w(vararg msg: String) {
            instance.println(Log.WARN, instance.tag, *msg)
        }

        @JvmStatic
        fun tagW(tag: String, msg: String) {
            instance.println(Log.WARN, tag, msg)
        }

        @JvmStatic
        fun tagW(tag: String, msg: Array<String>) {
            instance.println(Log.WARN, tag, *msg)
        }

        /**
         * ERROR = 6
         */
        @JvmStatic
        fun e(vararg msg: String) {
            instance.println(Log.ERROR, instance.tag, *msg)
        }

        @JvmStatic
        fun tagE(tag: String, msg: String) {
            instance.println(Log.ERROR, tag, msg)
        }

        @JvmStatic
        fun tagE(tag: String, msg: Array<String>) {
            instance.println(Log.ERROR, tag, *msg)
        }

        /**
         * ERROR = 6
         */
        @JvmStatic
        fun e(e: Throwable) {
            instance.println(Log.ERROR, instance.tag, e.stackTraceToString())
        }

        @JvmStatic
        fun tagE(tag: String, e: Throwable) {
            instance.println(Log.ERROR, tag, e.stackTraceToString())
        }
    }
}
