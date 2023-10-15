package com.freegang.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.freegang.base.BaseActivity
import com.freegang.helper.HighlightStyleBuilder
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.appVersionName
import com.freegang.ktutils.json.getIntOrDefault
import com.freegang.ktutils.json.parseJSONArray
import com.freegang.ui.asDp
import com.freegang.ui.component.FCard
import com.freegang.ui.component.FCardBorder
import com.freegang.ui.component.FCountDownMessageDialog
import com.freegang.ui.component.FMessageDialog
import com.freegang.ui.component.FWaitingMessageDialog
import com.freegang.ui.viewmodel.FreedomSettingVM
import com.freegang.webdav.WebDav
import com.freegang.xpler.HookPackages
import com.freegang.xpler.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class FreedomSettingActivity : BaseActivity() {
    private val model by viewModels<FreedomSettingVM>()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TopBarView() {
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
                        text = "Freedom+ Setting",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onSurface,
                        ),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                    Text(
                        text = "No one is always happy.",
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface.copy(0.5f),
                        ),
                    )
                }
                BoxWithConstraints {
                    var showLogDialog by remember { mutableStateOf(false) }
                    if (showLogDialog) {
                        FMessageDialog(
                            title = "类日志",
                            cancel = "取消",
                            confirm = "清除",
                            onCancel = {
                                showLogDialog = false
                            },
                            onConfirm = {
                                showLogDialog = false
                                model.clearClasses()
                                KToastUtils.show(application, "类日志清除")
                            }
                        ) {
                            Text(
                                text = """
                                    类日志是对混淆类名的本地存储操作, 设计该功能的主要目的是为了减少启动时间。
                                    清除类日志并不会造成功能丢失，相反来说若某功能无法正常使用, 也可尝试手动清除类日志, 模块在下次启动时会对功能类重新搜索。
                                """.trimIndent(),
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_manage),
                        contentDescription = "Log",
                        modifier = Modifier
                            .size(24.dp)
                            .combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onLongClick = {
                                    if (!model.hasClasses) {
                                        KToastUtils.show(application, "没有类日志!")
                                        return@combinedClickable
                                    }
                                    showLogDialog = true
                                },
                                onClick = {
                                    if (!model.hasClasses) {
                                        KToastUtils.show(application, "没有类日志")
                                        return@combinedClickable
                                    }
                                    showLogDialog = true
                                },
                            ),
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                BoxWithConstraints {
                    var rotate by remember { mutableStateOf(0f) }
                    val rotateAnimate by animateFloatAsState(
                        targetValue = rotate,
                        animationSpec = tween(durationMillis = Random.nextInt(500, 1500)),
                    )

                    // 更新日志弹窗
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
                                            SelectionContainer {
                                                Text(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    text = updateLog,
                                                    style = MaterialTheme.typography.body1,
                                                )
                                            }
                                        }
                                    },
                                )
                            },
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_motion),
                        contentDescription = "更新日志",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotateAnimate)
                            .combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onLongClick = {
                                    lifecycleScope.launch {
                                        updateLog = withContext(Dispatchers.IO) {
                                            val inputStream = mResources.pluginAssets.open("update.log")
                                            val bytes = inputStream.readBytes()
                                            val text = bytes.decodeToString()
                                            inputStream.close()
                                            text
                                        }
                                        showUpdateLogDialog = updateLog.isNotBlank()
                                    }
                                },
                                onClick = {
                                    rotate = if (rotate == 0f) 360f else 0f
                                },
                            ),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
    @Composable
    private fun BodyView(
        modifier: Modifier,
    ) {
        // 版本更新弹窗
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
                                        style = MaterialTheme.typography.body1,
                                    )
                                }
                            },
                        )
                    }
                )
            }
        }

        // 重启抖音提示
        var showRestartAppDialog by remember { mutableStateOf(false) }
        if (showRestartAppDialog) {
            if (application.packageName != HookPackages.modulePackageName) {
                FMessageDialog(
                    title = "提示",
                    cancel = "取消",
                    confirm = "重启",
                    onCancel = {
                        showRestartAppDialog = false
                    },
                    onConfirm = {
                        showRestartAppDialog = false
                        model.setVersionConfig(mResources.pluginAssets)
                        KAppUtils.restartApplication(application)
                    },
                    content = {
                        Text(
                            text = "需要重启应用生效, 若未重启请手动重启",
                            style = MaterialTheme.typography.body1,
                        )
                    },
                )
            }
        }

        LazyColumn(
            modifier = modifier,
            content = {
                item {
                    // 选项
                    BoxWithConstraints {
                        var showTipsDialog by remember { mutableStateOf(false) }
                        SwitchItem(
                            text = "视频/图文/音乐下载",
                            checked = model.isDownload.observeAsState(false),
                            onCheckedChange = {
                                model.changeIsDownload(it)
                                if (it) {
                                    showTipsDialog = true
                                }
                            }
                        )

                        if (showTipsDialog) {
                            FMessageDialog(
                                title = "提示",
                                confirm = "确定",
                                onlyConfirm = true,
                                onConfirm = {
                                    showTipsDialog = false
                                }
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("开启后在视频页操作")
                                        withStyle(SpanStyle(Color.Red)) {
                                            append("“分享->复制链接”")
                                        }
                                        append("即可弹出下载选项。")
                                    },
                                )
                            }
                        }
                    }
                    SwitchItem(
                        text = "视频创作者单独创建文件夹",
                        checked = model.isOwnerDir.observeAsState(false),
                        onCheckedChange = {
                            model.changeIsOwnerDir(it)
                        },
                    )
                    SwitchItem(
                        text = "通知栏下载",
                        subtext = "开启通知栏下载, 否则将显示下载弹窗",
                        checked = model.isNotification.observeAsState(false),
                        onCheckedChange = {
                            model.changeIsNotification(it)
                        },
                    )
                    SwitchItem(
                        text = "保存表情包/评论视频、图片",
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
                        text = "移除悬浮挑战/评论贴纸",
                        checked = model.isRemoveSticker.observeAsState(false),
                        onCheckedChange = {
                            model.changeIsRemoveSticker(it)
                        }
                    )
                    BoxWithConstraints {
                        var showDoubleClickModelDialog by remember { mutableStateOf(false) }
                        if (showDoubleClickModelDialog) {
                            var radioIndex by remember { mutableStateOf(model.doubleClickType.value ?: 2) }
                            FMessageDialog(
                                title = "请选择双击响应模式",
                                confirm = "更改",
                                onlyConfirm = true,
                                onConfirm = { showDoubleClickModelDialog = false },
                                content = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 0,
                                                onClick = {
                                                    radioIndex = 0
                                                    model.changeDoubleClickType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "暂停视频",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 1,
                                                onClick = {
                                                    radioIndex = 1
                                                    model.changeDoubleClickType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "打开评论",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 2,
                                                onClick = {
                                                    radioIndex = 2
                                                    model.changeDoubleClickType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "点赞视频",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        SwitchItem(
                            text = "双击视频响应类型",
                            subtext = "点击调整双击视频响应方式",
                            checked = model.isDoubleClickType.observeAsState(false),
                            onClick = {
                                showDoubleClickModelDialog = true
                            },
                            onCheckedChange = {
                                model.changeIsDoubleClickType(it)
                            }
                        )
                    }
                    SwitchItem(
                        text = "视频时长超过10分钟提示",
                        subtext = "避免你die在厕所",
                        checked = model.isLongtimeVideoToast.observeAsState(false),
                        onCheckedChange = {
                            model.changeIsLongtimeVideoToast(it)
                        }
                    )
                    BoxWithConstraints {
                        // 加号按钮响应状态
                        var showIsDisablePhotoDialog by remember { mutableStateOf(false) }
                        if (showIsDisablePhotoDialog) {
                            var radioIndex by remember { mutableStateOf(model.photoButtonType.value ?: 2) }
                            FMessageDialog(
                                title = "请选择拍摄按钮响应模式",
                                confirm = "更改",
                                onlyConfirm = true,
                                onConfirm = { showIsDisablePhotoDialog = false },
                                content = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 0,
                                                onClick = {
                                                    radioIndex = 0
                                                    model.changePhotoButtonType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "允许拍摄",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 1,
                                                onClick = {
                                                    radioIndex = 1
                                                    model.changePhotoButtonType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "禁止拍摄",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = radioIndex == 2,
                                                onClick = {
                                                    radioIndex = 2
                                                    model.changePhotoButtonType(radioIndex)
                                                },
                                            )
                                            Text(
                                                text = "移除按钮",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        SwitchItem(
                            text = "隐藏底部加号按钮",
                            subtext = "点击更改加号按钮响应状态",
                            checked = model.isDHidePhotoButton.observeAsState(false),
                            onClick = {
                                showIsDisablePhotoDialog = true
                            },
                            onCheckedChange = {
                                model.changeIsHidePhotoButton(it)
                                showRestartAppDialog = true
                            }
                        )
                    }
                    BoxWithConstraints { // 限制重构作用域
                        var showVideoFilterDialog by remember { mutableStateOf(false) }
                        var showVideoFilterTips by remember { mutableStateOf(false) }

                        if (showVideoFilterDialog) {
                            var inputValue by remember {
                                mutableStateOf(
                                    TextFieldValue(
                                        buildFilterTypeStyle(model.videoFilterKeywords.value ?: ""),
                                    )
                                )
                            }
                            FMessageDialog(
                                title = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 16.dp)
                                    ) {
                                        Text(
                                            text = "请输入关键字, 用逗号分开",
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = "帮助",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .combinedClickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    onClick = {
                                                        showVideoFilterTips = true
                                                    },
                                                ),
                                        )
                                    }
                                },
                                cancel = "取消",
                                confirm = "确定",
                                onCancel = {
                                    showVideoFilterDialog = false
                                },
                                onConfirm = {
                                    showVideoFilterDialog = false
                                    model.setVideoFilterKeywords(inputValue.text)
                                },
                                content = {
                                    FCard(
                                        border = FCardBorder(borderWidth = 1.0.dp),
                                        content = {
                                            BasicTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                value = inputValue,
                                                maxLines = 4,
                                                textStyle = MaterialTheme.typography.body1,
                                                decorationBox = { innerTextField ->
                                                    if (inputValue.text.isEmpty()) {
                                                        Text(
                                                            text = "视频类型或文本中出现的关键字",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                    }
                                                    innerTextField()
                                                },
                                                onValueChange = { value ->
                                                    inputValue = value
                                                },
                                                visualTransformation = { text ->
                                                    TransformedText(
                                                        text = buildFilterTypeStyle(text.text),
                                                        offsetMapping = OffsetMapping.Identity
                                                    )
                                                }
                                            )
                                        },
                                    )
                                }
                            )
                        }

                        if (showVideoFilterTips) {
                            FMessageDialog(
                                title = "提示",
                                onlyConfirm = true,
                                confirm = "确定",
                                onConfirm = {
                                    showVideoFilterTips = false
                                }
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("支持过滤的视频类型：")
                                        append(
                                            buildFilterTypeStyle(
                                                value = model.videoFilterTypes.joinToString(
                                                    "，"
                                                )
                                            )
                                        )
                                        append("\n支持文案关键字过滤视频，如视频文案中出现 “优惠,买,#生日” 等文本字样。")
                                    },
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                        }

                        SwitchItem(
                            text = "视频过滤",
                            subtext = "点击设置视频过滤类型或文本关键字",
                            checked = model.isVideoFilter.observeAsState(false),
                            onClick = {
                                showVideoFilterDialog = true
                            },
                            onCheckedChange = {
                                model.changeIsVideoFilter(it)
                            }
                        )
                    }
                    BoxWithConstraints { // 限制重构作用域
                        // 清爽模式响应模式
                        var showLongPressModeDialog by remember { mutableStateOf(false) }
                        if (showLongPressModeDialog) {
                            var isLongPressMode by remember {
                                mutableStateOf(
                                    model.isLongPressMode.value ?: true
                                )
                            }
                            FMessageDialog(
                                title = "请选择响应模式",
                                confirm = "更改",
                                onlyConfirm = true,
                                onConfirm = { showLongPressModeDialog = false },
                                content = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isLongPressMode,
                                                onClick = {
                                                    isLongPressMode = true
                                                    model.changeLongPressMode(isLongPressMode)
                                                },
                                            )
                                            Text(
                                                text = "长按视频上半",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = !isLongPressMode,
                                                onClick = {
                                                    isLongPressMode = false
                                                    model.changeLongPressMode(isLongPressMode)
                                                },
                                            )
                                            Text(
                                                text = "长按视频下半",
                                                style = MaterialTheme.typography.body1,
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        SwitchItem(
                            text = "清爽模式",
                            subtext = "长按视频进入清爽模式, 点击更改响应模式",
                            checked = model.isNeatMode.observeAsState(false),
                            onClick = {
                                showLongPressModeDialog = true
                            },
                            onCheckedChange = {
                                model.changeIsNeatMode(it)
                            }
                        )
                    }
                    BoxWithConstraints {
                        SwitchItem(
                            text = "全屏沉浸",
                            subtext = "体验全屏沉浸式播放, 但会造成视频剪辑拉伸",
                            checked = model.isImmersive.observeAsState(false),
                            onClick = {

                            },
                            onCheckedChange = {
                                model.changeIsImmersive(it)
                                showRestartAppDialog = true
                            }
                        )
                    }
                    BoxWithConstraints { // 限制重构作用域
                        // 隐藏Tab关键字编辑
                        var showHideTabKeywordsEditorDialog by remember { mutableStateOf(false) }
                        if (showHideTabKeywordsEditorDialog) {
                            var hideTabKeywords by remember {
                                mutableStateOf(
                                    model.hideTabKeywords.value ?: ""
                                )
                            }
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
                                                textStyle = MaterialTheme.typography.body1,
                                                onValueChange = {
                                                    hideTabKeywords = it
                                                },
                                            )
                                        },
                                    )
                                },
                            )
                        }

                        // 开启隐藏Tab关键字, 复确认弹窗
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
                                    Text(
                                        text = "一旦开启顶部Tab隐藏, 将禁止左右滑动切换, 具体效果自行查看!",
                                        style = MaterialTheme.typography.body1,
                                    )
                                },
                            )
                        }
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
                    BoxWithConstraints { // 限制重构作用域
                        // WebDav配置编辑
                        var showWebDavConfigEditorDialog by remember { mutableStateOf(false) }
                        if (showWebDavConfigEditorDialog) {
                            val webDavHistory = model.webDavHistory.observeAsState(initial = emptySet())
                            var showWebDavHistoryMenu by remember { mutableStateOf(false) }
                            var host by remember { mutableStateOf(model.webDavHost.value ?: "") }
                            var username by remember { mutableStateOf(model.webDavUsername.value ?: "") }
                            var password by remember { mutableStateOf(model.webDavPassword.value ?: "") }
                            var isWaiting by remember { mutableStateOf(false) }
                            FWaitingMessageDialog(
                                title = {
                                    ExposedDropdownMenuBox(
                                        expanded = showWebDavHistoryMenu,
                                        onExpandedChange = { /*expanded = !expanded*/ },
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                        ) {
                                            Text(
                                                text = "配置WebDav",
                                                style = MaterialTheme.typography.body1,
                                                modifier = Modifier.weight(1f),
                                            )

                                            BoxWithConstraints {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_history),
                                                    contentDescription = "WebDav列表",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .combinedClickable(
                                                            indication = null,
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            onClick = {
                                                                if (webDavHistory.value.isEmpty()) {
                                                                    KToastUtils.show(
                                                                        application,
                                                                        "没有WebDav历史"
                                                                    )
                                                                }
                                                                showWebDavHistoryMenu =
                                                                    webDavHistory.value.isNotEmpty()
                                                            },
                                                        ),
                                                )

                                                ExposedDropdownMenu(
                                                    expanded = showWebDavHistoryMenu,
                                                    onDismissRequest = { showWebDavHistoryMenu = false },
                                                    modifier = Modifier.heightIn(max = 200.dp)
                                                ) {
                                                    webDavHistory.value.forEach {
                                                        Text(
                                                            text = it.host,
                                                            style = MaterialTheme.typography.body1,
                                                            modifier = Modifier
                                                                .combinedClickable(
                                                                    onClick = {
                                                                        showWebDavHistoryMenu = false
                                                                        host = it.host
                                                                        username = it.username
                                                                        password = it.password
                                                                    },
                                                                    onLongClick = {
                                                                        model.removeWebDavConfig(
                                                                            WebDav.Config(
                                                                                it.host,
                                                                                it.username,
                                                                                it.password,
                                                                            )
                                                                        )
                                                                        showWebDavHistoryMenu =
                                                                            webDavHistory.value.isNotEmpty()
                                                                        KToastUtils.show(
                                                                            application,
                                                                            "删除成功"
                                                                        )
                                                                    }
                                                                )
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                cancel = "取消",
                                confirm = "确定",
                                isWaiting = isWaiting,
                                onCancel = {
                                    showWebDavConfigEditorDialog = false
                                },
                                onConfirm = {
                                    val webDavConfig = WebDav.Config(host, username, password)
                                    isWaiting = true
                                    model.setWebDavConfig(webDavConfig)
                                    model.initWebDav { test, msg ->
                                        KToastUtils.show(applicationContext, msg)
                                        isWaiting = false
                                        if (test) {
                                            showWebDavConfigEditorDialog = false
                                            model.changeIsWebDav(true)
                                            model.addWebDavConfig(webDavConfig)
                                            return@initWebDav
                                        }
                                        model.changeIsWebDav(false)
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
                                                    textStyle = MaterialTheme.typography.body1,
                                                    decorationBox = { innerTextField ->
                                                        if (host.isEmpty()) Text(
                                                            text = "http://服务器地址:端口/初始化路径",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                        innerTextField.invoke() // 必须调用这行哦
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
                                                    textStyle = MaterialTheme.typography.body1,
                                                    decorationBox = { innerTextField ->
                                                        if (username.isEmpty()) Text(
                                                            text = "用户名",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                        innerTextField.invoke() // 必须调用这行哦
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
                                                    textStyle = MaterialTheme.typography.body1,
                                                    decorationBox = { innerTextField ->
                                                        if (password.isEmpty()) Text(
                                                            text = "密码",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                        innerTextField.invoke() // 必须调用这行哦
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
                                    Toast.makeText(
                                        applicationContext,
                                        "请先进行WebDav配置!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@SwitchItem
                                }
                                if (it) {
                                    isWebDavWaiting = true
                                    model.initWebDav { test, msg ->
                                        KToastUtils.show(applicationContext, msg)
                                        isWebDavWaiting = false
                                        if (test) {
                                            model.changeIsWebDav(true)
                                            return@initWebDav
                                        }
                                        model.changeIsWebDav(false)
                                    }
                                }
                            },
                        )
                    }
                    BoxWithConstraints { // 限制重构作用域
                        // 定时退出
                        var showTimedExitSettingDialog by remember { mutableStateOf(false) }
                        if (showTimedExitSettingDialog) {
                            val times = model.timedExitValue.value?.parseJSONArray()
                            var timedExit by remember {
                                mutableStateOf(
                                    "${
                                        times?.getIntOrDefault(
                                            0,
                                            10
                                        ) ?: 10
                                    }"
                                )
                            }
                            var freeExit by remember {
                                mutableStateOf(
                                    "${
                                        times?.getIntOrDefault(
                                            1,
                                            3
                                        ) ?: 3
                                    }"
                                )
                            }

                            KToastUtils.show(applicationContext, "低于3分钟将不执行~")
                            FMessageDialog(
                                title = "定时退出时间设置",
                                cancel = "取消",
                                confirm = "确定",
                                onCancel = {
                                    showTimedExitSettingDialog = false
                                },
                                onConfirm = {
                                    showTimedExitSettingDialog = false
                                    val intTimedExit = timedExit.toIntOrNull()
                                    val intFreeExit = freeExit.toIntOrNull()
                                    if (intTimedExit == null || intFreeExit == null) {
                                        KToastUtils.show(applicationContext, "请输入正确的分钟数")
                                        return@FMessageDialog
                                    }
                                    if (intTimedExit < 0 || intFreeExit < 0) {
                                        KToastUtils.show(applicationContext, "请输入正确的分钟数")
                                        return@FMessageDialog
                                    }
                                    KToastUtils.show(applicationContext, "设置成功, 下次启动生效")
                                    model.setTimedExitValue("[$timedExit, $freeExit]")
                                }
                            ) {
                                Column {
                                    FCard(
                                        border = FCardBorder(borderWidth = 1.0.dp),
                                        content = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    modifier = Modifier.padding(horizontal = 12.dp),
                                                    text = "运行退出",
                                                )
                                                BasicTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(vertical = 12.dp),
                                                    value = timedExit,
                                                    maxLines = 1,
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.body1,
                                                    decorationBox = { innerTextField ->
                                                        if (timedExit.isEmpty()) Text(
                                                            text = "运行超过指定时间",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                        innerTextField.invoke() // 必须调用这行哦
                                                    },
                                                    onValueChange = {
                                                        timedExit = it
                                                    },
                                                )
                                                Text(
                                                    modifier = Modifier.padding(horizontal = 12.dp),
                                                    text = "分钟",
                                                )
                                            }
                                        },
                                    )
                                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                                    FCard(
                                        border = FCardBorder(borderWidth = 1.0.dp),
                                        content = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    modifier = Modifier.padding(horizontal = 12.dp),
                                                    text = "空闲退出",
                                                )
                                                BasicTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(vertical = 12.dp),
                                                    value = freeExit,
                                                    maxLines = 1,
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.body1,
                                                    decorationBox = { innerTextField ->
                                                        if (freeExit.isEmpty()) Text(
                                                            text = "空闲超过指定时间",
                                                            style = MaterialTheme.typography.body1.copy(
                                                                color = Color(0xFF999999)
                                                            ),
                                                        )
                                                        innerTextField.invoke() // 必须调用这行哦
                                                    },
                                                    onValueChange = {
                                                        freeExit = it
                                                    },
                                                )
                                                Text(
                                                    modifier = Modifier.padding(horizontal = 12.dp),
                                                    text = "分钟",
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        SwitchItem(
                            text = "定时退出",
                            subtext = "点击设置退出时间",
                            checked = model.isTimedExit.observeAsState(false),
                            onClick = {
                                showTimedExitSettingDialog = true
                            },
                            onCheckedChange = {
                                model.changeIsTimeExit(it)
                                showRestartAppDialog = true
                            },
                        )
                    }
                    BoxWithConstraints { // 限制重构作用域
                        // 去插件化提示
                        var showDisablePluginDialog by remember { mutableStateOf(false) }
                        if (showDisablePluginDialog) {
                            FCountDownMessageDialog(
                                title = "提示",
                                confirm = "确定",
                                waitingText = "请稍后 (%d)",
                                seconds = 10,
                                onlyConfirm = true,
                                onConfirm = {
                                    showDisablePluginDialog = false
                                },
                                content = {
                                    Text(
                                        text = buildAnnotatedString {
                                            append("开启该项后只能通过")
                                            withStyle(SpanStyle(color = Color.Red)) {
                                                append("单独安装模块app")
                                            }
                                            append("进入模块设置。如果模块生效后")
                                            withStyle(SpanStyle(color = Color.Red)) {
                                                append("无法打开抖音")
                                            }
                                            append("或者")
                                            withStyle(SpanStyle(color = Color.Red)) {
                                                append("出现黑屏")
                                            }
                                            append(", 可以尝试开启该选项。开启后, ")
                                            withStyle(SpanStyle(color = Color.Red)) {
                                                append("模块功能不会丢失")
                                            }
                                            append(", 区别在于无法在抖音内部直接调整模块设置。")
                                            append("如需关闭模块, 请前往对应框架或者使用官方版本。")
                                        },
                                        style = MaterialTheme.typography.body1,
                                    )
                                },
                            )
                        }

                        SwitchItem(
                            text = "去插件化",
                            subtext = "去掉抖音内部设置，可避免大部分闪退，提高稳定性",
                            checked = model.isDisablePlugin.observeAsState(false),
                            onClick = {

                            },
                            onCheckedChange = {
                                model.changeIsDisablePlugin(it)
                                showRestartAppDialog = true
                                if (it) {
                                    showDisablePluginDialog = true
                                }
                            },
                        )
                    }
                }
            },
        )
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
                            .padding(17.dp), // switch: width = 34.dp
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
                Scaffold(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    topBar = { TopBarView() }
                ) {
                    BodyView(
                        modifier = Modifier.padding(it),
                    )
                }
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
        model.setVersionConfig(mResources.pluginAssets)
    }

    private fun buildFilterTypeStyle(value: String): AnnotatedString {
        val regex = model.videoFilterTypes
            .joinToString("|") { "(?<![^,，\\s])($it)(?![^,，\\s])" }
            .toRegex()
        return HighlightStyleBuilder(value)
            .append(regex, Color.Red)
            .build()
    }
}