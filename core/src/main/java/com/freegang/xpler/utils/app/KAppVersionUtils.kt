package com.freegang.xpler.utils.app

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object KAppVersionUtils {

    /**
     * 返回某个App的版本名
     *
     * @param application application
     * @param packageName 包名(需要App已安装)
     * @return String
     */
    @JvmStatic
    @JvmOverloads
    fun getVersionName(application: Application, packageName: String = application.packageName): String {
        return getPackageInfo(application, packageName).versionName
    }

    /**
     * 返回某个App的版本号
     *
     * @param application application
     * @param packageName 包名(需要App已安装)
     * @return Long
     */
    @JvmStatic
    @JvmOverloads
    fun getVersionCode(application: Application, packageName: String = application.packageName): Long {
        val packageInfo = getPackageInfo(application, packageName)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }

    /**
     * 返回某个App的基本信息
     *
     * @param application application
     * @param packageName 包名(需要App已安装)
     * @return PackageInfo
     */
    @JvmStatic
    @JvmOverloads
    fun getPackageInfo(application: Application, packageName: String = application.packageName): PackageInfo {
        return application.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    }

    /**
     * 返回某个Apk的版本名
     *
     * @param application application
     * @param apkFile apk文件路径
     * @return String
     */
    @JvmStatic
    fun getApkVersionName(application: Application, apkFile: File): String? {
        return getApkPackageInfo(application, apkFile)?.versionName
    }

    /**
     * 返回某个Apk的版本号
     *
     * @param application application
     * @param apkFile apk文件路径
     * @return Long
     */
    @JvmStatic
    fun getApkVersionCode(application: Application, apkFile: File): Long? {
        val packageInfo = getApkPackageInfo(application, apkFile)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode
        } else {
            packageInfo?.versionCode?.toLong()
        }
    }

    /**
     * 返回某个Apk的基本信息
     *
     * @param application application
     * @param apkFile apk文件路径
     * @return PackageInfo
     */
    @JvmStatic
    fun getApkPackageInfo(application: Application, apkFile: File): PackageInfo? {
        return application.packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_ACTIVITIES)
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

    ///
    val Application.appVersionName
        get() = getVersionName(this)

    val Application.appVersionCode
        get() = getVersionCode(this)
}