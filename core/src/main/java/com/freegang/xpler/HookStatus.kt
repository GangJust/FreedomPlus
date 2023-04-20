package com.freegang.xpler

object HookStatus {
    /**
     * 该方法会在 HookInit 中被Hook替换,
     * 模块直接调用该方法进行状态判断
     */
    val isEnabled get() = false
}