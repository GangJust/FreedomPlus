package com.freegang.fplus.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.unit.sp
import androidx.core.content.PermissionChecker
import com.freegang.fplus.FreedomTheme
import com.freegang.fplus.Themes
import com.freegang.fplus.component.FDialog
import com.freegang.xpler.utils.io.KStorageUtils.hasOperationStorage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    /// 检查是否具有某个权限
    private fun checkPermission(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(application, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    /// 需要的权限列表
    private fun getPermissionList(): List<String> {
        val permissions = mutableListOf<String>()
        //是否具有外置存储器读
        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        //是否具有外置存储器写
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        //是否具有外置存储器管理(特殊权限)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        return permissions
    }

    /// 跳转对应权限修改页
    private fun requestAgainPermissions(deniedPermissions: List<String>) {
        //处理未请求成功的权限
        //应用设置页, 开启外部存储读写权限, 跳转
        if (deniedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) || deniedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${packageName}")
                )
            )
            return
        }

        //Android11 必须要的管理外部存储完全管理权限, 跳转
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (deniedPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE) && !Environment.isExternalStorageManager()) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${packageName}")
                    )
                )
            }
            return
        }
    }

    private fun toHomeActivity() {
        if (getPermissionList().isEmpty()) {
            startActivity(Intent(application, HomeActivity::class.java))
            finish()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        toHomeActivity()
        super.onCreate(savedInstanceState)
        setContent {
            val permissionsState = rememberMultiplePermissionsState(getPermissionList())
            FreedomTheme(window = window) {
                FDialog(
                    title = "Freedom+需要以下权限才能正常运行",
                    cancel = "取消",
                    confirm = "确定",
                    onCancel = {
                        finish()
                    },
                    onConfirm = {
                        if (getPermissionList().isNotEmpty()) {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
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

    override fun onResume() {
        toHomeActivity()
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Log.d("GLog", "requestCode=$requestCode ,resultCode=${permissions.joinToString()} ,grantResults=${grantResults.joinToString()}")
        if (getPermissionList().isNotEmpty()) {
            Toast.makeText(application, "请开启必要权限", Toast.LENGTH_SHORT).show()
            requestAgainPermissions(permissions.filterIndexed { i, _ -> grantResults[i] == -1 })
        } else {
            toHomeActivity()
        }
    }
}