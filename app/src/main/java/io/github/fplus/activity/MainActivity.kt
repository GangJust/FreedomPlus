package io.github.fplus.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import io.github.fplus.FreedomTheme
import io.github.fplus.Themes
import io.github.fplus.core.config.ConfigV1
import io.github.fplus.core.ui.component.FMessageDialog

class MainActivity : ComponentActivity() {
    /// Android 11+
    private val startActivityForResultByStorageManager =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 管理外部存储权限
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(applicationContext, "Android11+ 必须申请该权限!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    /// Android 11-
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startHomeActivity()
            } else {
                Toast.makeText(applicationContext, "请开启必要权限!", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${packageName}")
                    )
                )
            }
        }

    /// 检查是否具有某个权限
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    /// 外置存储器读/写权限
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android11+ 检查管理外部存储权限
            if (!Environment.isExternalStorageManager()) {
                startActivityForResultByStorageManager.launch(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${packageName}")
                    )
                )
            }
        } else {
            // Android11- 检查文件读写权限
            if (!permissions.all { checkPermission(it) }) {
                requestMultiplePermissions.launch(permissions)
            }
        }
    }

    /// 进入App
    private fun startHomeActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) return
        } else {
            if (!permissions.all { checkPermission(it) }) return
        }

        ConfigV1.initialize(application)
        startActivity(Intent(application, HomeActivity::class.java))
        finish()
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
                FMessageDialog(
                    title = "Freedom+需要以下权限才能正常运行",
                    cancel = "取消",
                    confirm = "确定",
                    onCancel = { finish() },
                    onConfirm = { requestPermissions() },
                ) {
                    Column {
                        Text(
                            text = "外置存储器读/写权限",
                            style = Themes.nowTypography.body1.copy(
                                color = Themes.nowColors.subtitle,
                                lineHeight = 1.2.sp,
                            ),
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Text(
                                text = "外置存储器管理权限(Android11+)",
                                style = Themes.nowTypography.body1.copy(
                                    color = Themes.nowColors.subtitle,
                                    lineHeight = 1.2.sp
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startHomeActivity()
    }

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
}