package io.github.fplus.core.ui.activity

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
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
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import io.github.fplus.core.R
import io.github.fplus.core.helper.HighlightStyleBuilder
import io.github.fplus.core.ui.ModuleTheme
import io.github.fplus.core.ui.asDp
import io.github.fplus.core.ui.component.FCard
import io.github.fplus.core.ui.component.FCardBorder
import io.github.fplus.core.ui.component.FMessageDialog
import io.github.fplus.core.ui.component.FWaitingMessageDialog
import io.github.fplus.core.ui.viewmodel.FreedomSettingVM
import io.github.fplus.plugin.activity.XplerActivity
import io.github.webdav.WebDav
import io.github.xpler.HookConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.system.exitProcess

class FreedomSettingActivity : XplerActivity() {
    private val model by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(FreedomSettingVM::class.java)
    }

    private var isModuleStart = false
    private var isDark = false

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
                                model.clearDexkitCache()
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
                                    if (!model.hasDexkitCache) {
                                        KToastUtils.show(application, "没有类日志!")
                                        return@combinedClickable
                                    }
                                    showLogDialog = true
                                },
                                onClick = {
                                    if (!model.hasDexkitCache) {
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
                        ) {
                            LazyColumn(
                                modifier = Modifier,
                            ) {
                                item {
                                    SelectionContainer {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = updateLog,
                                            style = MaterialTheme.typography.body1,
                                        )
                                    }
                                }
                            }
                        }
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
                                        withContext(Dispatchers.IO) {
                                            runCatching {
                                                // pluginAssets
                                                assets
                                                    .open("update.log")
                                                    .use {
                                                        updateLog = it
                                                            .readBytes()
                                                            .decodeToString()
                                                    }
                                            }.onFailure {
                                                withContext(Dispatchers.Main) {
                                                    KToastUtils.show(application, "更新日志获取失败")
                                                }
                                            }
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
        var showRestartAppDialog by remember { mutableStateOf(false) }

        if (showRestartAppDialog) {
            if (application.packageName != HookConfig.modulePackageName) {
                FMessageDialog(
                    title = "提示",
                    cancel = "取消",
                    confirm = "重启",
                    onCancel = {
                        showRestartAppDialog = false
                    },
                    onConfirm = {
                        showRestartAppDialog = false
                        runCatching {
                            // model.setVersionConfig(pluginAssets)
                            model.setVersionConfig(assets)
                        }.onFailure {
                            model.setVersionConfig(null)
                        }
                        KAppUtils.restartApplication(application)
                    },
                ) {
                    Text(
                        text = "需要重启应用生效, 若未重启请手动重启",
                        style = MaterialTheme.typography.body1,
                    )
                }
            }
        }

        LazyColumn(
            modifier = modifier,
        ) {
            item {
                var showTipsDialog by remember { mutableStateOf(false) }
                var showSettingDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "视频/图文/音乐下载",
                    subtext = "点击调整相关设置",
                    checked = model.isDownload.observeAsState(false),
                    onClick = {
                        showSettingDialog = true
                    },
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

                if (showSettingDialog) {
                    FMessageDialog(
                        title = "相关设置",
                        confirm = "确定",
                        onlyConfirm = true,
                        onConfirm = {
                            showSettingDialog = false
                        }
                    ) {
                        Column {
                            CheckBoxItem(
                                text = "视频创作者单独创建文件夹",
                                checked = model.isOwnerDir.observeAsState(false),
                                onCheckedChange = {
                                    model.changeIsOwnerDir(it)
                                }
                            )
                            CheckBoxItem(
                                text = "通知栏显示下载进度",
                                checked = model.isNotification.observeAsState(false),
                                onCheckedChange = {
                                    model.changeIsNotification(it)
                                }
                            )
                        }
                    }
                }
            }
            item {
                var showSettingDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "保存表情包/评论视频、图片",
                    subtext = "点击调整相关设置",
                    checked = model.isEmoji.observeAsState(false),
                    onClick = {
                        showSettingDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsEmoji(it)
                    }
                )

                if (showSettingDialog) {
                    FMessageDialog(
                        title = "相关设置",
                        confirm = "确定",
                        onlyConfirm = true,
                        onConfirm = {
                            showSettingDialog = false
                        }
                    ) {
                        Column {
                            CheckBoxItem(
                                text = "震动反馈",
                                checked = model.isVibrate.observeAsState(false),
                                onCheckedChange = {
                                    model.changeIsVibrate(it)
                                }
                            )
                        }
                    }
                }
            }
            item {
                var showTransparentDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "首页控件半透明",
                    subtext = "点击调整透明度",
                    checked = model.isTranslucent.observeAsState(false),
                    onClick = {
                        showTransparentDialog = true
                    },
                    onCheckedChange = {
                        showRestartAppDialog = true
                        model.changeIsTranslucent(it)
                    }
                )

                if (showTransparentDialog) {
                    val translucentValue = model.translucentValue.value ?: listOf(50, 50, 50)
                    var topBarTransparent by remember { mutableStateOf(translucentValue[0]) }
                    var videoAssemblyTransparent by remember { mutableStateOf(translucentValue[1]) }
                    var bottomBarTransparent by remember { mutableStateOf(translucentValue[2]) }

                    FMessageDialog(
                        title = "自定义控件透明度",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = {
                            showTransparentDialog = false
                            model.changeTranslucentValue(
                                listOf(
                                    topBarTransparent,
                                    videoAssemblyTransparent,
                                    bottomBarTransparent,
                                )
                            )
                            KToastUtils.show(application, "切换视频或重启抖音生效")
                        },
                    ) {
                        Column {
                            Text(
                                text = "顶部导航: $topBarTransparent",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Slider(
                                value = topBarTransparent.toFloat(),
                                valueRange = 0f..100f,
                                onValueChange = {
                                    topBarTransparent = it.toInt()
                                },
                            )
                            Text(
                                text = "视频控件: $videoAssemblyTransparent",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Slider(
                                value = videoAssemblyTransparent.toFloat(),
                                valueRange = 0f..100f,
                                onValueChange = {
                                    videoAssemblyTransparent = it.toInt()
                                },
                            )
                            Text(
                                text = "底部导航: $bottomBarTransparent",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Slider(
                                value = bottomBarTransparent.toFloat(),
                                valueRange = 0f..100f,
                                onValueChange = {
                                    bottomBarTransparent = it.toInt()
                                },
                            )
                        }
                    }
                }
            }
            item {
                SwitchItem(
                    text = "移除悬浮挑战/评论贴纸",
                    checked = model.isRemoveSticker.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsRemoveSticker(it)
                    }
                )
            }
            item {
                var showDoubleClickModeDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "双击视频响应类型",
                    subtext = "点击调整双击视频响应方式",
                    checked = model.isDoubleClickType.observeAsState(false),
                    onClick = {
                        showDoubleClickModeDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsDoubleClickType(it)
                    }
                )

                if (showDoubleClickModeDialog) {
                    var radioIndex by remember { mutableStateOf(model.doubleClickType.value ?: 2) }
                    FMessageDialog(
                        title = "请选择双击响应模式",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = { showDoubleClickModeDialog = false },
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = radioIndex == 0,
                                    onClick = {
                                        radioIndex = 0
                                        model.changeDoubleClickType(radioIndex)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "点赞视频",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                        }
                    }
                }
            }
            item {
                SwitchItem(
                    text = "视频时长超过10分钟提示",
                    subtext = "避免你die在厕所",
                    checked = model.isLongtimeVideoToast.observeAsState(false),
                    onCheckedChange = {
                        model.changeIsLongtimeVideoToast(it)
                    }
                )
            }
            item {
                var showIsDisablePhotoDialog by remember { mutableStateOf(false) }

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

                if (showIsDisablePhotoDialog) {
                    var radioIndex by remember { mutableStateOf(model.photoButtonType.value ?: 2) }
                    FMessageDialog(
                        title = "请选择拍摄按钮响应模式",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = { showIsDisablePhotoDialog = false },
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = radioIndex == 0,
                                    onClick = {
                                        radioIndex = 0
                                        model.changePhotoButtonType(radioIndex)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
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
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "移除按钮",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                        }
                    }
                }
            }
            item {
                var showFilterDialog by remember { mutableStateOf(false) }
                var showFilterTips by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "视频过滤",
                    subtext = "点击设置视频过滤类型或文本关键字",
                    checked = model.isVideoFilter.observeAsState(false),
                    onClick = {
                        showFilterDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsVideoFilter(it)
                    }
                )

                if (showFilterDialog) {
                    var inputValue by remember { mutableStateOf(model.videoFilterKeywords.value ?: "") }
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
                                                showFilterTips = true
                                            },
                                        ),
                                )
                            }
                        },
                        cancel = "取消",
                        confirm = "确定",
                        onCancel = {
                            showFilterDialog = false
                        },
                        onConfirm = {
                            showFilterDialog = false
                            model.setVideoFilterKeywords(inputValue)
                        },
                    ) {
                        FCard(
                            border = FCardBorder(borderWidth = 1.0.dp),
                        ) {
                            BasicTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                value = inputValue,
                                maxLines = 4,
                                textStyle = MaterialTheme.typography.body1,
                                decorationBox = { innerTextField ->
                                    if (inputValue.isEmpty()) {
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
                        }
                    }
                }

                if (showFilterTips) {
                    FMessageDialog(
                        title = "提示",
                        onlyConfirm = true,
                        confirm = "确定",
                        onConfirm = {
                            showFilterTips = false
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
            }
            item {
                var showFilterDialog by remember { mutableStateOf(false) }
                var showFilterTips by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "弹窗过滤",
                    subtext = "点击设置弹窗文本关键字",
                    checked = model.isDialogFilter.observeAsState(false),
                    onClick = {
                        showFilterDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsDialogFilter(it)
                    }
                )

                if (showFilterDialog) {
                    var inputValue by remember { mutableStateOf(model.dialogFilterKeywords.value ?: "") }
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
                                                showFilterTips = true
                                            },
                                        ),
                                )
                            }
                        },
                        cancel = "取消",
                        confirm = "确定",
                        onCancel = {
                            showFilterDialog = false
                        },
                        onConfirm = {
                            showFilterDialog = false
                            model.setDialogFilterKeywords(inputValue)
                        },
                    ) {
                        Column {
                            FCard(
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    value = inputValue,
                                    maxLines = 4,
                                    textStyle = MaterialTheme.typography.body1,
                                    decorationBox = { innerTextField ->
                                        if (inputValue.isEmpty()) {
                                            Text(
                                                text = "弹窗文本中出现的关键字",
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
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            CheckBoxItem(
                                text = "弹窗被关闭时提示",
                                checked = model.dialogDismissTips.observeAsState(initial = false),
                                onCheckedChange = {
                                    model.changeDialogDismissTips(it)
                                },
                            )
                        }
                    }
                }

                if (showFilterTips) {
                    FMessageDialog(
                        title = "提示",
                        onlyConfirm = true,
                        confirm = "确定",
                        onConfirm = {
                            showFilterTips = false
                        }
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("弹窗中出现的文本关键字，建议多写几个字，如：")
                                withStyle(SpanStyle(color = Color.Red)) {
                                    append("发现新版本")
                                }
                                append("，而不是")
                                withStyle(SpanStyle(color = Color.Red)) {
                                    append("版本")
                                }
                                append("，否则可能造成部分必要弹窗被禁止显示。")
                            },
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }

            }
            item {
                var showLongPressModeDialog by remember { mutableStateOf(false) }

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

                if (showLongPressModeDialog) {
                    var longPressMode by remember { mutableStateOf(model.longPressMode.value ?: true) }
                    FMessageDialog(
                        title = "请选择响应模式",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = { showLongPressModeDialog = false },
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = longPressMode,
                                    onClick = {
                                        longPressMode = true
                                        model.changeLongPressMode(longPressMode)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "长按视频上半",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !longPressMode,
                                    onClick = {
                                        longPressMode = false
                                        model.changeLongPressMode(longPressMode)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "长按视频下半",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                        }
                    }
                }
            }
            item {
                var showSettingDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "全屏沉浸",
                    subtext = "全屏沉浸式播放, 会造成视频剪辑拉伸, 点击调整设置",
                    checked = model.isImmersive.observeAsState(false),
                    onClick = {
                        showSettingDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsImmersive(it)
                        showRestartAppDialog = true
                    }
                )

                if (showSettingDialog) {
                    val systemControllerValue = model.systemControllerValue.value ?: listOf(false, false)
                    val isHideStatusBar = remember { mutableStateOf(systemControllerValue[0]) }
                    val isHideNavigateBar = remember { mutableStateOf(systemControllerValue[1]) }
                    FMessageDialog(
                        title = "请选择系统隐藏项",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = {
                            showSettingDialog = false
                            model.changeSystemControllerValue(
                                listOf(
                                    isHideStatusBar.value,
                                    isHideNavigateBar.value,
                                )
                            )
                        },
                    ) {
                        Column {
                            CheckBoxItem(
                                text = "隐藏状态栏",
                                checked = isHideStatusBar,
                                onCheckedChange = {
                                    isHideStatusBar.value = it
                                },
                            )

                            CheckBoxItem(
                                text = "隐藏导航栏",
                                checked = isHideNavigateBar,
                                onCheckedChange = {
                                    isHideNavigateBar.value = it
                                },
                            )
                        }
                    }
                }
            }
            item {
                var showCommentColorModeDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "评论区颜色模式",
                    subtext = "点击调整评论区颜色模式",
                    checked = model.isCommentColorMode.observeAsState(false),
                    onClick = {
                        showCommentColorModeDialog = true
                    },
                    onCheckedChange = {
                        model.changeIsCommentColorMode(it)
                        showRestartAppDialog = true
                    }
                )

                if (showCommentColorModeDialog) {
                    var radioIndex by remember { mutableStateOf(model.commentColorMode.value ?: 0) }
                    FMessageDialog(
                        title = "请选择评论区颜色模式",
                        confirm = "更改",
                        onlyConfirm = true,
                        onConfirm = {
                            showCommentColorModeDialog = false
                            KToastUtils.show(application, "重启抖音生效")
                        },
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = radioIndex == 0,
                                    onClick = {
                                        radioIndex = 0
                                        model.changeCommentColorMode(radioIndex)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "浅色模式",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = radioIndex == 1,
                                    onClick = {
                                        radioIndex = 1
                                        model.changeCommentColorMode(radioIndex)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "深色模式",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = radioIndex == 2,
                                    onClick = {
                                        radioIndex = 2
                                        model.changeCommentColorMode(radioIndex)
                                    },
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "跟随主题",
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                        }
                    }
                }
            }
            item {
                var showHideTabKeywordsEditorDialog by remember { mutableStateOf(false) }
                var showHideTabTipsDialog by remember { mutableStateOf(false) }

                SwitchItem(
                    text = "隐藏顶部选项",
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
                    ) {
                        FCard(
                            border = FCardBorder(borderWidth = 1.0.dp),
                        ) {
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
                        }
                    }
                }

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
                    ) {
                        Text(
                            text = "一旦开启顶部选项隐藏, 将禁止左右滑动切换, 具体效果自行查看!",
                            style = MaterialTheme.typography.body1,
                        )
                    }
                }
            }
            item {
                var showWebDavConfigEditorDialog by remember { mutableStateOf(false) }
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
                                KToastUtils.show(application, msg)
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
                                KToastUtils.show(application, msg)
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
                    ) {
                        Column {
                            FCard(
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
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
                            }
                            FCard(
                                modifier = Modifier.padding(vertical = 8.dp),
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
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
                            }
                            FCard(
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
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
                            }
                        }
                    }
                }
            }
            item {
                var showTimedExitSettingDialog by remember { mutableStateOf(false) }

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

                if (showTimedExitSettingDialog) {
                    val times = model.timedShutdownValue.value ?: listOf(10, 3)
                    var timedExit by remember { mutableStateOf("${times[0]}") }
                    var freeExit by remember { mutableStateOf("${times[1]}") }

                    KToastUtils.show(application, "低于3分钟将不执行~")
                    FMessageDialog(
                        title = "定时退出时间设置",
                        cancel = "取消",
                        confirm = "确定",
                        onCancel = {
                            showTimedExitSettingDialog = false
                        },
                        onConfirm = {
                            showTimedExitSettingDialog = false
                            val intTimedExit = timedExit.toIntOrNull() ?: -1
                            val intFreeExit = freeExit.toIntOrNull() ?: -1
                            if (intTimedExit < 0 || intFreeExit < 0) {
                                KToastUtils.show(application, "请输入正确的分钟数")
                                return@FMessageDialog
                            }
                            KToastUtils.show(application, "设置成功, 下次启动生效")
                            model.setTimedShutdownValue(listOf(intTimedExit, intFreeExit))
                        }
                    ) {
                        Column {
                            FCard(
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
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
                            }
                            Spacer(modifier = Modifier.padding(vertical = 4.dp))
                            FCard(
                                border = FCardBorder(borderWidth = 1.0.dp),
                            ) {
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
                            }
                        }
                    }
                }
            }
            /*item {
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
                    ) {
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
                    }
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
            }*/
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
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
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
            }
            if (isWaiting) {
                BoxWithConstraints(
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .padding(17.dp), // switch: width = 34.dp
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(MaterialTheme.typography.body1.fontSize.asDp),
                    )
                }
            } else {
                Switch(
                    checked = checked.value,
                    onCheckedChange = {
                        onCheckedChange.invoke(it)
                    },
                )
            }
        }
    }

    @Composable
    private fun CheckBoxItem(
        text: String,
        checked: State<Boolean>,
        onCheckedChange: ((Boolean) -> Unit)?,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked.value,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
            )
        }
    }

    private fun buildFilterTypeStyle(value: String): AnnotatedString {
        val regex = model.videoFilterTypes
            .joinToString("|") { "(?<![^,，\\s])($it)(?![^,，\\s])" }
            .toRegex()
        return HighlightStyleBuilder(value)
            .append(regex, Color.Red)
            .build()
    }

    private fun initExtra() {
        isModuleStart = intent.getBooleanExtra("isModuleStart", false)
        isDark = intent.getBooleanExtra("isDark", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExtra()
        setContent {
            ModuleTheme(
                isDark = isDark,
                followSystem = false,
            ) {
                Scaffold(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    topBar = {
                        TopBarView()
                    }
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
    }

    override fun onPause() {
        super.onPause()
        runCatching {
            // model.setVersionConfig(pluginAssets)
            model.setVersionConfig(assets)
        }.onFailure {
            model.setVersionConfig(null)
        }
    }

    override fun finish() {
        super.finish()
        if (isModuleStart) exitProcess(0)
    }
}