package com.freegang.fplus.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.Themes
import com.freegang.fplus.asDp
import com.freegang.fplus.resource.StringRes
import com.freegang.fplus.viewmodel.HomeVM
import com.freegang.ui.component.FCard
import com.freegang.ui.component.FCardBorder
import com.freegang.ui.component.FMessageDialog
import com.freegang.xpler.HookStatus


class HomeActivity : ComponentActivity() {
    private val model by viewModels<HomeVM>()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SwitchItem(
        text: String,
        subtext: String = "",
        isWaiting: Boolean = false,
        checked: State<Boolean>,
        onCheckedChange: (checked: Boolean) -> Unit,
        onClick: () -> Unit = {},
        onLongClick: () -> Unit = {},
    ) {
        FCard(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onClick.invoke()
                    },
                    onLongClick = {
                        onLongClick.invoke()
                    }
                ),
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .then(if (subtext.isNotBlank()) Modifier.padding(vertical = 4.dp) else Modifier),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Column(
                            modifier = Modifier.weight(1f),
                            content = {
                                Text(
                                    text = text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Themes.nowTypography.body1,
                                )
                                if (subtext.isNotBlank()) {
                                    Text(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        text = subtext,
                                        style = Themes.nowTypography.overline.copy(
                                            color = Themes.nowColors.subtitle,
                                        ),
                                    )
                                }
                            },
                        )
                        if (isWaiting) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .wrapContentSize(Alignment.Center)
                                    .padding(17.dp), //switch: width = 34.dp
                                contentAlignment = Alignment.Center,
                                content = {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(Themes.nowTypography.body1.fontSize.asDp),
                                    )
                                }
                            )
                        } else {
                            Switch(
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Themes.nowColors.checkedThumb,
                                    checkedTrackColor = Themes.nowColors.checkedTrack,
                                    uncheckedThumbColor = Themes.nowColors.uncheckedThumb,
                                    uncheckedTrackColor = Themes.nowColors.uncheckedTrack,
                                ),
                                checked = checked.value,
                                onCheckedChange = {
                                    onCheckedChange.invoke(it)
                                },
                            )
                        }
                    }
                )
            }
        )
    }

    @Composable
    fun TopBarView() {
        //view
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

    @Composable
    fun BodyView() {
        //WebDav配置编辑
        var showWebDavConfigEditorDialog by remember { mutableStateOf(false) }
        if (showWebDavConfigEditorDialog) {
            var host by remember { mutableStateOf(model.webDavHost.value ?: "") }
            var username by remember { mutableStateOf(model.webDavUsername.value ?: "") }
            var password by remember { mutableStateOf(model.webDavPassword.value ?: "") }
            var isWaiting by remember { mutableStateOf(false) }
            FMessageDialog(
                title = "配置WebDav",
                cancel = "取消",
                confirm = "确定",
                isWaiting = isWaiting,
                onCancel = {
                    showWebDavConfigEditorDialog = false
                },
                onConfirm = {
                    isWaiting = true
                    model.setWebDavConfig(host, username, password)
                    model.initWebDav { test ->
                        isWaiting = false
                        if (test) {
                            showWebDavConfigEditorDialog = false
                            model.changeIsWebDav(true)
                            Toast.makeText(applicationContext, "测试成功!", Toast.LENGTH_SHORT).show()
                            return@initWebDav
                        }
                        model.changeIsWebDav(false)
                        Toast.makeText(applicationContext, "测试失败, 请检查配置!", Toast.LENGTH_SHORT).show()
                    }
                },
                content = {
                    Column {
                        FCard(
                            border = FCardBorder(borderWidth = 1.0.dp),
                            content = {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    value = host,
                                    maxLines = 1,
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (host.isEmpty()) Text(text = "http://服务器地址:端口")
                                        innerTextField.invoke() //必须调用这行哦
                                    },
                                    onValueChange = {
                                        host = it
                                    },
                                )
                            },
                        )
                        FCard(
                            modifier = Modifier.padding(vertical = 8.dp),
                            border = FCardBorder(borderWidth = 1.0.dp),
                            content = {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    value = username,
                                    maxLines = 1,
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (username.isEmpty()) Text(text = "用户名")
                                        innerTextField.invoke() //必须调用这行哦
                                    },
                                    onValueChange = {
                                        username = it
                                    },
                                )
                            },
                        )
                        FCard(
                            border = FCardBorder(borderWidth = 1.0.dp),
                            content = {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    value = password,
                                    maxLines = 1,
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (password.isEmpty()) Text(text = "密码")
                                        innerTextField.invoke() //必须调用这行哦
                                    },
                                    onValueChange = {
                                        password = it
                                    }
                                )
                            },
                        )
                    }
                },
            )
        }

        //隐藏Tab关键字编辑
        var showHideTabKeywordsEditorDialog by remember { mutableStateOf(false) }
        if (showHideTabKeywordsEditorDialog) {
            var hideTabKeywords by remember { mutableStateOf(model.hideTabKeywords.value ?: "") }
            FMessageDialog(
                title = "请输入关键字, 用逗号分开",
                cancel = "取消",
                confirm = "确定",
                onCancel = { showHideTabKeywordsEditorDialog = false },
                onConfirm = {
                    showHideTabKeywordsEditorDialog = false
                    model.setHideTabKeywords(hideTabKeywords)
                },
                content = {
                    FCard(
                        border = FCardBorder(borderWidth = 1.0.dp),
                        content = {
                            BasicTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                value = hideTabKeywords,
                                maxLines = 1,
                                singleLine = true,
                                onValueChange = {
                                    hideTabKeywords = it
                                },
                            )
                        },
                    )
                },
            )
        }

        //开启隐藏Tab关键字, 复确认弹窗
        var showHideTabTipsDialog by remember { mutableStateOf(false) }
        if (showHideTabTipsDialog) {
            FMessageDialog(
                title = "提示",
                cancel = "关闭",
                confirm = "开启",
                onCancel = {
                    showHideTabTipsDialog = false
                    model.changeIsHideTab(false)
                },
                onConfirm = {
                    showHideTabTipsDialog = false
                    model.changeIsHideTab(true)
                },
                content = {
                    Text(text = "一旦开启顶部Tab隐藏, 将禁止左右滑动切换, 具体效果自行查看!")
                },
            )
        }

        //获取模块状态
        var moduleHint = StringRes.moduleHintFailed
        if (HookStatus.isEnabled) {
            moduleHint = StringRes.moduleHintSucceeded
        } else if (HookStatus.isExpModuleActive(this)) {
            moduleHint = StringRes.moduleHintSucceeded
        }

        //view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            content = {
                // 模块状态
                FCard(
                    modifier = Modifier.padding(bottom = 24.dp, top = 12.dp),
                    content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            content = {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = moduleHint,
                                    style = Themes.nowTypography.body1,
                                )
                            },
                        )
                    }
                )

                // 选项
                SwitchItem(
                    text = "视频创作者单独创建文件夹",
                    checked = model.isOwnerDir.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsOwnerDir(it)
                    },
                )
                SwitchItem(
                    text = "视频/图文/音乐下载",
                    checked = model.isDownload.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsDownload(it)
                    }
                )
                SwitchItem(
                    text = "表情包保存",
                    checked = model.isEmoji.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsEmoji(it)
                    }
                )
                SwitchItem(
                    text = "震动反馈",
                    checked = model.isVibrate.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsVibrate(it)
                    }
                )
                SwitchItem(
                    text = "首页控件半透明",
                    checked = model.isTranslucent.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsTranslucent(it)
                    }
                )
                SwitchItem(
                    text = "清爽模式",
                    subtext = "开启后首页长按视频进入清爽模式",
                    checked = model.isNeat.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsNeat(it)
                    }
                )
                SwitchItem(
                    text = "通知栏下载",
                    subtext = "开启通知栏下载, 否则将显示下载弹窗",
                    checked = model.isNotification.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsNotification(it)
                    },
                )
                var isWebDavWaiting by remember { mutableStateOf(false) }
                SwitchItem(
                    text = "WebDav",
                    subtext = "点击配置WebDav",
                    isWaiting = isWebDavWaiting,
                    checked = model.isWebDav.observeAsState(false),
                    onClick = {
                        showWebDavConfigEditorDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsWebDav(it)
                        if (it && !model.hasWebDavConfig()) {
                            showWebDavConfigEditorDialog = true
                            model.changeIsWebDav(false)
                            Toast.makeText(applicationContext, "请先进行WebDav配置!", Toast.LENGTH_SHORT).show()
                            return@SwitchItem
                        }
                        if (it) {
                            isWebDavWaiting = true
                            model.initWebDav { test ->
                                isWebDavWaiting = false
                                if (test) {
                                    model.changeIsWebDav(true)
                                    Toast.makeText(applicationContext, "WebDav连接成功!", Toast.LENGTH_SHORT).show()
                                    return@initWebDav
                                }
                                Toast.makeText(applicationContext, "WebDav连接失败, 请检查配置!", Toast.LENGTH_SHORT).show()
                                model.changeIsWebDav(false)
                            }
                        }
                    },
                )
                SwitchItem(
                    text = "隐藏顶部tab",
                    subtext = "点击设置关键字",
                    checked = model.isHideTab.observeAsState(false),
                    onClick = {
                        showHideTabKeywordsEditorDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsHideTab(it)
                        if (it) {
                            showHideTabTipsDialog = true
                        }
                    },
                )
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreedomTheme(
                window = window,
                isImmersive = true,
                isDark = false,
                followSystem = false,
                content = {
                    Scaffold(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        topBar = { TopBarView() },
                        content = {
                            BoxWithConstraints(
                                modifier = Modifier.padding(it),
                                content = { BodyView() },
                            )
                        },
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        model.loadConfig()
    }

    override fun onPause() {
        super.onPause()
        model.saveModuleConfig()
    }
}