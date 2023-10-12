package com.freegang.xpler


/**
 * 你只需要在下面增加需要被hook的app包名,
 * 然后在 `HookMain` 中写你的Hook逻辑
 */
object HookPackages {
    const val modulePackageName = "com.freegang.fplus"
    const val corePackageName = "com.freegang.xpler"
    const val douYinPackageName = "com.ss.android.ugc.aweme"
    const val douYinAllPackageName = "com.ss.android.ugc.awem" //测试用


    val packages = listOf(
        modulePackageName,
        corePackageName,
        douYinPackageName,
        douYinAllPackageName,
    )
}