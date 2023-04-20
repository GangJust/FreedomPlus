package com.freegang.douyin

import android.app.Application
import com.freegang.base.BaseHook
import com.freegang.config.Config
import com.freegang.xpler.utils.app.KAppCrashUtils
import com.freegang.xpler.utils.io.KStorageUtils.hasOperationStorage
import com.freegang.xpler.utils.log.KLogCat
import com.freegang.xpler.xp.EmptyHook
import com.freegang.xpler.xp.hookClass
import com.ss.android.ugc.aweme.app.host.AwemeHostApplication
import de.robv.android.xposed.callbacks.XC_LoadPackage

class DouYinMain(lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook<EmptyHook>(lpparam) {

    override fun onInit() {
        lpparam.hookClass(AwemeHostApplication::class.java)
            .method("onCreate") {
                onBefore {
                    val app = thisObject as Application

                    //日志工具
                    KLogCat.init(app)
                    KLogCat.openStorage()

                    //全局异常捕获工具
                    KAppCrashUtils.instance.init(app, "抖音异常崩溃!")

                    //文件读写权限检查
                    if (!app.hasOperationStorage) {
                        showToast(app, "抖音没有文件读写权限!")
                        return@onBefore
                    }

                    //加载配置
                    Config.read(app)

                    //Hook
                    HMainActivity(lpparam)
                    HDetailActivity(lpparam)
                    HMainFragment(lpparam)
                    HLazyFragmentPagerAdapter(lpparam)
                    HGifEmojiDetailActivity(lpparam)
                    HEmojiDetailDialog(lpparam)
                    HEmojiDetailDialogNew(lpparam)
                }
            }
    }
}