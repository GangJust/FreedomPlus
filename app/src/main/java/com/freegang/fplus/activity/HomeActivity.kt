package com.freegang.fplus.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.R
import com.freegang.fplus.Themes
import com.freegang.fplus.resource.StringRes
import com.freegang.fplus.viewmodel.HomeVM
import com.freegang.ktutils.app.KAppUtils
import com.freegang.ktutils.app.KToastUtils
import com.freegang.ktutils.app.appVersionName
import com.freegang.ui.activity.FreedomSettingActivity
import com.freegang.ui.component.FCard
import com.freegang.ui.component.FMessageDialog
import com.freegang.xpler.HookPackages
import com.freegang.xpler.HookStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import kotlin.random.Random

class HomeActivity : ComponentActivity() {
    private val aliasActivityName = "com.freegang.fplus.activity.MainActivityAlias"
    private val model by viewModels<HomeVM>()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TopBarView() {
        var rotate by remember { mutableStateOf(0f) }
        val rotateAnimate by animateFloatAsState(
            targetValue = rotate,
            animationSpec = tween(durationMillis = Random.nextInt(500, 1500)),
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
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        TextView(context).apply {
                                            text = updateLog
                                            textSize = Themes.nowTypography.body1.fontSize.value
                                            setTextIsSelectable(true)
                                        }
                                    },
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
        ) {
            val context = LocalContext.current
            var visible by remember { mutableStateOf(isLauncherIconVisible(context)) }
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
                    painter = painterResource(id = if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                    contentDescription = "显示/隐藏图标",
                    tint = Themes.nowColors.icon,
                    modifier = Modifier
                        .size(20.dp)
                        .combinedClickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                visible = !visible
                                setLauncherIconVisible(context, visible)
                                KToastUtils.show(context, if (visible) "显示图标" else "隐藏图标")
                            },
                            onLongClick = {
                                throw RuntimeException("测试!")
                            }
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
        }
    }

    @Composable
    fun BodyView() {
        //旧数据迁移弹窗
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
            ) {
                Text(
                    text = showMigrateToContent,
                    style = MaterialTheme.typography.body1,
                )
            }

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
                ) {
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
            }
        }

        //view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 模块状态
            FCard(
                modifier = Modifier.padding(bottom = 24.dp, top = 12.dp),
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        content = {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val packageInfo = KAppUtils.getPackageInfo(application, HookPackages.douYinPackageName)
                                val lspatchActive = HookStatus.isLspatchActive(application, HookPackages.douYinPackageName)
                                if (lspatchActive.isNotEmpty()) {
                                    Text(
                                        text = "Lspatch加载成功!",
                                        style = Themes.nowTypography.body1,
                                    )
                                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                                    Text(
                                        text = "${lspatchActive[0]} ${lspatchActive[1]} - ${lspatchActive[2]}",
                                        style = Themes.nowTypography.body2,
                                    )
                                } else if (HookStatus.isExpModuleActive(this@HomeActivity)) {
                                    Text(
                                        text = "太极加载成功!",
                                        style = Themes.nowTypography.body1,
                                    )
                                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                                    Text(
                                        text = "已放弃太极适配, 部分功能在使用时可能出现异常",
                                        style = Themes.nowTypography.body2,
                                    )
                                } else if (HookStatus.isEnabled) {
                                    Text(
                                        text = if (HookStatus.moduleState == "Unknown") {
                                            "未知框架, 加载成功!"
                                        } else {
                                            "${HookStatus.moduleState}加载成功!"
                                        },
                                        style = Themes.nowTypography.body1,
                                    )
                                } else {
                                    Text(
                                        text = StringRes.moduleHintFailed,
                                        style = Themes.nowTypography.body1,
                                    )
                                }
                                if (packageInfo != null) {
                                    var hint by remember { mutableStateOf("自行测试功能") }
                                    LaunchedEffect("Versions") {
                                        hint = model.isSupportVersions(packageInfo.versionName)
                                    }

                                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                                    Text(
                                        text = "抖音: ${packageInfo.versionName}，$hint",
                                        style = Themes.nowTypography.body2,
                                    )
                                }
                            }
                        },
                    )
                }
            )

            //模块设置
            FCard(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            startActivity(
                                Intent(
                                    application,
                                    FreedomSettingActivity::class.java,
                                )
                            )
                        }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置",
                            tint = Themes.nowColors.icon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Column {
                            Text(
                                text = "模块设置",
                                style = Themes.nowTypography.body1,
                            )
                            Text(
                                text = if (model.isDisablePlugin) "点击跳转模块设置" else "抖音内部左上角侧滑栏/设置页，滑动至底部唤起模块设置",
                                style = Themes.nowTypography.overline,
                            )
                        }
                    },
                )
            }

            //数据目录
            FCard(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {

                        }
                    ),
            ) {
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
                            text = "数据目录: `外置存储器/DCIM/Freedom`",
                            style = Themes.nowTypography.body1,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }

            //源码地址
            FCard(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { toBrowse() }
                    ),
            ) {
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
            }

            //开源说明
            FCard(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { toEmail() }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = "开源说明",
                            tint = Themes.nowColors.icon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Column {
                            Text(
                                text = "开源说明",
                                style = Themes.nowTypography.body1,
                            )
                            Text(
                                text = "代码开源旨在个人学习，如果认为代码内容存在不当，请联系删除",
                                style = Themes.nowTypography.overline,
                            )
                        }
                    },
                )
            }

            //tg频道
            FCard(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { joinTgGroup() }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram),
                            contentDescription = "Telegram频道",
                            tint = Themes.nowColors.icon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Column {
                            Text(
                                text = "Telegram频道",
                                style = Themes.nowTypography.body1,
                            )
                            Text(
                                text = "版本发布, Bug反馈~",
                                style = Themes.nowTypography.overline,
                            )
                        }
                    },
                )
            }

            //打赏
            /*FCard(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { rewardByAlipay() }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_spicy_strips),
                            contentDescription = "请我吃辣条",
                            tint = Themes.nowColors.icon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Column {
                            Text(
                                text = "请我吃辣条",
                                style = Themes.nowTypography.body1,
                            )
                            Text(
                                text = "模块免费且开源，不强制打赏~",
                                style = Themes.nowTypography.overline,
                            )
                        }
                    },
                )
            }*/
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreedomTheme(
                window = window,
                isImmersive = true,
                isDark = false,
                followSystem = false,
            ) {
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
        }
    }

    override fun onResume() {
        super.onResume()
        model.checkVersion()
        model.updateVersions()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun toBrowse() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/GangJust/FreedomPlus"),
                )
            )
            return
        } catch (_: Exception) {

        }

        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("github link", "https://github.com/GangJust/FreedomPlus"))
        KToastUtils.show(applicationContext, "未安装浏览器，链接已复制！")
    }

    private fun toEmail() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("mailto:freegang555@gmail.com"),
                )
            )
            return
        } catch (_: Exception) {
        }

        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("feedback email", "freegang555@gmail.com"))
        Toast.makeText(applicationContext, "未安装邮箱类App，邮箱已复制！", Toast.LENGTH_SHORT).show()
    }

    private fun joinTgGroup() {
        val uri = Uri.parse("https://t.me/FreedomPlugin")
        try {
            val telegramIntent = Intent(Intent.ACTION_VIEW, uri)
            telegramIntent.setPackage("org.telegram.messenger")
            startActivity(telegramIntent)
            return
        } catch (_: Exception) {
        }

        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("telegram link", "https://t.me/FreedomPlugin"))
        KToastUtils.show(applicationContext, "未安装telegram，链接已复制！")
    }

    private fun rewardByAlipay() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("alipays://platformapi/startapp?appId=09999988&actionType=toAccount&goBack=NO&amount=3.00&userId=2088022940366251&memo=呐，拿去吃辣条!")
                )
            )
            return
        } catch (_: Exception) {
        }
        KToastUtils.show(applicationContext, "谢谢，你没有安装支付宝客户端")
    }

    private fun isLauncherIconVisible(context: Context): Boolean {
        val component = ComponentName(context, aliasActivityName)
        val intent = Intent().setComponent(component)
        val manager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }.isNotEmpty()
    }

    private fun setLauncherIconVisible(context: Context, visible: Boolean) {
        val component = ComponentName(context, aliasActivityName)
        val manager = context.packageManager
        manager.setComponentEnabledSetting(
            component,
            if (visible) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}