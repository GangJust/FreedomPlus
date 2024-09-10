package io.github.fplus

import io.github.xpler.core.entrance.at

object Constant {
    val modulePackage = "io.github.fplus"

    val scopes = setOf(
        "com.ss.android.ugc.aweme" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.aweme"),
        "com.ss.android.ugc.aweme.lite" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.aweme.lite"),
        "com.ss.android.ugc.live" at ("com.ss.android.ugc.aweme.app.host.AwemeHostApplication" to "com.ss.android.ugc.live"),
    )

}