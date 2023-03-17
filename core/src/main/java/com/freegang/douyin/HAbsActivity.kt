package com.freegang.douyin

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bytedance.ies.uikit.base.AbsActivity
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.douyin.logic.DownloadLogic
import com.freegang.douyin.logic.SaveLogic
import com.freegang.douyin.model.DeAweme
import com.freegang.douyin.model.ImageUrlStruct
import com.freegang.xpler.R
import com.freegang.xpler.databinding.HookAppbarLayoutBinding
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.utils.json.KJSONUtils.getStringOrDefault
import com.freegang.xpler.utils.json.KJSONUtils.isEmpties
import com.freegang.xpler.utils.json.KJSONUtils.parseJSON
import com.freegang.xpler.utils.json.KJSONUtils.toJSONObjectArray
import com.freegang.xpler.utils.log.KLogCat
import com.freegang.xpler.utils.net.KHttpUtils
import com.freegang.xpler.utils.other.KResourceUtils
import com.freegang.xpler.xp.*
import com.ss.android.ugc.aweme.detail.ui.DetailActivity
import com.ss.android.ugc.aweme.feed.model.Aweme
import com.ss.android.ugc.aweme.main.MainActivity
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject


/// 基类Activity
class HAbsActivity(
    lpparam: XC_LoadPackage.LoadPackageParam,
) : BaseHook(lpparam) {
    private var primaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    val config: Config //get, 确保获取时已经有值
        get() = Config.get()

    override fun onHook() {

        lpparam.hookClass(AbsActivity::class.java)
            .method("onCreate", Bundle::class.java) {
                onAfter {
                    if (!config.isEmoji) return@onAfter
                    val absActivity = thisObject as AbsActivity
                    if (absActivity !is DetailActivity) return@onAfter
                    hookComment(absActivity)
                }
            }
            .method("onResume") {
                onAfter {
                    if (!config.isDownload) return@onAfter
                    addClipboardListener(this, config)
                }
            }
            .method("onPause") {
                onBefore {
                    if (!config.isDownload) return@onBefore
                    removeClipboardListener(this)
                }
            }
    }

    // 添加剪贴板监听
    private fun addClipboardListener(it: XC_MethodHook.MethodHookParam, config: Config) {
        val absActivity = it.thisObject as AbsActivity
        val clipboardManager = absActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (!clipboardManager.hasPrimaryClip() || clipData!!.itemCount <= 0) return@OnPrimaryClipChangedListener

            //获取剪贴板内容
            val clipDataItem = clipData.getItemAt(0)
            val shareText = clipDataItem.text.toString()
            if (!shareText.contains("http")) return@OnPrimaryClipChangedListener

            //跳过直播链接, 按文本检查
            if (shareText.contains("【抖音】") && shareText.contains("正在直播") && shareText.contains("一起支持")) {
                showToast(absActivity, "不支持直播!")
                return@OnPrimaryClipChangedListener
            }

            // @Deprecated
            //showToast(absActivity, "复制成功!\n$shareText")

            //截取短链接, 一般这个截取逻辑能用到死, 但是不排除抖音更新分享文本格式, 如果真更新再说.
            //val urlIndexOf = shareText.indexOf("http")
            //val sortUrl = shareText.substring(urlIndexOf)
            //mainLogic(absActivity, sortUrl, config)

            hookActivity(absActivity)
        }
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    // 移除剪贴板监听
    private fun removeClipboardListener(it: XC_MethodHook.MethodHookParam) {
        val absActivity = it.thisObject as Context
        val clipboardManager = absActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener)
    }

    private fun hookActivity(absActivity: AbsActivity) {
        hookActivityAt1(absActivity)
        hookActivityAt2(absActivity)
    }

    private fun hookActivityAt1(absActivity: AbsActivity) {
        val versions = listOf("24.0.0", "24.1.0", "24.2.0")
        if (!versions.contains(absActivity.appVersionName)) return

        if (absActivity is MainActivity) {
            val methods = absActivity.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                var aweme = methods.first().call(absActivity)
                if (aweme == null) {
                    val curFragment = absActivity.findMethod("getCurFragment", *arrayOf<Any>())?.call(absActivity)
                    val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
                    if (curFragmentMethods.isNotEmpty()) {
                        aweme = curFragmentMethods.first().call(curFragment!!)
                    }
                }
                DownloadLogic(this@HAbsActivity, absActivity, aweme, config.isOwnerDir)
            }
        }

        if (absActivity is DetailActivity) {
            val any1 = absActivity.getObjectField<Any>("LIZJ") ?: return
            val methods = any1.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                val aweme = methods.first().call(any1)
                DownloadLogic(this@HAbsActivity, absActivity, aweme, config.isOwnerDir)
            }
        }
    }

    private fun hookActivityAt2(absActivity: AbsActivity) {
        val versions = listOf("24.2.0", "24.3.0", "24.4.0", "24.5.0", "24.6.0")
        if (!versions.contains(absActivity.appVersionName)) return

        if (absActivity is DetailActivity || absActivity is MainActivity) {
            val methods = absActivity.findMethodsByReturnType(Aweme::class.java)
            if (methods.isNotEmpty()) {
                var aweme = methods.first().call(absActivity)
                if (aweme == null) {
                    val curFragment = absActivity.findMethod("getCurFragment", *arrayOf<Any>())?.call(absActivity)
                    val curFragmentMethods = curFragment?.findMethodsByReturnType(Aweme::class.java) ?: listOf()
                    if (curFragmentMethods.isNotEmpty()) {
                        aweme = curFragmentMethods.first().call(curFragment!!)
                    }
                }
                DownloadLogic(this@HAbsActivity, absActivity, aweme, config.isOwnerDir)
            }
        }
    }

    // 获取评论区图片
    private fun hookComment(absActivity: AbsActivity) {
        launch {
            delay(200L)

            hookCommentAt1(absActivity)
            hookCommentAt2(absActivity)
        }
    }

    private fun hookCommentAt1(absActivity: AbsActivity) {
        val versions = listOf("24.0.0", "24.1.0", "24.2.0")

        if (!versions.contains(absActivity.appVersionName)) return
        var urlList: List<String> = listOf()

        val any1 = absActivity.getObjectField<Any>("LIZJ") ?: return
        val methods = any1.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            val aweme = methods.first().call(any1) ?: return
            val aid = aweme.getObjectField<Any>("aid")
            if (aid != null && "$aid".contains("-|[a-z]".toRegex())) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }
        }

        if (urlList.isEmpty()) return
        rebuildCommentTopBarView(absActivity, urlList)
    }

    private fun hookCommentAt2(absActivity: AbsActivity) {
        val versions = listOf("24.2.0", "24.3.0", "24.4.0", "24.5.0", "24.6.0")
        if (!versions.contains(absActivity.appVersionName)) return

        var urlList: List<String> = listOf()
        val methods = absActivity.findMethodsByReturnType(Aweme::class.java)
        if (methods.isNotEmpty()) {
            val aweme = methods.first().call(absActivity) ?: return
            // 如果是评论区的评论, 则获取图片
            // 24.2.0, 24.3.0, 24.4.0
            val commentFeedOuterCid = aweme.getObjectField<Any>("commentFeedOuterCid")
            if (commentFeedOuterCid != null) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }

            // 24.5.0, 24.6.0
            val commentFeedOuterAweme = aweme.getObjectField<Any>("commentFeedOuterAweme")
            if (commentFeedOuterAweme != null) {
                val image = aweme.getObjectField<List<Any>>("images")?.first() ?: return
                urlList = image.getObjectField<List<String>>("urlList") ?: listOf()
            }
        }
        if (urlList.isEmpty()) return
        rebuildCommentTopBarView(absActivity, urlList)
    }

    // 重新构建评论区顶部操作栏
    private fun rebuildCommentTopBarView(absActivity: AbsActivity, urlList: List<String> = listOf()) {
        val contentView: View = absActivity.window.decorView.findViewById(android.R.id.content)
        val outViews = ArrayList<View>()
        contentView.findViewsWithText(outViews, "返回", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        val backBtn = outViews.first { it.contentDescription.equals("返回") }

        //清空旧视图
        val viewGroup = backBtn.parent as ViewGroup
        viewGroup.removeAllViews()

        //重新构建视图
        val appbar = KResourceUtils.inflateView<RelativeLayout>(viewGroup.context, R.layout.hook_appbar_layout)
        val binding = HookAppbarLayoutBinding.bind(appbar)
        binding.backBtn.setOnClickListener {
            backBtn.performClick()
        }
        binding.saveBtn.setOnClickListener {
            SaveLogic(this@HAbsActivity, it.context, urlList)
        }
        viewGroup.addView(appbar)
    }

    ///// @Deprecated
    // 获取接口中的视频信息
    @Deprecated("Deprecated")
    private fun mainLogic(absActivity: AbsActivity, sortUrl: String) {
        launch {
            try {
                val originUrl = getOriginalUrl(sortUrl)
                val detailId = getDetailId(originUrl)
                if (detailId.isBlank()) {
                    showToast(absActivity, "未获取到视频ID!")
                    return@launch
                }

                var detailJson = getDetailJson(detailId)
                for (i in 0 until 10) {
                    if (detailJson != "blocked") break //出错重试, 最多10次
                    detailJson = getDetailJson(detailId)
                }

                val json = detailJson.parseJSON().apply {
                    if (isEmpties) {
                        showToast(absActivity, detailJson) //json解析失败, 直接返回失败文本
                        return@launch
                    } else if (isNull("aweme_detail")) {
                        showToast(absActivity, "未获取到视频信息!")
                        return@launch
                    }
                }

                val detail = parseDetailInfo(json)
                DownloadLogic(this@HAbsActivity, absActivity, detail, config.isOwnerDir)
            } catch (e: JSONException) {
                showToast(absActivity, "视频信息解析失败!")
                KLogCat.e("视频信息解析失败:\n${e.stackTraceToString()}")
            }
        }
    }

    /**
     * 获取原始地址
     * @param sortUrl 短链接
     */
    private suspend fun getOriginalUrl(sortUrl: String): String {
        return withContext(Dispatchers.IO) {
            KHttpUtils.getRedirectsUrl(sortUrl)
        }
    }

    /**
     * 获取视频/图文ID
     * @param originUrl 原始url地址
     */
    private fun getDetailId(originUrl: String): String {
        val find = Regex("/(\\d+)/").find(originUrl)
        return find?.groupValues?.last() ?: ""
    }

    /**
     *
     * 获取单个视频/图文详情 Json
     *
     * @param videoId 视频/图文ID
     */
    private suspend fun getDetailJson(videoId: String): String {
        // thanks for: https://github.com/Evil0ctal/Douyin_TikTok_Download_API
        val url = "https://www.iesdouyin.com/aweme/v1/web/aweme/detail/"
        val params = "aid=1128&version_name=23.5.0&device_platform=android&os_version=2333&aweme_id=$videoId"
        return withContext(Dispatchers.IO) {
            KHttpUtils.get(url, params)
        }
    }

    /**
     * 解析视频/图文基本信息
     * @param json
     */
    @Throws(JSONException::class)
    private fun parseDetailInfo(json: JSONObject): DeAweme {
        val deAweme = DeAweme()
        /// 视频/图文基本信息 Json
        val awemeDetail = json.getJSONObject("aweme_detail")
        //描述
        deAweme.desc = awemeDetail.getStringOrDefault("desc")

        /// 用户公开的基本信息 Json
        val author = awemeDetail.getJSONObject("author")
        //昵称
        deAweme.nickname = author.getStringOrDefault("nickname")
        //用户唯一(账号)ID
        val uniqueId = author.getStringOrDefault("unique_id")
        //目前来看, uniqueId为空, shortId即用户Id
        deAweme.shortId = author.getStringOrDefault("short_id", uniqueId)

        if (awemeDetail.isNull("images")) {
            /// 视频基本信息 Json
            val video = awemeDetail.getJSONObject("video")
            val playAddrH264 = video.getJSONObject("play_addr_h264")

            val videoUrlList = mutableListOf<String>()
            val urlList = playAddrH264.getJSONArray("url_list")
            for (index in 0 until urlList.length()) {
                videoUrlList.add(urlList.getStringOrDefault(index))
            }


            deAweme.videoUrlList = videoUrlList
        } else {
            /// 图文基本信息 Json
            val imageUrlStructList = mutableListOf<ImageUrlStruct>()
            val images = awemeDetail.getJSONArray("images").toJSONObjectArray()
            for (image in images) {
                val imageUrlList = mutableListOf<String>()
                val urlList = image.getJSONArray("url_list")
                for (index in 0 until urlList.length()) {
                    imageUrlList.add(urlList.getStringOrDefault(index))
                }
                imageUrlStructList.add(ImageUrlStruct(imageUrlList))
            }

            deAweme.imageUrlStructList = imageUrlStructList
        }

        /// 背景音乐基本信息 Json
        val music = awemeDetail.getJSONObject("music")
        val playUrl = music.getJSONObject("play_url")

        val musicUrlList = mutableListOf<String>()
        val urlList = playUrl.getJSONArray("url_list")
        for (index in 0 until urlList.length()) {
            musicUrlList.add(urlList.getStringOrDefault(index))
        }
        deAweme.musicUrlList = musicUrlList

        return deAweme
    }
}