package com.freegang.douyin.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DateRange
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
import androidx.lifecycle.lifecycleScope
import com.freegang.base.BaseActivity
import com.freegang.config.Config
import com.freegang.douyin.viewmodel.SettingVM
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.appVersionName
import com.freegang.ui.asDp
import com.freegang.ui.component.FCard
import com.freegang.ui.component.FCardBorder
import com.freegang.ui.component.FMessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FreedomSettingActivity : BaseActivity() {
    private val model by viewModels<SettingVM>()

    @Composable
    private fun HomeView() {
        //版本更新弹窗
        var showNewVersionDialog by remember { mutableStateOf(true) }
        val version by model.versionConfig.observeAsState()
        if (version != null) {
            val version = version!!
            if (version.name.compareTo("v${application.appVersionName}") >= 1 && showNewVersionDialog) {
                FMessageDialog(
                    title = "发现新版本 ${version.name}!",
                    confirm = "确定",
                    cancel = "取消",
                    onCancel = {
                        showNewVersionDialog = false
                    },
                    onConfirm = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(version.browserDownloadUrl),
                            )
                        )
                    },
                    content = {
                        LazyColumn(
                            modifier = Modifier,
                            content = {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = version.body,
                                    )
                                }
                            },
                        )
                    }
                )
            }
        }

        //重启抖音提示
        var showRestartAppDialog by remember { mutableStateOf(false) }
        if (showRestartAppDialog) {
            FMessageDialog(
                title = "提示",
                cancel = "取消",
                confirm = "重启",
                onCancel = {
                    showRestartAppDialog = false
                },
                onConfirm = {
                    showRestartAppDialog = false
                    Config.read(application)
                    model.saveModuleConfig(mResources.moduleAssets)
                    KAppUtils.restartApplication(application)
                },
                content = {
                    Text(text = "需要重启应用生效, 若未重启请手动重启")
                },
            )
        }

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
                                    textStyle = MaterialTheme.typography.body2,
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
                                    textStyle = MaterialTheme.typography.body2,
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
                                    textStyle = MaterialTheme.typography.body2,
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
                                textStyle = MaterialTheme.typography.body2,
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
                    showRestartAppDialog = true
                    model.changeIsHideTab(true)
                },
                content = {
                    Text(text = "一旦开启顶部Tab隐藏, 将禁止左右滑动切换, 具体效果自行查看!")
                },
            )
        }

        Scaffold(
            modifier = Modifier.padding(horizontal = 24.dp),
            topBar = { TopBarView() }
        ) {
            Box(Modifier.padding(it)) {
                LazyColumn(
                    content = {
                        item {
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
                                    showRestartAppDialog = true
                                    model.changeIsTranslucent(it)
                                }
                            )
                            SwitchItem(
                                text = "清爽模式",
                                subtext = "开启后首页长按视频进入清爽模式",
                                checked = model.isNeat.observeAsState(false),
                                onCheckedChange = {
                                    showRestartAppDialog = true
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
                                                Toast.makeText(applicationContext, "WebDav连接成功!", Toast.LENGTH_SHORT)
                                                    .show()
                                                return@initWebDav
                                            }
                                            Toast.makeText(
                                                applicationContext,
                                                "WebDav连接失败, 请检查配置!",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                    showRestartAppDialog = true
                                    if (it) {
                                        showHideTabTipsDialog = true
                                    }
                                    model.changeIsHideTab(it)
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun TopBarView() {
        //更新日志弹窗
        var showUpdateLogDialog by remember { mutableStateOf(false) }
        var updateLog by remember { mutableStateOf("") }
        if (showUpdateLogDialog) {
            FMessageDialog(
                title = "更新日志",
                onlyConfirm = true,
                confirm = "确定",
                onConfirm = { showUpdateLogDialog = false },
                content = {
                    LazyColumn(
                        modifier = Modifier,
                        content = {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = updateLog,
                                )
                            }
                        },
                    )
                },
            )
        }

        TopAppBar(
            modifier = Modifier.padding(vertical = 24.dp),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.background,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoxWithConstraints(modifier = Modifier.padding(end = 24.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onBackPressedDispatcher.onBackPressed()
                                },
                            ),
                    )
                }
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
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                lifecycleScope.launch {
                                    updateLog = withContext(Dispatchers.IO) {
                                        val inputStream = mResources.moduleAssets.open("update.log")
                                        val bytes = inputStream.readBytes()
                                        val text = bytes.decodeToString()
                                        inputStream.close()
                                        text
                                    }
                                    showUpdateLogDialog = updateLog.isNotBlank()
                                }
                            },
                        ),
                    imageVector = Icons.Rounded.DateRange,
                    contentDescription = "更新日志"
                )
                /*Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { rewardByAlipay() },
                        ),
                    imageVector = Icons.Rounded.FavoriteBorder,
                    contentDescription = "打赏"
                )*/
            }
        }
    }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onClick.invoke()
                    },
                    onLongClick = {
                        onLongClick.invoke()
                    }
                )
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
                            style = MaterialTheme.typography.body1,
                        )
                        if (subtext.isNotBlank()) {
                            Text(
                                modifier = Modifier.padding(vertical = 2.dp),
                                text = subtext,
                                style = MaterialTheme.typography.body2,
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
                                modifier = Modifier.size(MaterialTheme.typography.body1.fontSize.asDp),
                            )
                        }
                    )
                } else {
                    Switch(
                        checked = checked.value,
                        onCheckedChange = {
                            onCheckedChange.invoke(it)
                        },
                    )
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoTheme {
                HomeView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.loadConfig()
        model.checkVersion()
    }

    override fun onPause() {
        super.onPause()
        model.saveModuleConfig(mResources.moduleAssets)
        Config.read(application)
    }

    private fun rewardByAlipay() {
        if (!KAppUtils.isAppInstalled(this, "com.eg.android.AlipayGphone")) {
            Toast.makeText(applicationContext, "谢谢，你没有安装支付宝客户端", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("alipays://platformapi/startapp?appId=09999988&actionType=toAccount&goBack=NO&amount=3.00&userId=2088022940366251&memo=呐，拿去吃辣条!")
            )
        )
    }
}