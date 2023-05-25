package com.freegang.xpler.utils.other

import android.content.Context

object KNumUtils {
    /**
     * 将dp值转换为px值
     * @param context 上下文
     * @param dpValue 需要转换的dp值
     * @return 转换后的px值
     */
    fun dp2Px(context: Context, dpValue: Float): Float {
        val density = context.resources.displayMetrics.density
        return dpValue * density
    }

    /**
     * 将px值转换为dp值
     * @param context 上下文
     * @param pxValue 需要转换的px值
     * @return 转换后的dp值
     */
    fun px2Dp(context: Context, pxValue: Float): Float {
        val density = context.resources.displayMetrics.density
        return pxValue / density
    }

    /**
     * 将sp值转换为px值
     * @param context 上下文
     * @param spValue 需要转换的sp值
     * @return 转换后的px值
     */
    fun sp2Px(context: Context, spValue: Float): Float {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return spValue * scaledDensity
    }

    /**
     * 将px值转换为sp值
     * @param context 上下文
     * @param pxValue 需要转换的px值
     * @return 转换后的sp值
     */
    fun px2Sp(context: Context, pxValue: Float): Float {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return pxValue / scaledDensity
    }
}

/// 简化循环
fun Int.forTo(to: Int, both: Boolean = false, block: (index: Int) -> Unit) {
    //10 -> 0
    if (to < this) {
        val finalTo = if (both) to else to + 1
        for (i in this downTo finalTo) block.invoke(i)
    } else { // 0 -> 10
        val finalTo = if (both) to else to - 1
        for (i in this..finalTo) block.invoke(i)
    }
}

/// 简化计算
fun <T> Int.forCalc(from: Int, initValue: T, both: Boolean = false, block: (previous: T) -> T): T {
    var result: T = initValue

    if (both) {
        for (i in from..this) {
            result = block.invoke(result)
        }
        return result
    }

    for (i in from until this) {
        result = block.invoke(result)
    }
    return result
}

///
fun Context.dp2Px(dpValue: Float) = KNumUtils.dp2Px(this, dpValue)

fun Context.px2Dp(pxValue: Float) = KNumUtils.dp2Px(this, pxValue)

fun Context.sp2Px(spValue: Float) = KNumUtils.sp2Px(this, spValue)

fun Context.px2Sp(pxValue: Float) = KNumUtils.px2Sp(this, pxValue)

