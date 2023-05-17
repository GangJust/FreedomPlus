package com.freegang.xpler.utils.io

import java.io.File

object KFileUtils {

}

/**
 * 删除所有内容, 如果文件夹不为空, 则递归遍历删除其子项, 直到将它自身删除结束
 * see at [File.deleteRecursively()]
 */
fun File.deleteForcefully() {
    if (!this.exists()) {
        return
    }

    try {
        // 文件直接删除
        if (this.isFile) {
            this.delete()
            return
        }

        // 遍历删除文件夹
        this.listFiles()?.forEach {
            it.deleteForcefully()
        }

        // 删除文件夹本身
        this.delete()
    } catch (e: SecurityException) {
        // 处理没有权限删除的情况
        e.printStackTrace()
    }
}

/**
 * 从当前file的基础上, 增加一个 child, 当前file如果是文件, 则返回抛出异常
 * @param name 子文件名
 */
fun File.child(name: String): File {
    if (this.isFile) {
        throw Exception("`File.child(\"$name\")` trying to add a child to a file.")
    }
    return File(this, name)
}

/**
 * 必要的路径地址, 如果该路径不存在, 则为其创建
 * @param isFile 该路径最右侧的子项是否为文件
 */
fun File.need(isFile: Boolean = false): File {
    if (isFile) {
        val parent = this.parentFile!!
        if (!parent.exists()) parent.mkdirs()
        if (!this.exists()) this.createNewFile()
        return this
    }

    if (!this.exists()) this.mkdirs()
    return this
}

/**
 * 将一个字符串转换为文件, 该字符串必须是一个正确的路径地址
 */
fun String.toFile() = File(this)

/**
 * 获取纯净的文件名, 替换掉部分特殊符号
 */
val File.pureName: String get() = name.pureFileName

/**
 * 获取纯净的文件名, 替换掉部分特殊符号
 */
val String.pureFileName: String
    get() {
        val replaceMap = mapOf(
            " " to "",
            "<" to "‹",
            ">" to "›",
            "\"" to "”",
            "\'" to "’",
            "\\" to "-",
            "$" to "¥",
            "/" to "-",
            "|" to "-",
            "*" to "-",
            ":" to "-",
            "?" to "？"
        )
        var pureName = this
        for (entry in replaceMap) pureName = pureName.replace(entry.key, entry.value)
        return pureName
    }

/**
 * 获取安全的文件名, 如果某个文件名超过225个字符, 则会从最后一个字符截断
 */
val String.secureFilename: String
    get() {
        val maxLength = 255
        val lastIndexOf = this.lastIndexOf(".")
        val hasSuffix = lastIndexOf != -1

        // 如果字符串长度未超过最大限制且没有后缀名，则直接返回该字符串
        if (this.length <= maxLength && !hasSuffix) {
            return this
        }

        // 计算保留后缀名的最大长度
        val suffixLength = if (hasSuffix) this.substring(lastIndexOf).length else 0
        val maxByteLength = maxLength - suffixLength

        // 对字符串进行截取
        val bytes = this.toByteArray(Charsets.UTF_8)
        var byteLength = 0
        var lastByteIsChineseChar = false
        for ((_, byte) in bytes.withIndex()) {
            // 如果当前字符是中文字符
            val byteLengthToAdd = if (byte.toInt() and 0xff > 127) 2 else 1
            if (byteLength + byteLengthToAdd > maxByteLength) {
                break
            }
            byteLength += byteLengthToAdd
            lastByteIsChineseChar = byteLengthToAdd == 2
            if (byteLength == maxByteLength) {
                break
            }
        }

        // 如果最后一个字符是中文字符，需要将其去掉，因为如果截取中文字符的一部分，会导致编码问题
        if (lastByteIsChineseChar && byteLength > 1) {
            byteLength -= 1
        }

        return if (hasSuffix) {
            val prefix = this.substring(0, lastIndexOf)
            prefix.substring(0, byteLength.coerceAtMost(prefix.length)) + this.substring(lastIndexOf)
        } else {
            this.substring(0, byteLength)
        }
    }

