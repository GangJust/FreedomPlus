package io.github.fplus.drawable

import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import io.github.fplus.drawable.DrawableBuilder.Companion.dp2px
import kotlin.math.roundToInt

/**
 * 创建时间 2019/1/3
 * 描述     多重样式叠加的Drawable构建器，后添加的覆盖在新添加的上面
 */
class LayerListBuilder : DrawableBuilder {
    private val mDrawableList: MutableList<Drawable> = ArrayList()
    private val mInsetList: MutableList<LayerInset> = ArrayList()

    /**
     * 添加图层，带属性
     *
     * @param layer 新添加的图层
     * @param inset 图层属性设置
     */
    @JvmOverloads
    fun addLayer(layer: Drawable, inset: LayerInset = LayerInset()): LayerListBuilder {
        mDrawableList.add(layer)
        mInsetList.add(inset)
        return this
    }

    override fun build(): Drawable {
        return if (mDrawableList.isEmpty()) {
            ColorDrawable(Color.TRANSPARENT)
        } else {
            val layerDrawable = LayerDrawable(mDrawableList.toTypedArray())
            for (x in mDrawableList.indices) {
                val inset = mInsetList[x]
                layerDrawable.setLayerInset(x, inset.left, inset.top, inset.right, inset.bottom)
                layerDrawable.setId(x, inset.id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    layerDrawable.setLayerGravity(x, inset.gravity)
                    layerDrawable.setLayerSize(x, inset.width, inset.height)
                }
            }
            layerDrawable
        }
    }

    /**
     * layer中图层的属性类
     */
    class LayerInset {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        var width = -1
        var height = -1
        var id = View.NO_ID
        var gravity = Gravity.NO_GRAVITY

        fun id(id: Int): LayerInset {
            this.id = id
            return this
        }

        fun padding(left: Int, top: Int, right: Int, bottom: Int): LayerInset {
            this.left = dp2px(left.toFloat()).roundToInt()
            this.top = dp2px(top.toFloat()).roundToInt()
            this.right = dp2px(right.toFloat()).roundToInt()
            this.bottom = dp2px(bottom.toFloat()).roundToInt()
            return this
        }

        fun padding(padding: Int): LayerInset {
            val paddingPx = dp2px(padding.toFloat()).roundToInt()
            left = paddingPx
            top = paddingPx
            right = paddingPx
            bottom = paddingPx
            return this
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun width(width: Int): LayerInset {
            this.width = dp2px(width.toFloat()).roundToInt()
            return this
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun height(height: Int): LayerInset {
            this.height = dp2px(height.toFloat()).roundToInt()
            return this
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun gravity(gravity: Int): LayerInset {
            this.gravity = gravity
            return this
        }
    }
}