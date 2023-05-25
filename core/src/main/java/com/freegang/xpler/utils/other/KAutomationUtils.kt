package com.freegang.xpler.utils.other

import android.app.Instrumentation
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.view.postDelayed

object KAutomationUtils {

    /// Instrumentation
    /** 需要注意, Instrumentation 只能在本应用范围内使用,
     * 如果出现异常: Injecting to another application requires INJECT_EVENTS permission
     * 证明你的坐标方式不对, 参考 [android.view.View.getLocationOnScreen] 获取屏幕坐标方法
     */
    private val instrumentation by lazy { Instrumentation() }

    /**
     * 模拟点击指定坐标
     *
     * 禁止在 UI线程 中调用
     *
     * @param x 相对于屏幕的 x 坐标
     * @param y 相对于屏幕的 y 坐标
     */
    @JvmStatic
    fun simulateClick(
        x: Float,
        y: Float,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        // 构造 MotionEvent 对象，模拟按下动作
        val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
        instrumentation.sendPointerSync(downEvent)
        downEvent.recycle()

        // 模拟抬起动作
        val upEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0)
        instrumentation.sendPointerSync(upEvent)
        upEvent.recycle()
    }

    /**
     * 模拟长按指定坐标
     *
     * 禁止在 UI线程 中调用
     *
     * @param x 相对于屏幕的 x 坐标
     * @param y 相对于屏幕的 y 坐标
     * @param duration 长按持续时间，默认为 500 毫秒
     */
    @JvmStatic
    @JvmOverloads
    fun simulateLongPress(
        x: Float,
        y: Float,
        duration: Long = 500L,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        // 构造 MotionEvent 对象，模拟按下动作
        val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
        instrumentation.sendPointerSync(downEvent)
        downEvent.recycle()

        // 模拟长按动作
        val longPressEventTime = eventTime + duration
        val longPressEvent = MotionEvent.obtain(downTime, longPressEventTime, MotionEvent.ACTION_MOVE, x, y, 0)
        instrumentation.sendPointerSync(longPressEvent)
        longPressEvent.recycle()

        // 模拟抬起动作
        val upEvent = MotionEvent.obtain(downTime, longPressEventTime, MotionEvent.ACTION_UP, x, y, 0)
        instrumentation.sendPointerSync(upEvent)
        upEvent.recycle()
    }

    /**
     * 模拟滑动操作
     *
     * 禁止在 UI线程 中调用
     *
     * @param startX 相对于屏幕的 起点x 坐标,
     * @param startY 相对于屏幕的 起点y 坐标
     * @param endX 相对于屏幕的 止点x 坐标
     * @param endY 相对于屏幕的 止点y 坐标
     * @param duration 滑动持续时间，默认为 200 毫秒
     */
    @JvmStatic
    @JvmOverloads
    fun simulateSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 200L,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        // 构造起始点的 MotionEvent 对象，模拟按下动作
        val downEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            startX,
            startY,
            0
        )
        instrumentation.sendPointerSync(downEvent)
        downEvent.recycle()

        // 计算每个时间步长应该移动的距离
        val totalSteps = duration / 10 // 假设每10毫秒进行一次移动
        val stepX = (endX - startX) / totalSteps
        val stepY = (endY - startY) / totalSteps

        // 模拟滑动动作
        for (step in 0 until totalSteps) {
            val moveEventTime = eventTime + (step * 10) // 每10毫秒进行一次移动
            val moveEvent = MotionEvent.obtain(
                downTime,
                moveEventTime,
                MotionEvent.ACTION_MOVE,
                startX + stepX * step,
                startY + stepY * step,
                0
            )
            instrumentation.sendPointerSync(moveEvent)
            moveEvent.recycle()
        }

        // 模拟抬起动作
        val upEventTime = eventTime + duration
        val upEvent = MotionEvent.obtain(
            downTime,
            upEventTime,
            MotionEvent.ACTION_UP,
            endX,
            endY,
            0
        )
        instrumentation.sendPointerSync(upEvent)
        upEvent.recycle()
    }

    /**
     * 模拟输入文本, 需要某个文本框处于待输入状态
     *
     * 禁止在 UI线程 中调用
     *
     * @param text 要输入的文本
     */
    @JvmStatic
    fun simulateText(text: String) {
        instrumentation.sendStringSync(text)
    }

    /**
     * 模拟按下指定的按键
     *
     * 禁止在 UI线程 中调用
     *
     * @param keyCode 按键的代码，参考 KeyEvent 类的常量
     */
    @JvmStatic
    fun simulateKeyPress(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()

        // 模拟按下动作
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        instrumentation.sendKeySync(downEvent)

        // 模拟抬起动作
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        instrumentation.sendKeySync(upEvent)
    }

    /// View
    /**
     * 模拟点击手势操作，点击指定坐标的视图
     *
     * 需要在 UI线程 中调用
     *
     * 如果你需要直接触发某个视图的点击事件，请考虑使用 [View.performClick] 方法。
     *
     * @param view 被点击的目标视图
     * @param x 相对于目标视图的 x 坐标
     * @param y 相对于目标视图的 y 坐标
     */
    @JvmStatic
    fun simulateClickByView(
        view: View,
        x: Float,
        y: Float,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 10

        //模拟按下
        val downEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            x,
            y,
            0,
        )
        view.dispatchTouchEvent(downEvent)
        downEvent.recycle()

        //模拟抬起
        val upEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            0,
        )
        view.dispatchTouchEvent(upEvent)
        upEvent.recycle()
    }

    /**
     * 模拟长按手势操作，长按指定坐标的视图
     *
     * 需要在 UI线程 中调用
     *
     * @param view 被长按的目标视图
     * @param x 相对于目标视图的 x 坐标
     * @param y 相对于目标视图的 y 坐标
     * @param duration 长按的持续时间，默认为 500 毫秒
     */
    @JvmStatic
    fun simulateLongPressByView(
        view: View,
        x: Float,
        y: Float,
        duration: Long = 500L,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        // 模拟按下事件
        val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
        view.dispatchTouchEvent(downEvent)
        downEvent.recycle()

        // 延迟一段时间后模拟移动事件（可选）
        // 例如：长按时可能需要模拟拖动手势，你可以在此处添加相应的事件来实现
        // val moveEvent = MotionEvent.obtain(downTime, eventTime + delay, MotionEvent.ACTION_MOVE, x, y, 0)
        // view.dispatchTouchEvent(moveEvent)
        // moveEvent.recycle()

        // 延迟指定的持续时间后模拟抬起事件
        view.postDelayed(duration) {
            val upEvent = MotionEvent.obtain(downTime, eventTime + duration, MotionEvent.ACTION_UP, x, y, 0)
            view.dispatchTouchEvent(upEvent)
            upEvent.recycle()
        }
    }

    /**
     * 模拟滑动手势操作，长按指定坐标的视图
     *
     * 需要在 UI线程 中调用
     *
     * @param startX 相对于目标视图的 起点x 坐标,
     * @param startY 相对于目标视图的 起点y 坐标
     * @param endX 相对于目标视图的 止点x 坐标
     * @param endY 相对于目标视图的 止点y 坐标
     * @param duration 滑动持续时间，默认为 200 毫秒
     */
    @JvmStatic
    @JvmOverloads
    fun simulateSwipeByView(
        view: View,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 200L,
    ) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()

        // 构造起始点的 MotionEvent 对象，模拟按下动作
        val downEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            startX,
            startY,
            0
        )
        view.dispatchTouchEvent(downEvent)

        // 计算每个时间步长应该移动的距离
        val totalSteps = duration / 10 // 假设每10毫秒进行一次移动
        val stepX = (endX - startX) / totalSteps
        val stepY = (endY - startY) / totalSteps

        // 模拟滑动动作
        for (step in 0 until totalSteps) {
            val moveEventTime = eventTime + (step * 10) // 每10毫秒进行一次移动
            val moveEvent = MotionEvent.obtain(
                downTime,
                moveEventTime,
                MotionEvent.ACTION_MOVE,
                startX + stepX * step,
                startY + stepY * step,
                0
            )
            view.dispatchTouchEvent(moveEvent)
        }

        // 模拟抬起动作
        val upEventTime = eventTime + duration
        val upEvent = MotionEvent.obtain(
            downTime,
            upEventTime,
            MotionEvent.ACTION_UP,
            endX,
            endY,
            0
        )
        view.dispatchTouchEvent(upEvent)
    }
}