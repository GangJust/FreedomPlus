package io.github.fplus.drawable

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log

// see: https://github.com/forJrking/DrawableDsl
interface DrawableBuilder {
    /**
     * 此方法构建出新的Drawable给控件设置背景
     *
     * @return 构造好的Drawable
     */
    fun build(): Drawable?


    /** DES: 高版本废弃反射后建议自己赋值 */
    companion object {

        /** DES: 高版本废弃反射后建议自己赋值 */
        lateinit var app: Application

        // 获取context方法
        fun cxt(): Context = if (this::app.isInitialized) app else {
            app = reflectContext();app
        }

        /**
         * Value of dp to value of px.
         * @param dpValue The value of dp.
         * @return value of px
         */
        internal fun dp2px(dpValue: Float): Float {
            val scale = cxt().resources.displayMetrics.density
            return dpValue * scale
        }

        /**字符串转换为颜色*/
        fun string2Color(colorString: String?, defaultColor: Int = Color.TRANSPARENT): Int =
            if (!colorString.isNullOrEmpty()) {
                try {
                    Color.parseColor(colorString.trim())
                } catch (e: Exception) {
                    e.printStackTrace()
                    defaultColor
                }
            } else {
                defaultColor
            }

        /**
         * DES: 反射获取全局Context  后期可能被google废弃这里会报错
         */
        private fun reflectContext(): Application {
            Log.w("DrawableBuilder", "reflectContext called")
            try {
                return Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null) as Application
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                return Class.forName("android.app.AppGlobals")
                    .getMethod("getInitialApplication")
                    .invoke(null) as Application
            } catch (e: Exception) {
                e.printStackTrace()
            }
            throw IllegalStateException("reflect Context error,高版本废弃反射后建议自己赋值")
        }
    }
}