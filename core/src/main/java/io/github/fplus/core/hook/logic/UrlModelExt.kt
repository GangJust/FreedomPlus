package io.github.fplus.core.hook.logic

import com.ss.android.ugc.aweme.base.model.UrlModel

fun UrlModel.urlString(): String {
    return urlList.joinToString()
}

fun UrlModel.downUrlString(): String {
    return downUrlList.joinToString()
}