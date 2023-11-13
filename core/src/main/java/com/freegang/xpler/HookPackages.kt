package com.freegang.xpler


/**
 * 你只需要在下面增加需要被hook的app包名,
 * 然后在 `HookMain` 中写你的Hook逻辑
 */
object HookPackages {
    const val modulePackageName = "com.freegang.fplus"
    const val corePackageName = "com.freegang.xpler"
    const val douYinPackageName = "com.ss.android.ugc.aweme"
    const val douYinLitePackageName = "com.ss.android.ugc.aweme.lite"
    const val douYinLivePackageName = "com.ss.android.ugc.live"
    const val douYinClonePackageName = "com.ss.android.ugc.awemf" // 测试用
    const val douYinClone1PackageName = "com.ss.android.ugc.awemg" // 测试用


    val packages = listOf(
        modulePackageName,
        corePackageName,
        douYinPackageName,
        douYinLitePackageName,
        douYinLivePackageName,
        douYinClonePackageName,
        douYinClone1PackageName,
    )
}