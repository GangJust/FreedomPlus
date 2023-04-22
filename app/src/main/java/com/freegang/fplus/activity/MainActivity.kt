package com.freegang.fplus.activity

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
import androidx.core.content.PermissionChecker
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.Themes
import com.freegang.fplus.component.FMessageDialog

class MainActivity : ComponentActivity() {

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        val denied = map.filterValues { !it }.mapNotNull { it.key }
        if (denied.isEmpty()) {
            toHomeActivity()
            return@registerForActivityResult
        }

        //处理未授予权限
        Toast.makeText(applicationContext, "请开启必要权限!", Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${packageName}")
            )
        )
    }

    /// 检查是否具有某个权限
    private fun checkPermission(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(application, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    /// 请求权限
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  //Android11+ 必须要的管理外部存储完全管理权限, 跳转
            if (!Environment.isExternalStorageManager()) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${packageName}")
                    )
                )
                return
            }
        } else {  //Android11以下, 必须要的外部存储读写权限
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            if (!checkPermission(permissions[0]) || !checkPermission(permissions[1])) {
                requestMultiplePermissions.launch(permissions)
                return
            }
        }
        toHomeActivity()
    }

    /// 进入App
    private fun toHomeActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) return
        } else {
            if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return
        }

        startActivity(Intent(application, HomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreedomTheme(window = window) {
                FMessageDialog(
                    title = "Freedom+需要以下权限才能正常运行",
                    cancel = "取消",
                    confirm = "确定",
                    onCancel = { finish() },
                    onConfirm = { requestPermissions() },
                    content = {
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
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        toHomeActivity()
    }
}