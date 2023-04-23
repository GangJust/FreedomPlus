package com.freegang.xpler.utils.app

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.PermissionChecker
import java.io.File
import kotlin.io.path.ExperimentalPathApi

object KAppUtils {

    /**
     * 返回某个App的版本名
     *
     * @param context Context
     * @param packageName 包名(需要App已安装)
     * @return String
     */
    @JvmStatic
    @JvmOverloads
    fun getVersionName(context: Context, packageName: String = context.packageName): String {
        return getPackageInfo(context, packageName).versionName
    }

    /**
     * 返回某个App的版本号
     *
     * @param context Context
     * @param packageName 包名(需要App已安装)
     * @return Long
     */
    @JvmStatic
    @JvmOverloads
    fun getVersionCode(context: Context, packageName: String = context.packageName): Long {
        val packageInfo = getPackageInfo(context, packageName)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }

    /**
     * 返回某个App的基本信息
     *
     * @param context Context
     * @param packageName 包名(需要App已安装)
     * @return PackageInfo
     */
    @JvmStatic
    @JvmOverloads
    fun getPackageInfo(context: Context, packageName: String = context.packageName): PackageInfo {
        return context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    }

    /**
     * 返回某个Apk的版本名
     *
     * @param context Context
     * @param apkFile apk文件路径
     * @return String
     */
    @JvmStatic
    fun getApkVersionName(context: Context, apkFile: File): String? {
        return getApkPackageInfo(context, apkFile)?.versionName
    }

    /**
     * 返回某个Apk的版本号
     *
     * @param context Context
     * @param apkFile apk文件路径
     * @return Long
     */
    @JvmStatic
    fun getApkVersionCode(context: Context, apkFile: File): Long? {
        val packageInfo = getApkPackageInfo(context, apkFile)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode
        } else {
            packageInfo?.versionCode?.toLong()
        }
    }

    /**
     * 返回某个Apk的基本信息
     *
     * @param context Context
     * @param apkFile apk文件路径
     * @return PackageInfo
     */
    @JvmStatic
    fun getApkPackageInfo(context: Context, apkFile: File): PackageInfo? {
        return context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_ACTIVITIES)
    }

    /**
     * 如果[versionName1]等于[versionName2]，则返回一个等于 0 的值;
     * 如果[versionName1]小于[versionName2]，则返回一个小于 0 的值;
     * 如果[versionName1]大于[versionName2]，则返回一个大于 0 的值;
     *
     * @param versionName1 versionName1
     * @param versionName2 versionName2
     * @return Int
     */
    @JvmStatic
    fun compareVersionName(versionName1: String, versionName2: String): Int {
        return versionName1.compareTo(versionName2, true)
    }

    /**
     * 判断是否具有某个权限
     * @param application Application
     * @param permission 权限名
     */
    @JvmStatic
    fun checkPermission(application: Application, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 管理外部存储
            if (permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE) return Environment.isExternalStorageManager()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //悬浮窗
            if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) return Settings.canDrawOverlays(application)
            //修改系统设置
            if (permission == Manifest.permission.WRITE_SETTINGS) return Settings.System.canWrite(application)
        }
        return PermissionChecker.checkSelfPermission(application, permission) == PermissionChecker.PERMISSION_GRANTED
    }
}

///
val Context.appLabelName get() = this.resources.getString(KAppUtils.getPackageInfo(this).applicationInfo.labelRes)

val Context.appVersionName get() = KAppUtils.getVersionName(this)

val Context.appVersionCode get() = KAppUtils.getVersionCode(this)