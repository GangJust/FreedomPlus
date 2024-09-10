package io.github.fplus.drawable

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.github.fplus.drawable.DrawableBuilder.Companion.cxt
import io.github.fplus.drawable.DrawableBuilder.Companion.dp2px
import io.github.fplus.drawable.DrawableBuilder.Companion.string2Color
import java.lang.reflect.Field
import kotlin.math.roundToInt


enum class Shape {
    /*** Shape is a rectangle, possibly with rounded corners*/
    RECTANGLE,

    /*** Shape is an ellipse*/
    OVAL,

    /*** Shape is a line*/
    LINE,

//    /*** Shape is a ring.*/
//    RING,
}

/**
 * 创建时间 2019/1/3
 * 描述     shape样式的构建类，所有距离值均可传dp
 */
class ShapeBuilder : DrawableBuilder {

    private var mRadius = 0f
    private var mRadii: FloatArray? = null
    private var mWidth = 0f
    private var mHeight = 0f
    private var mStrokeWidth = 0f
    private var mDashWidth = 0f
    private var mDashGap = 0f
    private var mShape = GradientDrawable.RECTANGLE
    private var mSolidColor = 0
    private var mStrokeColor = 0
    private var mGradientColor: IntArray? = null
    private var mAngle = 0
    private var mPadding: Rect? = null

    /**
     * 设置四个角的圆角
     *
     * @param radius 圆角的半径
     */
    fun corner(radius: Float, isDp: Boolean = true): ShapeBuilder {
        mRadius = if (isDp) dp2px(radius) else radius
        return this
    }

    /**
     * 设置水平方向的圆角
     *
     * @param radius 圆角的半径
     */
    fun cornerHorizontal(radius: Float, isDp: Boolean = true): ShapeBuilder {
        val radiusPx = if (isDp) dp2px(radius) else radius
        cornerOnly(radiusPx, radiusPx, 0f, 0f, isDp)
        return this
    }

    /**
     * 设置垂直方向的圆角
     *
     * @param radius 圆角的半径
     */
    fun cornerVertical(radius: Float, isDp: Boolean = true): ShapeBuilder {
        val radiusPx = if (isDp) dp2px(radius) else radius
        cornerOnly(0f, 0f, radiusPx, radiusPx, isDp)
        return this
    }

    /**
     * 分别设置四个角的圆角
     *
     * @param topLeft     左上角圆角半径
     * @param topRight    右上角圆角半径
     * @param bottomLeft  左下角圆角半径
     * @param bottomRight 右下角圆角半径
     */
    fun cornerOnly(
        topLeft: Float = 0f,
        topRight: Float = 0f,
        bottomLeft: Float = 0f,
        bottomRight: Float = 0f,
        isDp: Boolean = true
    ): ShapeBuilder {
        val topLeftPx = if (isDp) dp2px(topLeft) else topLeft
        val topRightPx = if (isDp) dp2px(topRight) else topRight
        val bottomRightPx = if (isDp) dp2px(bottomRight) else bottomRight
        val bottomLeftPx = if (isDp) dp2px(bottomLeft) else bottomLeft
        mRadii = floatArrayOf(
            topLeftPx, topLeftPx,
            topRightPx, topRightPx,
            bottomRightPx, bottomRightPx,
            bottomLeftPx, bottomLeftPx
        )
        return this
    }

    /**
     * 设置padding
     *
     * @param padding padding值
     */
    fun padding(padding: Float, isDp: Boolean = true): ShapeBuilder {
        return paddingOnly(padding, padding, padding, padding, isDp)
    }

    /**
     * 设置水平方向的padding
     *
     * @param padding padding值
     */
    fun paddingHorizontal(padding: Float, isDp: Boolean = true): ShapeBuilder {
        return paddingOnly(padding, 0f, padding, 0f, isDp)
    }

    /**
     * 设置垂直方向的padding
     *
     * @param padding padding值
     */
    fun paddingVertical(padding: Float, isDp: Boolean = true): ShapeBuilder {
        return paddingOnly(0f, padding, 0f, padding, isDp)
    }

