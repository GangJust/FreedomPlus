package com.freegang.fplus.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.freegang.config.Config
import com.freegang.config.Version
import com.freegang.config.VersionConfig
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.R
import com.freegang.fplus.Themes
import com.freegang.fplus.component.FCard
import com.freegang.fplus.component.FDialog
import com.freegang.fplus.resource.StringRes
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionCode
import com.freegang.xpler.utils.app.KAppVersionUtils.appVersionName
import com.freegang.xpler.utils.io.KFileUtils.child
import com.freegang.xpler.utils.io.KStorageUtils.storageRootFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

class HomeActivity : ComponentActivity() {
    private val isDark = mutableStateOf(false)
    private val hookHint = mutableStateOf(StringRes.moduleHintFailed)

    private val versionConfig = mutableStateOf<VersionConfig?>(null)

    private lateinit var config: Config
    private val isOwnerDir = mutableStateOf(false)
    private val isDownload = mutableStateOf(false)
    private val isEmoji = mutableStateOf(false)
    private val isHideTab = mutableStateOf(false)
    private val hideTabKeyword = mutableStateOf("")

    // Freedom -> 外置存储器/Download/Freedom/
    private val freedomData
        get() = application.storageRootFile
            .child(Environment.DIRECTORY_DOWNLOADS)
            .child("Freedom")

    // FreedomPlus -> 外置存储器/DCIM/Freedom/
    private val freedomPlusData
        get() = application.storageRootFile
            .child(Environment.DIRECTORY_DCIM)
            .child("Freedom")

