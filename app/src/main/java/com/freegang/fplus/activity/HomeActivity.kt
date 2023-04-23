package com.freegang.fplus.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.R
import com.freegang.fplus.Themes
import com.freegang.fplus.asDp
import com.freegang.fplus.component.*
import com.freegang.fplus.resource.StringRes
import com.freegang.fplus.viewmodel.HomeVM
import com.freegang.xpler.HookStatus
import com.freegang.xpler.utils.app.appVersionName
import com.freegang.xpler.utils.io.storageRootPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random


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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TopBarView() {
        val isDark by model.isDark.observeAsState(false)

        var rotate by remember { mutableStateOf(0f) }
        val rotateAnimate by animateFloatAsState(
            targetValue = rotate,
            animationSpec = tween(durationMillis = Random.nextInt(500, 1500))
        )

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
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { model.toggleThemeModel() },
                            ),
                        painter = painterResource(id = if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                        contentDescription = if (isDark) "浅色模式" else "深色模式",
                        tint = Themes.nowColors.icon,
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_motion),
                        contentDescription = "检查更新/日志",
                        tint = Themes.nowColors.icon,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotateAnimate)
                            .combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    rotate = if (rotate == 0f) 360f else 0f
                                },
                                onLongClick = {
                                    lifecycleScope.launch {
                                        updateLog = withContext(Dispatchers.IO) {
                                            val inputStream = assets.open("update.log")
                                            val bytes = inputStream.readBytes()
                                            val text = bytes.decodeToString()
                                            inputStream.close()
                                            text
                                        }
                                        showUpdateLogDialog = updateLog.isNotBlank()
                                    }
                                }
                            )
                    )
                }
            },
        )
    }

    @Composable
    fun BodyView() {
        //旧数据迁移
        var showNeedMigrateOldDataDialog by remember { mutableStateOf(model.freedomData.exists()) }
        if (showNeedMigrateOldDataDialog) {
            var showMigrateToContent by remember { mutableStateOf("存在[Freedom]下载数据, 正在迁移至[Freedom+]下载目录!") }
            var showMigrateToConfirm by remember { mutableStateOf("请稍后...") }

            FMessageDialog(
                title = "提示",
                onlyConfirm = true,
                confirm = showMigrateToConfirm,
                onConfirm = {
                    if (showMigrateToConfirm == "确定") {
                        showNeedMigrateOldDataDialog = false
                    }
                },
                content = {
                    Text(text = showMigrateToContent)
                },
            )

            //数据迁移
            LaunchedEffect(key1 = "migrateData") {
                val result = withContext(Dispatchers.IO) {
                    model.freedomData.copyRecursively(
                        target = model.freedomPlusData,
                        overwrite = true,
                        onError = { _, _ ->
                            OnErrorAction.TERMINATE
                        },
                    )
                }
                showMigrateToConfirm = "确定"
                showMigrateToContent = if (!result) {
                    "旧数据迁移失败, 请手动将[外置存储器/Download/Freedom]目录合并至[外置存储器/DCIM/Freedom]中!"
                } else {
                    val deleteAll = model.freedomData.deleteRecursively()
                    if (deleteAll) "旧数据迁移成功!" else "旧数据迁移成功, 但旧数据删除失败, 请手动将[外置存储器/Download/Freedom]删除!"
                }
            }
        }

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
                                    text = if (HookStatus.isEnabled) StringRes.moduleHintSucceeded else StringRes.moduleHintFailed,
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
                    text = "首页控件半透明",
                    checked = model.isTranslucent.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsTranslucent(it)
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

                //数据目录
                FCard(
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_find_file),
                                    contentDescription = "Github",
                                    tint = Themes.nowColors.icon,
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Text(
                                    text = "数据目录: `外置存储器/DCIM/Freedm`",
                                    style = Themes.nowTypography.body1,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                        )
                    },
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 4.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {

                            }
                        ),
                )

                //源码地址
                FCard(
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_github),
                                    contentDescription = "Github",
                                    tint = Themes.nowColors.icon,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Column {
                                    Text(
                                        text = "源码地址",
                                        style = Themes.nowTypography.body1,
                                    )
                                    Text(
                                        text = "https://github.com/GangJust/FreedomPlus",
                                        style = Themes.nowTypography.overline,
                                    )
                                }
                            },
                        )
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/GangJust/FreedomPlus"),
                                    )
                                )
                            }
                        ),
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
                isDark = model.isDark.observeAsState(false).value,
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
        model.checkVersion()
    }

    override fun onPause() {
        super.onPause()
        model.saveModuleConfig()
    }
}