package com.freegang.ui.activity

import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.freegang.base.BaseActivity
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.app.appLabelName
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.log.KLogCat
import kotlin.system.exitProcess

class FreedomErrorActivity : BaseActivity() {
    private var mMessage: String? = null
    private var mStackTrace: String? = null

    @Composable
    fun TopBarView() {
        TopAppBar(
            modifier = Modifier.padding(vertical = 24.dp),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.background,
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Freedom+",
                            style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface),
                        )
                        Spacer(modifier = Modifier.padding(vertical = 2.dp))
                        Text(
                            text = "No one is always happy.",
                            style = MaterialTheme.typography.subtitle2.copy(color = MaterialTheme.colors.onSurface.copy(0.5f)),
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
            AutoTheme {
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
                                            val fontSizeValue = MaterialTheme.typography.body2.fontSize.value
                                            AndroidView(
                                                modifier = Modifier.fillMaxSize(),
                                                factory = { context ->
                                                    TextView(context).apply {
                                                        text = errorText
                                                        textSize = fontSizeValue
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
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        application!!.activeActivity?.finishAffinity()
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}