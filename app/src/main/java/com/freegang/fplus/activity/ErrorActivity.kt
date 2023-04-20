package com.freegang.fplus.activity

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.Themes
import com.freegang.fplus.resource.StringRes
import com.freegang.fplus.viewmodel.AppVM
import com.freegang.xpler.utils.app.KAppVersionUtils.appLabelName
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionCode
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.utils.log.KLogCat

class ErrorActivity : ComponentActivity() {
    private val model by viewModels<AppVM>()

    private var mMessage: String? = null
    private var mStackTrace: String? = null


    @Composable
    fun TopBarView() {
        TopAppBar(
            modifier = Modifier.padding(vertical = 24.dp),
            elevation = 0.dp,
            backgroundColor = Themes.nowColors.colors.background,
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StringRes.moduleTitle,
                            style = Themes.nowTypography.subtitle1,
                        )
                        Spacer(modifier = Modifier.padding(vertical = 2.dp))
                        Text(
                            text = StringRes.moduleSubtitle,
                            style = Themes.nowTypography.subtitle2,
                        )
                    }
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMessage = intent.getStringExtra("message")
        mStackTrace = intent.getStringExtra("stack_trace")

        val errorText = "发生错误: ${mMessage}\n" +
                "出现时间: ${KLogCat.dateTimeFormat.format(System.currentTimeMillis())}\n" +
                "设备信息: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "系统版本: Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})\n" +
                "应用版本: ${application.appLabelName} ${application.appVersionName} (${application.appVersionCode})\n" +
                "堆栈信息: \n${mStackTrace}"

        setContent {
            FreedomTheme(
                window = window,
                isImmersive = true,
                isDark = model.isDark.observeAsState(false).value,
                followSystem = false,
                content = {
                    Scaffold(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        topBar = { TopBarView() },
                        content = {
                            BoxWithConstraints(
                                modifier = Modifier.padding(it),
                                content = {
                                    LazyColumn(
                                        content = {
                                            item {
                                                AndroidView(
                                                    modifier = Modifier.fillMaxSize(),
                                                    factory = { context ->
                                                        TextView(context).apply {
                                                            text = errorText
                                                            textSize = Themes.nowTypography.body2.fontSize.value
                                                            setTextIsSelectable(true)
                                                        }
                                                    },
                                                )
                                            }
                                        },
                                    )
                                },
                            )
                        },
                    )
                }
            )
        }
    }
}