    // 模块反射调用
    private fun hookHint() {
        hookHint.value = StringRes.moduleHintSucceeded
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TopBarView() {
        var showUpdateLogDialog by remember { mutableStateOf(false) }
        var updateLog by remember { mutableStateOf("") }

        var rotate by remember { mutableStateOf(0f) }
        val rotateAnimate by animateFloatAsState(
            targetValue = rotate,
            animationSpec = tween(durationMillis = Random.nextInt(500, 1500))
        )

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
                        painter = painterResource(id = if (isDark.value) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                        contentDescription = if (isDark.value) "浅色模式" else "深色模式",
                        tint = Themes.nowColors.icon,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    isDark.value = !isDark.value
                                },
                            )
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
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
                                            assets
                                                .open("update.log")
                                                .readBytes()
                                                .decodeToString()
                                        }
                                        showUpdateLogDialog = updateLog.isNotBlank()
                                    }
                                }
                            )
                    )
                }
            },
        )

        //更新日志弹层
        if (showUpdateLogDialog) {
            FDialog(
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
                                    text = updateLog,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                    )
                },
            )
        }
    }

    @Composable
    fun ContentView() {
        var showInputKeywordDialog by remember { mutableStateOf(false) }
        var showHideTabTips by remember { mutableStateOf(false) }
        var showDataMoveDialog by remember { mutableStateOf(freedomData.exists()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                                    text = hookHint.value,
                                    style = Themes.nowTypography.body1,
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                        )
                    }
                )

                // 选项
                SwitchItem(
                    text = "视频创作者单独创建文件夹",
                    checked = isOwnerDir,
                    onCheckedChange = {
                        isOwnerDir.value = it
                        config.isOwnerDir = isOwnerDir.value
                    },
                )
                SwitchItem(
                    text = "视频/图文/音乐下载",
                    checked = isDownload,
                    onClick = {
                    },
                    onCheckedChange = {
                        isDownload.value = it
                        config.isDownload = isDownload.value
                    }
                )
                SwitchItem(
                    text = "评论区图片/表情包保存",
                    checked = isEmoji,
                    onCheckedChange = {
                        isEmoji.value = it
                        config.isEmoji = isEmoji.value
                    },
                )
                SwitchItem(
                    text = "隐藏顶部tab",
                    subtext = "点击设置关键字",
                    checked = isHideTab,
                    onClick = {
                        showInputKeywordDialog = true
                    },
                    onCheckedChange = {
                        isHideTab.value = it
                        if (isHideTab.value) {
                            showHideTabTips = true
                        }
                    },
                )

                //数据目录
                FCard(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 4.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                startActivity(
                                    Intent().apply {
                                        action = "com.android.fileexplorer.export.VIEW_HOME"
                                        addCategory("android.intent.category.DEFAULT")
                                    }
                                )
                            }
                        ),
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_find_file),
                                    contentDescription = "Github",
                                    tint = Themes.nowColors.icon,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Text(text = "数据目录: `外置存储器/DCIM/Freedm`")
                            }
                        )
                    }
                )

                //源码地址
                FCard(
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
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
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
                            }
                        )
                    }
                )
            }
        )

        //版本更新
        if (versionConfig.value != null) {
            val version = versionConfig.value!!
            if (version.name.compareTo("v${application.appVersionName}") >= 1) {
                resetConfig()
                FDialog(
                    title = "发现新版本${version.name}!",
                    onlyConfirm = true,
                    confirm = "确定",
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
                                        text = version.body,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                        )
                    }
                )
            }
        }

        //过滤关键字输入弹层
        if (showInputKeywordDialog) {
            FDialog(
                title = "请输入关键字, 用逗号分开",
                cancel = "取消",
                confirm = "确定",
                onCancel = {
                    showInputKeywordDialog = false
                },
                onConfirm = {
                    config.hideTabKeyword = hideTabKeyword.value
                    if (config.hideTabKeyword.isBlank()) {
                        Toast.makeText(application, "关键字内容为空!", Toast.LENGTH_SHORT).show()
                    } else {
                        showInputKeywordDialog = false
                    }
                },
                content = {
                    FCard(
                        content = {
                            BasicTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                value = hideTabKeyword.value,
                                onValueChange = {
                                    hideTabKeyword.value = it
                                }
                            )
                        }
                    )
                },
            )
        }

        //开启tab过滤,提示
        if (showHideTabTips) {
            FDialog(
                title = "提示",
                cancel = "关闭",
                confirm = "开启",
                onCancel = {
                    showHideTabTips = false
                    isHideTab.value = false
                    config.isHideTab = isHideTab.value
                },
                onConfirm = {
                    showHideTabTips = false
                    isHideTab.value = true
                    config.isHideTab = isHideTab.value
                },
                content = {
                    Text(text = "一旦开启顶部Tab隐藏, 将禁止左右滑动切换, 具体效果自行查看!")
                },
            )
        }

        //旧数据迁移
        if (showDataMoveDialog) {
            var showMoveToContent by remember { mutableStateOf("存在Freedom下载数据, 正在迁移至Freedom+下载目录!") }
            var showMoveToConfirm by remember { mutableStateOf("请稍后...") }

            FDialog(
                title = "提示",
                onlyConfirm = true,
                confirm = showMoveToConfirm,
                onConfirm = {
                    if (showMoveToConfirm == "确定") {
                        showDataMoveDialog = false
                    }
                },
                content = {
                    Text(text = showMoveToContent)
                },
            )

            //数据迁移
            LaunchedEffect(key1 = "moveData") {
                val result = withContext(Dispatchers.IO) {
                    freedomData.copyRecursively(
                        target = freedomPlusData,
                        overwrite = true,
                        onError = { _, _ ->
                            OnErrorAction.TERMINATE
                        },
                    )
                }
                showMoveToConfirm = "确定"
                showMoveToContent = if (!result) {
                    "数据迁移失败, 请手动将[外置存储器/Download/Freedom]目录合并至[外置存储器/DCIM/Freedom]中!"
                } else {
                    val deleteAll = freedomData.deleteRecursively()
                    if (deleteAll) "数据迁移成功!" else "数据迁移成功, 但旧数据删除失败, 请手动将[外置存储器/Download/Freedom]删除!"
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SwitchItem(
        text: String,
        subtext: String = "",
        checked: MutableState<Boolean>,
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
                                        text = subtext,
                                        style = Themes.nowTypography.overline.copy(
                                            color = Themes.nowColors.subtitle,
                                        ),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            },
                        )
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
                )
            }
        )
    }

    // 获取远程版本信息
    private fun getVersion() {
        lifecycleScope.launch {
            versionConfig.value = withContext(Dispatchers.IO) {
                Version.getRemoteReleasesLatest()
            }
        }
    }

    // 读取配置
    private fun readConfig() {
        lifecycleScope.launch {
            config = withContext(Dispatchers.IO) { Config.read(application) }
            isOwnerDir.value = config.isOwnerDir
            isDownload.value = config.isDownload
            isEmoji.value = config.isEmoji
            isHideTab.value = config.isHideTab
            hideTabKeyword.value = config.hideTabKeyword
        }
    }

    // 重置配置
    private fun resetConfig() {
        isOwnerDir.value = false
        isDownload.value = false
        isEmoji.value = false
        isHideTab.value = false

        config.isSupportHint = true
        config.isOwnerDir = isOwnerDir.value
        config.isDownload = isDownload.value
        config.isEmoji = isEmoji.value
        config.isHideTab = isHideTab.value
    }

    // 保存配置
    private fun saveConfig() {
        lifecycleScope.launch {
            config.isSupportHint = application.appVersionCode != config.versionCode
            config.versionName = application.appVersionName
            config.versionCode = application.appVersionCode
            config.save(application)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            readConfig()

            FreedomTheme(
                window = window,
                isImmersive = true,
                isDark = isDark.value,
                followSystem = false,
                content = {
                    Scaffold(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        topBar = { TopBarView() },
                        content = {
                            BoxWithConstraints(
                                modifier = Modifier.padding(it),
                                content = {
                                    ContentView()
                                }
                            )
                        },
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        getVersion()
    }

    override fun onPause() {
        super.onPause()
        saveConfig()
    }
}

