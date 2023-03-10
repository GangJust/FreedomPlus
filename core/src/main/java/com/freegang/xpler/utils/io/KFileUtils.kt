package com.freegang.xpler.utils.io

import java.io.File


object KFileUtils {
    /**
     * 删除所有内容, 如果文件夹不为空, 则递归遍历删除其子项, 直到将它自身删除结束
     * see at [File.deleteRecursively()]
     */
    fun File.forceDelete() {
        //文件直接删除
        if (this.isFile) {
            this.delete()
            return
        }
        //遍历删除文件夹
        this.listFiles()?.forEach {
            if (it.isFile) it.delete()
            if (it.isDirectory) it.forceDelete()
        }
        //删除文件夹本身
        this.delete()
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
     * 按给定最大长度截取某个字符串, 成功返回截取后的文本, 失败返回原文本
     * 该字符串最大长度不能超过[max],
     * 并以[ellipsis]结尾
     * @param max 最大长度
     * @param ellipsis 省略占位文本
     * @return String
     */
    fun String.subMax(max: Int = 10, ellipsis: String = ""): String {
        if (this.length <= max) return this
        return this.substring(0, max).plus(ellipsis)
    }

    //获取纯净的文件名, 替换掉部分特殊符号
    val File.pureName: String
        get() = name.pureFileName

    //获取纯净的文件名, 替换掉部分特殊符号
    val String.pureFileName: String
        get() {
            return this.replace("\\s".toRegex(), "")
                .replace("<", "‹")
                .replace(">", "›")
                .replace("\"", "”")
                .replace("\'", "’")
                .replace("\\", "-")
                .replace("\$", "¥")
                .replace("/", "-")
                .replace("|", "-")
                .replace("*", "-")
                .replace(":", "-")
                .replace("?", "？")
        }
}
