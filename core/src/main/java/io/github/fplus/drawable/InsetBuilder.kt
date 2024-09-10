package io.github.fplus.drawable

import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import io.github.fplus.drawable.DrawableBuilder.Companion.dp2px
import kotlin.math.roundToInt

/**
 * 创建时间 2019/1/3
 * 描述     inset样式的构造器
 */
class InsetBuilder : DrawableBuilder {
    var drawable: Drawable? = null
    private var mInsetLeft = 0f
    private var mInsetRight = 0f
    private var mInsetTop = 0f
    private var mInsetBottom = 0f

    /**
     * 设置统一的边距
     * @param inset 统一的边距
     */
    fun setInset(inset: Int, isDp: Boolean = true): InsetBuilder {
        val insetPx = if (isDp) dp2px(inset.toFloat()) else inset.toFloat()
        mInsetLeft = insetPx
        mInsetRight = insetPx
        mInsetTop = insetPx
        mInsetBottom = insetPx
        return this
    }

    /**
     * 单独设置边距
     *
     * @param insetLeft   左边距
     * @param insetTop    上边距
     * @param insetRight  右边距
     * @param insetBottom 下边距
     */
    fun setInset(
        insetLeft: Int,
        insetTop: Int,
        insetRight: Int,
        insetBottom: Int,
        isDp: Boolean = true
    ): InsetBuilder {
        mInsetLeft = if (isDp) dp2px(insetLeft.toFloat()) else insetLeft.toFloat()
        mInsetRight = if (isDp) dp2px(insetRight.toFloat()) else insetRight.toFloat()
        mInsetTop = if (isDp) dp2px(insetTop.toFloat()) else insetTop.toFloat()
        mInsetBottom = if (isDp) dp2px(insetBottom.toFloat()) else insetBottom.toFloat()
        return this
    }

    override fun build(): Drawable {
        return InsetDrawable(
            drawable,
            mInsetLeft.roundToInt(),
            mInsetTop.roundToInt(),
            mInsetRight.roundToInt(),
            mInsetBottom.roundToInt()
        )
    }

}