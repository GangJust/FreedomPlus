package io.github.xpler


/**
 * 你只需要在下面增加需要被hook的app包名,
 * 然后在 `HookMain` 中写你的Hook逻辑
 */
object HookConfig {
    const val modulePackageName = "io.github.fplus"
    const val douYinPackageName = "com.ss.android.ugc.aweme"
    const val douYinLitePackageName = "com.ss.android.ugc.aweme.lite"
    const val douYinLivePackageName = "com.ss.android.ugc.live"
    const val douYinClonePackageName = "com.ss.android.ugc.awemf" // test
    const val douYinClone1PackageName = "com.ss.android.ugc.awemg" // test


    /**
     * module and host package names
     */
    val allPackageNames
        get() = listOf(
            modulePackageName,
            douYinPackageName,
            douYinLitePackageName,
            douYinLivePackageName,
            douYinClonePackageName,
            douYinClone1PackageName,
        )

    /**
     * host application
     */
    val hostApplicationName
        get() = "com.ss.android.ugc.aweme.app.host.AwemeHostApplication"
}