    /**
     * 设置padding
     *
     * @param left  左边距
     * @param top   上边距
     * @param right 右边距
     * @param bottom 下边距
     */
    fun paddingOnly(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        isDp: Boolean = true
    ): ShapeBuilder {
        mPadding = Rect().apply {
            if (isDp) {
                set(
                    dp2px(left).roundToInt(),
                    dp2px(top).roundToInt(),
                    dp2px(right).roundToInt(),
                    dp2px(bottom).roundToInt()
                )
            } else {
                set(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
            }
        }
        return this
    }

    /**
     * 设置size
     */
    fun size(width: Float, height: Float, isDp: Boolean = true): ShapeBuilder {
        mWidth = if (isDp) dp2px(width) else width
        mHeight = if (isDp) dp2px(height) else height
        return this
    }

    /**
     * 设置填充色
     * @param colorId 填充的颜色
     */
    fun solid(@ColorRes colorId: Int): ShapeBuilder {
        mSolidColor = ContextCompat.getColor(cxt(), colorId)
        return this
    }

    /**
     * 设置填充色
     *
     * @param colorString 颜色的string值
     */
    fun solid(colorString: String): ShapeBuilder {
        mSolidColor = string2Color(colorString)
        return this
    }

    /**
     * 设置填充色
     * @param colorString 颜色的string值
     */
    fun solid(colorString: String?, @ColorRes defaultColor: Int): ShapeBuilder {
        mSolidColor = string2Color(colorString, ContextCompat.getColor(cxt(), defaultColor))
        return this
    }

    /**
     * 设置Drawable的形状
     *
     * @param shape 形状
     */
    fun shape(shape: Shape): ShapeBuilder {
        mShape = when (shape) {
            Shape.OVAL -> GradientDrawable.OVAL
            Shape.LINE -> GradientDrawable.LINE
//            Shape.RING -> GradientDrawable.RING
            else -> GradientDrawable.RECTANGLE
        }
        return this
    }

    /**
     * 设置线条的颜色和粗细
     *
     * @param colorId 线条颜色
     * @param dpWidth 线条粗细，dp
     */
    @JvmOverloads
    fun stroke(
        @ColorRes colorId: Int, dpWidth: Float,
        dpDashGap: Float = 0f, dpDashWidth: Float = 0f, isDp: Boolean = true
    ): ShapeBuilder {
        return strokeInt(
            ContextCompat.getColor(cxt(), colorId),
            dpWidth,
            dpDashGap,
            dpDashWidth,
            isDp
        )
    }

    /**
     * 设置线条的颜色和粗细
     *
     * @param colorString 线条颜色
     * @param width     线条粗细，dp
     */
    @JvmOverloads
    fun stroke(
        colorString: String, width: Float,
        dashGap: Float = 0f,
        dashWidth: Float = 0f, isDp: Boolean = true
    ): ShapeBuilder {
        return strokeInt(string2Color(colorString), width, dashGap, dashWidth, isDp)
    }

    /**
     * 设置线条的颜色和粗细
     * @param colorInt 线条颜色
     */
    @JvmOverloads
    fun strokeInt(
        colorInt: Int,
        width: Float,
        dashGap: Float = 0f,
        dashWidth: Float = 0f,
        isDp: Boolean = true
    ): ShapeBuilder {
        mStrokeColor = colorInt
        mStrokeWidth = if (isDp) dp2px(width) else dashWidth
        mDashGap = if (isDp) dp2px(dashGap) else dashWidth
        mDashWidth = if (isDp) dp2px(dashWidth) else dashWidth
        return this
    }

    /**
     * 设置虚线
     *
     * @param dashGap   实线的距离
     * @param dashWidth 实线的宽度
     */
    fun dash(dashGap: Float, dashWidth: Float, isDp: Boolean = true): ShapeBuilder {
        mDashGap = if (isDp) dp2px(dashGap) else dashWidth
        mDashWidth = if (isDp) dp2px(dashWidth) else dashWidth
        return this
    }

    @JvmOverloads
    fun gradient(
        angle: Int, startColor: String, endColor: String,
        centerColor: String? = null
    ): ShapeBuilder {
        return gradientInt(
            angle,
            startColor = string2Color(startColor),
            endColor = string2Color(endColor),
            centerColor = if (centerColor == null) null
            else string2Color(centerColor)
        )
    }

    /**
     * 设置渐变色 资源id方式
     *
     * @param startColor 起始颜色
     * @param endColor   结束颜色
     * @param angle      渐变角度，必须为45的倍数
     */
    @JvmOverloads
    fun gradient(
        angle: Int,
        @ColorRes startColor: Int, @ColorRes endColor: Int, @ColorRes centerColor: Int? = null,
    ): ShapeBuilder {
        return gradientInt(
            angle,
            startColor = ContextCompat.getColor(cxt(), startColor),
            endColor = ContextCompat.getColor(cxt(), endColor),
            centerColor = if (centerColor == null) null
            else ContextCompat.getColor(cxt(), centerColor)
        )
    }

    /**
     * 设置渐变色
     *
     * @param startColor 起始颜色
     * @param endColor   结束颜色
     * @param angle      渐变角度，必须为45的倍数
     */
    @JvmOverloads
    fun gradientInt(
        angle: Int, startColor: Int, endColor: Int, centerColor: Int? = null,
    ): ShapeBuilder {
        require(angle % 45 == 0) { "'angle' attribute to be a multiple of 45" }
        if (centerColor != null) {
            val color = IntArray(3)
            color[0] = startColor
            color[1] = centerColor
            color[2] = endColor
            mGradientColor = color
        } else {
            val color = IntArray(2)
            color[0] = startColor
            color[1] = endColor
            mGradientColor = color
        }
        mAngle = angle
        return this
    }

    override fun build(): Drawable {
        val drawable: GradientDrawable
        if (mGradientColor != null) {
            val orientation = when (mAngle % 360) {
                45 -> GradientDrawable.Orientation.BL_TR
                90 -> GradientDrawable.Orientation.BOTTOM_TOP
                135 -> GradientDrawable.Orientation.BR_TL
                180 -> GradientDrawable.Orientation.RIGHT_LEFT
                225 -> GradientDrawable.Orientation.TR_BL
                270 -> GradientDrawable.Orientation.TOP_BOTTOM
                315 -> GradientDrawable.Orientation.TL_BR
                0 -> GradientDrawable.Orientation.LEFT_RIGHT
                else -> GradientDrawable.Orientation.LEFT_RIGHT
            }
            drawable = GradientDrawable(orientation, mGradientColor)
        } else {
            drawable = GradientDrawable()
            drawable.setColor(mSolidColor)
        }
        // 设置边框和虚线
        if (mStrokeWidth != 0f) {
            if (mDashWidth != 0f && mDashGap != 0f) {
                drawable.setStroke(
                    mStrokeWidth.roundToInt(),
                    mStrokeColor, mDashWidth, mDashGap
                )
            } else {
                drawable.setStroke(mStrokeWidth.roundToInt(), mStrokeColor)
            }
        }
        // 圆角
        if (mRadius != 0f) {
            drawable.cornerRadius = mRadius
        } else if (mRadii != null) {
            drawable.cornerRadii = mRadii
        }
        // size
        if (mWidth != 0f && mHeight != 0f) {
            drawable.setSize(mWidth.roundToInt(), mHeight.roundToInt())
        }

        // padding属性, 全版本兼容
        mPadding?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.setPadding(it.left, it.top, it.right, it.bottom)
            } else {
                fun setPadding(obj: Any?, clazz: Class<*>, rect: Rect) {
                    val mPaddingField = clazz.declaredField("mPadding")
                    mPaddingField.set(obj, rect)
                }

                try {
                    setPadding(drawable, GradientDrawable::class.java, it)
                    val gradientStateClass = Class.forName(CLASS_GRADIENT_STATE)
                    setPadding(drawable.constantState, gradientStateClass, it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        drawable.shape = mShape
        return drawable
    }

    companion object {
        private const val CLASS_GRADIENT_STATE =
            "android.graphics.drawable.GradientDrawable\$GradientState"

        private inline fun Class<*>.declaredField(fieldName: String): Field {
            val declaredField = this.getDeclaredField(fieldName)
            declaredField.isAccessible = true
            return declaredField
        }
    }

}