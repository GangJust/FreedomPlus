package com.freegang.xpler.utils.io

import java.io.File

object KFileUtils {
    /**
     * 强制删除文件或目录，即使文件或目录不存在也不会抛出异常。
     *
     * @param file 要删除的文件或目录
     */
    @JvmStatic
    fun deleteForcefully(file: File) {
        if (!file.exists()) {
            return
        }

        try {
            // 文件直接删除
            if (file.isFile) {
                file.delete()
                return
            }

            // 遍历删除文件夹
            file.listFiles()?.forEach {
                it.deleteForcefully()
            }

            // 删除文件夹本身
            file.delete()
        } catch (e: SecurityException) {
            // 处理没有权限删除的情况
            e.printStackTrace()
        }
    }

    /**
     * 递归删除文件或目录。
     *
     * @param file 要删除的文件或目录
     */
    @JvmStatic
    fun deleteRecursively(file: File) {
        file.deleteRecursively()
    }

    /**
     * 清理文件名中的特殊字符，返回纯净的文件名。
     *
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    @JvmStatic
    fun pureFileName(filename: String): String {
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
        var pureName = filename.replace("\\s".toRegex(), "")
        for (entry in replaceMap) pureName = pureName.replace(entry.key, entry.value)
        return pureName
    }

    /**
     * 将文件名转换为安全的文件名，确保文件名不超过255个字节。
     *
     * @param filename 原始文件名
     * @return 安全的文件名
     */
    @JvmStatic
    fun secureFilename(filename: String): String {
        val maxLength = 255
        val lastIndexOf = filename.lastIndexOf(".")
        val hasSuffix = lastIndexOf != -1

        // 如果字符串长度未超过最大限制且没有后缀名，则直接返回该字符串
        if (filename.length <= maxLength && !hasSuffix) {
            return filename
        }

        // 计算保留后缀名的最大长度
        val suffixLength = if (hasSuffix) filename.substring(lastIndexOf).length else 0
        val maxByteLength = maxLength - suffixLength

        // 对字符串进行截取
        val bytes = filename.toByteArray(Charsets.UTF_8)
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
            val prefix = filename.substring(0, lastIndexOf)
            prefix.substring(0, byteLength.coerceAtMost(prefix.length)) + filename.substring(lastIndexOf)
        } else {
            filename.substring(0, byteLength)
        }
    }
}

///
/**
 * 强制删除文件或目录。
 * 如果文件或目录不存在，则不进行任何操作。
 * 如果删除目录时发生异常，将会打印堆栈轨迹。
 */
fun File.deleteForcefully() = KFileUtils.deleteForcefully(this)

/**
 * 在当前文件路径下创建指定名称的子文件或子目录。
 * 如果当前文件是一个文件而不是目录，将抛出异常。
 *
 * @param name 子文件或子目录的名称
 * @return 创建的子文件或子目录
 * @throws Exception 如果当前文件是一个文件而不是目录
 */
fun File.child(name: String): File {
    if (this.isFile) {
        throw Exception("`File.child(\"$name\")` trying to add a child to a file.")
    }
    return File(this, name)
}

/**
 * 根据需要创建文件或目录。
 * 如果指定的文件不存在，将会创建它。
 * 如果指定的文件是一个目录，将会创建该目录及其父目录（如果不存在）。
 *
 * @param isFile 是否是文件。默认为 `false`，表示创建目录；如果为 `true`，表示创建文件。
 * @return 创建的文件或目录
 * @throws IOException 如果创建文件或目录时发生 I/O 异常
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
 * 将字符串转换为文件对象, 请确保该字符串是一个正确的文件路径
 *
 * @return 字符串对应的文件对象
 */
fun String.toFile() = File(this)

/**
 * 获取文件的纯文件名，去除特殊字符。
 *
 * @return 纯文件名
 */
val File.pureName: String
    get() = KFileUtils.pureFileName(this.name)

/**
 * 获取字符串的纯文件名，去除特殊字符。
 *
 * @return 纯文件名
 */
val String.pureFileName: String
    get() = KFileUtils.pureFileName(this)

/**
 * 将字符串转换为安全的文件名，去除特殊字符，并限制总长度不超过255个字节。
 *
 * @return 安全的文件名
 */
val String.secureFilename: String
    get() = KFileUtils.secureFilename(this)


