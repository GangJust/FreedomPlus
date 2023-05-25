package com.freegang.xpler.utils.other

import kotlin.concurrent.thread

object KThreadUtils {

    /**
     * 开启一个新线程
     * @param runnable
     */
    @JvmStatic
    fun newThread(runnable: Runnable) {
        thread { runnable.run() }
    }
}