package io.github.fplus.drawable

import android.content.res.ColorStateList
import android.util.SparseIntArray
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.github.fplus.drawable.DrawableBuilder.Companion.cxt

/**
 * 创建时间 2019/1/4
 * 描述    字体颜色构建器
 */
class ColorStateListBuilder constructor(@param:ColorRes private val mNormalColorId: Int) {

    private val array = SparseIntArray()

    /**
     * 设置按下的颜色
     *
     * @param colorId 按下的颜色id
     */
    fun pressed(@ColorRes colorId: Int): ColorStateListBuilder {
        val pressed = android.R.attr.state_pressed
        array.put(pressed, colorId)
        return this
    }

    /**
     * 设置不可用时的颜色
     *
     * @param colorId 不可用的颜色id
     */
    fun unable(@ColorRes colorId: Int): ColorStateListBuilder {
        val unable = -android.R.attr.state_enabled
        array.put(unable, colorId)
        return this
    }

    /**
     * 设置选中时的颜色
     *
     * @param colorId 选中的颜色id
     */
    fun selected(@ColorRes colorId: Int): ColorStateListBuilder {
        val selected = android.R.attr.state_selected
        array.put(selected, colorId)
        return this
    }

    /**
     * 设置checkbox中被选中时的颜色
     *
     * @param colorId 选中的颜色id
     */
    fun checked(@ColorRes colorId: Int): ColorStateListBuilder {
        val checked = android.R.attr.state_checked
        array.put(checked, colorId)
        return this
    }

    fun build(): ColorStateList {
        val size = array.size() + 1
        val color = IntArray(size)
        val colorState = arrayOfNulls<IntArray>(size)
        for (x in 0 until size) {
            if (x != size - 1) {
                colorState[x] = intArrayOf(array.keyAt(x))
                color[x] = ContextCompat.getColor(cxt(), array.valueAt(x))
            } else {
                colorState[x] = intArrayOf()
                color[x] = ContextCompat.getColor(cxt(), mNormalColorId)
            }
        }
        return ColorStateList(colorState, color)
    }

    companion object {
        /**
         * 正常显示的颜色
         *
         * @param colorId 颜色id
         */
        fun normal(@ColorRes colorId: Int): ColorStateListBuilder {
            require(colorId != 0) { "Id can not be 0" }
            return ColorStateListBuilder(colorId)
        }
    }
}