package com.freegang.fplus.activity

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.R
import com.freegang.fplus.Themes
import com.freegang.fplus.resource.StringRes
import com.freegang.ktutils.app.activeActivity
import com.freegang.ktutils.app.appVersionCode
import com.freegang.ktutils.app.appVersionName
import com.freegang.ui.component.ScrollableContainer
import kotlin.system.exitProcess

class ErrorActivity : ComponentActivity() {
    private var errMessage: String? = null

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TopBarView() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
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
                Text(
                    text = "Version $appVersionName ($appVersionCode)",
                    style = Themes.nowTypography.caption,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_acute),
                contentDescription = "分享",
                tint = Themes.nowColors.icon,
                modifier = Modifier
                    .size(20.dp)
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            shareErrorMessage(errMessage!!)
                        },
                        onLongClick = {
                            shareErrorMessage(errMessage!!)
                        }
                    )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        errMessage = intent.getStringExtra("errMessage") ?: return finish()
        errMessage = "模块版本: $appVersionName ($appVersionCode)\n$errMessage"

        setContent {
            FreedomTheme(
                window = window,
                isImmersive = true,
                isDark = false,
                followSystem = false,
                content = {
                    Scaffold(
                        topBar = { TopBarView() },
                        content = {
                            BoxWithConstraints(
                                modifier = Modifier.padding(it),
                                content = {
                                    ScrollableContainer {
                                        BasicTextField(
                                            value = errMessage!!,
                                            textStyle = Themes.nowTypography.body2,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            readOnly = true,
                                            onValueChange = { /*nothing*/ },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        application!!.activeActivity?.finishAffinity()
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }

    private fun shareErrorMessage(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "日志分享"))
    }
}