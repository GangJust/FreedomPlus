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

    // Freedom -> ???????????????/Download/Freedom/
    private val freedomData
        get() = application.storageRootFile
            .child(Environment.DIRECTORY_DOWNLOADS)
            .child("Freedom")

    // FreedomPlus -> ???????????????/DCIM/Freedom/
    private val freedomPlusData
        get() = application.storageRootFile
            .child(Environment.DIRECTORY_DCIM)
            .child("Freedom")

    // ??????????????????
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
                        contentDescription = if (isDark.value) "????????????" else "????????????",
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
                        contentDescription = "????????????/??????",
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

        //??????????????????
        if (showUpdateLogDialog) {
            FDialog(
                title = "????????????",
                onlyConfirm = true,
                confirm = "??????",
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
                // ????????????
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

                // ??????
                SwitchItem(
                    text = "????????????????????????????????????",
                    checked = isOwnerDir,
                    onCheckedChange = {
                        isOwnerDir.value = it
                        config.isOwnerDir = isOwnerDir.value
                    },
                )
                SwitchItem(
                    text = "??????/??????/????????????",
                    checked = isDownload,
                    onClick = {
                    },
                    onCheckedChange = {
                        isDownload.value = it
                        config.isDownload = isDownload.value
                    }
                )
                SwitchItem(
                    text = "???????????????/???????????????",
                    checked = isEmoji,
                    onCheckedChange = {
                        isEmoji.value = it
                        config.isEmoji = isEmoji.value
                    },
                )
                SwitchItem(
                    text = "????????????tab",
                    subtext = "?????????????????????",
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

                //????????????
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
                                Text(text = "????????????: `???????????????/DCIM/Freedm`")
                            }
                        )
                    }
                )

                //????????????
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
                                        text = "????????????",
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

        //????????????
        if (versionConfig.value != null) {
            val version = versionConfig.value!!
            if (version.name.compareTo("v${application.appVersionName}") >= 1) {
                resetConfig()
                FDialog(
                    title = "???????????????${version.name}!",
                    onlyConfirm = true,
                    confirm = "??????",
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

        //???????????????????????????
        if (showInputKeywordDialog) {
            FDialog(
                title = "??????????????????, ???????????????",
                cancel = "??????",
                confirm = "??????",
                onCancel = {
                    showInputKeywordDialog = false
                },
                onConfirm = {
                    config.hideTabKeyword = hideTabKeyword.value
                    if (config.hideTabKeyword.isBlank()) {
                        Toast.makeText(application, "?????????????????????!", Toast.LENGTH_SHORT).show()
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

        //??????tab??????,??????
        if (showHideTabTips) {
            FDialog(
                title = "??????",
                cancel = "??????",
                confirm = "??????",
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
                    Text(text = "??????????????????Tab??????, ???????????????????????????, ????????????????????????!")
                },
            )
        }

        //???????????????
        if (showDataMoveDialog) {
            var showMoveToContent by remember { mutableStateOf("??????Freedom????????????, ???????????????Freedom+????????????!") }
            var showMoveToConfirm by remember { mutableStateOf("?????????...") }

            FDialog(
                title = "??????",
                onlyConfirm = true,
                confirm = showMoveToConfirm,
                onConfirm = {
                    if (showMoveToConfirm == "??????") {
                        showDataMoveDialog = false
                    }
                },
                content = {
                    Text(text = showMoveToContent)
                },
            )

            //????????????
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
                showMoveToConfirm = "??????"
                showMoveToContent = if (!result) {
                    "??????????????????, ????????????[???????????????/Download/Freedom]???????????????[???????????????/DCIM/Freedom]???!"
                } else {
                    val deleteAll = freedomData.deleteRecursively()
                    if (deleteAll) "??????????????????!" else "??????????????????, ????????????????????????, ????????????[???????????????/Download/Freedom]??????!"
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

    // ????????????????????????
    private fun getVersion() {
        lifecycleScope.launch {
            versionConfig.value = withContext(Dispatchers.IO) {
                Version.getRemoteReleasesLatest()
            }
        }
    }

    // ????????????
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

    // ????????????
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

    // ????????????
    private fun saveConfig() {
        lifecycleScope.launch {
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

