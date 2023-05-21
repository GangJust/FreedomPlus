package com.freegang.xpler.utils.view

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.annotation.IdRes
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayDeque

object KViewUtils {

    /**
     * 设置 ViewGroup 及其所有子视图的可见性为 [View.VISIBLE]，即全部显示。
     *
     * @param viewGroup 要设置可见性的 ViewGroup。
     */
    @JvmStatic
    fun showAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.VISIBLE)
    }

    /**
     * 设置 ViewGroup 及其所有子视图的可见性为 [View.GONE]，即全部隐藏。
     *
     * @param viewGroup 要设置可见性的 ViewGroup。
     */
    @JvmStatic
    fun hideAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.GONE)
    }

    /**
     * 设置 ViewGroup 及其所有子视图的可见性为 [View.INVISIBLE]，即全部不可见。
     *
     * @param viewGroup 要设置可见性的 ViewGroup。
     */
    @JvmStatic
    fun invisibleAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.INVISIBLE)
    }

    /**
     * 递归设置 ViewGroup 及其所有子视图的可见性。
     *
     * @param viewGroup   要设置可见性的 ViewGroup。
     * @param visibility  要设置的可见性，可选值为 [View.VISIBLE]、[View.GONE] 或 [View.INVISIBLE]。
     */
    private fun setVisibilityAll(viewGroup: ViewGroup, visibility: Int) {
        if (viewGroup.visibility == visibility) return // 如果已经设置过了，就直接返回
        val childCount = viewGroup.childCount
        if (childCount == 0) return

        // 先递归遍历设置所有子视图的 Visibility
        for (i in 0 until childCount) {
            val childAt = viewGroup.getChildAt(i)
            if (childAt.visibility != visibility) { // 仅当可见性状态不同时才进行递归调用
                if (childAt is ViewGroup) {
                    setVisibilityAll(childAt, visibility)
                } else {
                    childAt.visibility = visibility // 仅限于子视图范围内设置可见性
                }
            }
        }
        viewGroup.visibility = visibility // 再设置当前视图的 Visibility
    }

    //
    /**
     * 判断视图是否可见。
     *
     * @param view 要判断可见性的视图。
     * @return 如果视图可见，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }

    /**
     * 判断视图是否隐藏。
     *
     * @param view 要判断可见性的视图。
     * @return 如果视图隐藏，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isGone(view: View): Boolean {
        return view.visibility == View.GONE
    }

    /**
     * 判断视图是否不可见。
     *
     * @param view 要判断可见性的视图。
     * @return 如果视图不可见，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isInvisible(view: View): Boolean {
        return view.visibility == View.INVISIBLE
    }

    /**
     * 判断 ViewGroup 及其所有子视图是否全部可见。
     *
     * @param viewGroup 要判断可见性的 ViewGroup。
     * @return 如果 ViewGroup 及其所有子视图全部可见，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isVisibleAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return !resultList.contains(View.GONE) && !resultList.contains(View.INVISIBLE)
    }

    /**
     * 判断 ViewGroup 及其所有子视图是否全部隐藏。
     *
     * @param viewGroup 要判断可见性的 ViewGroup。
     * @return 如果 ViewGroup 及其所有子视图全部隐藏，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isGoneAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return resultList.contains(View.GONE) && !resultList.contains(View.VISIBLE)
    }

    /**
     * 判断 ViewGroup 及其所有子视图是否全部不可见。
     *
     * @param viewGroup 要判断可见性的 ViewGroup。
     * @return 如果 ViewGroup 及其所有子视图全部不可见，则返回 `true`；否则返回 `false`。
     */
    @JvmStatic
    fun isInvisibleAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return !resultList.contains(View.GONE) && resultList.contains(View.INVISIBLE)
    }

    /**
     * 递归遍历 ViewGroup 及其所有子视图，返回可见性状态列表。
     *
     * @param viewGroup 要遍历的 ViewGroup。
     * @return 可见性状态列表，包含 ViewGroup 及其所有子视图的可见性状态。
     */
    private fun traverseVisibilityAll(viewGroup: ViewGroup): List<Int> {
        val resultList = mutableListOf<Int>()
        if (viewGroup.visibility == View.GONE) {
            resultList.add(View.GONE)
            return resultList
        }

        val stack = ArrayDeque<ViewGroup>()
        stack.addFirst(viewGroup)

        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            for (i in 0 until current.childCount) {
                val child = current.getChildAt(i)
                if (child is ViewGroup) {
                    if (child.visibility == View.GONE) {
                        resultList.add(View.GONE)
                    } else {
                        stack.addFirst(child)
                    }
                }
                resultList.add(child.visibility)
            }
        }
        return resultList
    }

    //
    /**
     * 判断给定的 ViewGroup 及其所有子视图是否全部可用。
     *
     * @param viewGroup 要判断的 ViewGroup。
     * @return 是否全部可用，如果全部可用返回 true，否则返回 false。
     */
    @JvmStatic
    fun isEnabledAll(viewGroup: ViewGroup): Boolean {
        val stack = ArrayDeque<ViewGroup>()
        stack.addFirst(viewGroup)

        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()

            if (!current.isEnabled) {
                return false
            }

            for (i in 0 until current.childCount) {
                val child = current.getChildAt(i)
                if (child is ViewGroup) {
                    stack.addFirst(child)
                } else if (!child.isEnabled) {
                    return false
                }
            }
        }

        return true
    }

    //
    /**
     * 获取视图的 ID 的十六进制表示形式
     *
     * @param view 目标视图
     * @return 视图 ID 的十六进制字符串表示形式，如果视图的 ID 为 View.NO_ID，则返回字符串 "${View.NO_ID}"
     */
    @JvmStatic
    fun getIdHex(view: View): String {
        return try {
            if (view.id == View.NO_ID) {
                "${View.NO_ID}"
            } else {
                "0x${Integer.toHexString(view.id)}"
            }
        } catch (e: Exception) {
            "${View.NO_ID}"
        }
    }

    /**
     * 获取视图的 ID 的资源名称
     *
     * @param view 目标视图
     * @return 视图 ID 的资源名称，如果视图的 ID 为 View.NO_ID，则返回字符串 "${View.NO_ID}"
     */
    @JvmStatic
    fun getIdName(view: View): String {
        return try {
            if (view.id == View.NO_ID) {
                "${View.NO_ID}"
            } else {
                "@id/${view.context.resources.getResourceEntryName(view.id)}"
            }
        } catch (e: Exception) {
            "${View.NO_ID}"
        }
    }

    /**
     * 获取指定资源 ID 的资源名称
     *
     * @param context 上下文对象
     * @param resId 目标资源 ID
     * @return 资源 ID 的资源名称，如果资源 ID 为 View.NO_ID，则返回字符串 "${View.NO_ID}"
     */
    @JvmStatic
    fun getIdName(context: Context, @IdRes resId: Int): String {
        return try {
            if (resId == View.NO_ID) {
                "${View.NO_ID}"
            } else {
                "@id/${context.resources.getResourceEntryName(resId)}"
            }
        } catch (e: Exception) {
            "${View.NO_ID}"
        }
    }

    //
    /**
     * 获取 View 的点击事件监听器。
     *
     * @param view 要获取点击事件监听器的 View
     * @return View.OnClickListener 对象，如果不存在则返回 null
     */
    @JvmStatic
    fun <T : View> getOnClickListener(view: T): View.OnClickListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnClickListenerField = listenerInfo.javaClass.getDeclaredField("mOnClickListener")
            mOnClickListenerField.isAccessible = true
            val onClickListener = mOnClickListenerField.get(listenerInfo)
            return if (onClickListener is View.OnClickListener) onClickListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace() // 捕获 NoSuchFieldException 异常并打印异常信息
        } catch (e: IllegalAccessException) {
            e.printStackTrace() // 捕获 IllegalAccessException 异常并打印异常信息
        } catch (e: Exception) {
            e.printStackTrace() // 捕获其他异常并打印异常信息
        }
        return null
    }

    /**
     * 获取 View 的长按事件监听器。
     *
     * @param view 要获取长按事件监听器的 View
     * @return View.OnLongClickListener 对象，如果不存在则返回 null
     */
    @JvmStatic
    fun <T : View> getOnLongClickListener(view: T): View.OnLongClickListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnLongClickListenerField =
                listenerInfo.javaClass.getDeclaredField("mOnLongClickListener")
            mOnLongClickListenerField.isAccessible = true
            val onLongClickListener = mOnLongClickListenerField.get(listenerInfo)
            return if (onLongClickListener is View.OnLongClickListener) onLongClickListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace() // 捕获 NoSuchFieldException 异常并打印异常信息
        } catch (e: IllegalAccessException) {
            e.printStackTrace() // 捕获 IllegalAccessException 异常并打印异常信息
        } catch (e: Exception) {
            e.printStackTrace() // 捕获其他异常并打印异常信息
        }
        return null
    }

    /**
     * 获取 View 的触摸事件监听器。
     *
     * @param view 要获取触摸事件监听器的 View
     * @return View.OnTouchListener 对象，如果不存在则返回 null
     */
    @JvmStatic
    fun <T : View> getOnTouchListener(view: T): View.OnTouchListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnTouchListenerField = listenerInfo.javaClass.getDeclaredField("mOnTouchListener")
            mOnTouchListenerField.isAccessible = true
            val onTouchListener = mOnTouchListenerField.get(listenerInfo)
            return if (onTouchListener is View.OnTouchListener) onTouchListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace() // 捕获 NoSuchFieldException 异常并打印异常信息
        } catch (e: IllegalAccessException) {
            e.printStackTrace() // 捕获 IllegalAccessException 异常并打印异常信息
        } catch (e: Exception) {
            e.printStackTrace() // 捕获其他异常并打印异常信息
        }
        return null
    }

    /**
     * 获取 View 的 ListenerInfo 对象，用于访问 View 的点击、长按和触摸事件监听器。
     *
     * @param view 要获取 ListenerInfo 对象的 View
     * @return ListenerInfo 对象，如果不存在则返回 null
     */
    private fun <T : View> getListenerInfo(view: T): Any? {
        try {
            val mListenerInfoField = findFieldRecursiveImpl(view::class.java, "mListenerInfo")
            mListenerInfoField?.isAccessible = true
            return mListenerInfoField?.get(view)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace() // 捕获 NoSuchFieldException 异常并打印异常信息
        } catch (e: IllegalAccessException) {
            e.printStackTrace() // 捕获 IllegalAccessException 异常并打印异常信息
        }
        return null
    }

    /**
     * 递归查找指定类及其父类中的字段。
     *
     * @param clazz 要查找字段的类
     * @param fieldName 字段名
     * @return 找到的字段对象，如果未找到则抛出 NoSuchFieldException 异常
     * @throws NoSuchFieldException 如果未找到字段则抛出 NoSuchFieldException 异常
     */
    @Throws(NoSuchFieldException::class)
    private fun findFieldRecursiveImpl(clazz: Class<*>, fieldName: String): Field? {
        var currentClass: Class<*>? = clazz
        return try {
            currentClass?.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            while (true) {
                currentClass = currentClass?.superclass
                if (currentClass == null || currentClass == Any::class.java) break
                try {
                    return currentClass.getDeclaredField(fieldName)
                } catch (ignored: NoSuchFieldException) {
                }
            }
            throw e
        }
    }


    //
    /**
     * 通过事件分发模拟点击某个指定坐标
     * 如果你需要触发某个view的点击事件，请参考: [View.performClick] 方法
     *
     * @param view 目标view, 或者是它的父view
     * @param x x坐标轴
     * @param y y坐标轴
     */
    fun motionClickView(view: View, x: Float, y: Float) {
        var downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        downTime += 10
        val upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, x, y, 0)
        view.dispatchTouchEvent(downEvent)
        view.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
    }


    //
    /**
     * 遍历 ViewGroup 及其子 View
     *
     * @param viewGroup 需要遍历的 ViewGroup
     * @param call 遍历回调函数，接收一个 View 参数
     */
    @JvmStatic
    fun traverseViewGroup(viewGroup: ViewGroup, call: (it: View) -> Unit) {
        val stack = Stack<View>()
        stack.push(viewGroup)
        while (!stack.isEmpty()) {
            val current = stack.pop()
            call.invoke(current)
            if (current is ViewGroup) {
                for (i in current.childCount - 1 downTo 0) {
                    stack.push(current.getChildAt(i))
                }
            }
        }
    }

    /**
     * 深度遍历 ViewGroup，获取所有的 View（包括 ViewGroup 本身和其所有子 View）
     *
     * @param viewGroup 需要遍历的 ViewGroup
     * @return 所有的 View 列表
     */
    @JvmStatic
    fun deepViewGroup(viewGroup: ViewGroup): List<View> {
        val views = mutableListOf<View>()
        val stack = Stack<View>()
        stack.push(viewGroup)

        while (!stack.isEmpty()) {
            val current = stack.pop()
            views.add(current)
            if (current is ViewGroup) {
                // 遍历当前 ViewGroup 的所有子 View
                for (i in current.childCount - 1 downTo 0) {
                    stack.push(current.getChildAt(i))
                }
            }
        }

        return views
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @return List<T> T extends View
     */
    @JvmStatic
    fun <T : View> findViews(
        viewGroup: ViewGroup,
        targetType: Class<T>,
    ): List<T> {
        return findViewsExact(viewGroup, targetType) { true }
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * 并且该ChildView的[contentDescription]值包含了指定正则表达式[containsDesc]中的内容
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param containsDesc 指定正则表达式
     * @return List<T> T extends View
     */
    @JvmStatic
    fun <T : View> findViewsByDesc(
        viewGroup: ViewGroup,
        targetType: Class<T>,
        containsDesc: Regex,
    ): List<T> {
        return findViewsExact(viewGroup, targetType) {
            val desc = it.contentDescription ?: ""
            desc.contains(containsDesc)
        }
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * 并且该ChildView的[contentDescription]值包含了指定文本[containsDesc]中的内容
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param containsDesc 指定文本
     * @return List<T> T extends View
     */
    @JvmStatic
    fun <T : View> findViewsByDesc(
        viewGroup: ViewGroup,
        targetType: Class<T>,
        containsDesc: String,
        ignoreCase: Boolean = false,
    ): List<T> {
        return findViewsExact(viewGroup, targetType) {
            val desc = it.contentDescription ?: ""
            desc.contains(containsDesc, ignoreCase)
        }
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * 并且该ChildView的[idName]等于指定的[idName]
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param idName idName 举例: @id/textView
     * @return List<T> T extends View
     */
    @JvmStatic
    fun <T : View> findViewsByIdName(
        viewGroup: ViewGroup,
        targetType: Class<T>,
        idName: String
    ): List<T> {
        return findViewsExact(viewGroup, targetType) { getIdName(it) == idName }
    }

    /**
     * 获取某个ViewGroup视图树中所有满足指定逻辑的ChildView
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param logic 回调方法, 该方法参数会传入所有被遍历的view, 返回一个[Boolean]
     * @return List<T> T extends View
     */
    @JvmStatic
    fun <T : View> findViewsExact(
        viewGroup: ViewGroup,
        targetType: Class<T>,
        logic: (it: T) -> Boolean
    ): List<T> {
        val views = mutableListOf<T>()
        val stack = Stack<View>()
        stack.push(viewGroup)
        while (!stack.isEmpty()) {
            val current = stack.pop()
            if (targetType.isInstance(current) && logic.invoke(targetType.cast(current) as T)) {
                views.add(targetType.cast(current) as T)
            }
            if (current is ViewGroup) {
                for (i in current.childCount - 1 downTo 0) {
                    stack.push(current.getChildAt(i))
                }
            }
        }
        return views
    }

    /**
     * 获取某个View指定类型的 父View
     * @param view 被获取父类的 View
     * @param targetType 被指定的父类
     * @param deep 回退深度, 默认找到第1个
     * @param T 父View实例
     */
    fun <T : View> findParentExact(
        view: View,
        targetType: Class<T>,
        deep: Int = 1,
    ): T? {
        var deepCount = deep
        var parent: ViewParent? = view.parent
        while (parent != null) {
            if (targetType.isInstance(parent)) deepCount -= 1
            if (deepCount == 0) return targetType.cast(parent)
            parent = parent.parent
        }

        return null
    }

    //
    /**
     * 构建View节点树
     * @param viewGroup ViewGroup
     * @return ViewNode viewGroup作为根节点
     */
    @JvmStatic
    fun buildViewTree(viewGroup: ViewGroup): ViewNode {
        val root = ViewNode(viewGroup, viewGroup, 0, mutableListOf())
        val stack = Stack<ViewNode>()
        stack.push(root)
        while (!stack.isEmpty()) {
            val current = stack.pop()
            val view = current.view
            if (view is ViewGroup) {
                for (i in view.childCount - 1 downTo 0) {
                    val child = view.getChildAt(i)
                    val childNode = ViewNode(view, child, current.depth + 1, mutableListOf())
                    current.children.add(0, childNode) // 添加到当前节点的子节点列表头部
                    stack.push(childNode)
                }
            }
        }
        return root
    }

    data class ViewNode(
        var parent: ViewGroup? = null,  //父视图
        var view: View? = null,  //当前视图
        var depth: Int = 0,  //当前树的深度
        var children: MutableList<ViewNode> = mutableListOf(), //子节点
    ) {

        //销毁当前节点下的所有视图树
        fun destroy() {
            destroy(this)
        }

        private fun destroy(node: ViewNode) {
            val stack = Stack<ViewNode>()
            stack.push(node)
            while (!stack.isEmpty()) {
                val current = stack.pop()
                for (childNode in current.children) {
                    stack.push(childNode)
                }
                current.parent = null
                current.children.clear()
                current.view = null
            }
        }

        //当前节点下的所有视图树深度遍历到字符串
        fun deepToString(indent: Int = 4): String {
            val buffer = StringBuffer()
            deepChildren("|-", indent, this) { trunk, node ->
                buffer.append("$trunk${node}\n")
            }
            return buffer.toString()
        }

        private fun deepChildren(
            trunk: String = "|-",
            indent: Int = 4,
            node: ViewNode,
            block: (trunk: String, node: ViewNode) -> Unit
        ) {
            var indentTrunk = ""
            for (i in 0 until indent) indentTrunk += "-"

            val stack = Stack<Pair<ViewNode, Int>>()
            stack.push(Pair(node, 0))
            while (!stack.isEmpty()) {
                val (current, level) = stack.pop()
                val currentTrunk = if (level == 0) trunk else trunk + indentTrunk.repeat(level)
                block.invoke(currentTrunk, current)
                for (childNode in current.children) {
                    stack.push(Pair(childNode, level + 1))
                }
            }
        }

        override fun toString(): String {
            val view = view
                ?: return "ViewNode{parent=${parent}, view=null, depth=${depth}, hashCode=${this.hashCode()}}"

            return "ViewNode{view=${view.javaClass.name}" +
                    ", idHex=${getIdHex(view)}" +
                    ", idName=${getIdName(view)}" +
                    ", depth=${depth}" +
                    ", descr=${view.contentDescription}" +
                    ", childrenSize=${children.size}" +
                    "}"
        }
    }
}

///
fun ViewGroup.getViewTree(): KViewUtils.ViewNode {
    return KViewUtils.buildViewTree(this)
}

fun ViewGroup.traverse(call: (it: View) -> Unit) {
    KViewUtils.traverseViewGroup(this, call)
}

fun <T : View> ViewGroup.findViewsByIdName(targetType: Class<T>, idName: String): List<T> {
    return KViewUtils.findViewsByIdName(this, targetType, idName)
}

fun <T : View> ViewGroup.findViewsByType(targetType: Class<T>): List<T> {
    return KViewUtils.findViews(this, targetType)
}

fun <T : View> ViewGroup.findViewsByDesc(targetType: Class<T>, containsDesc: Regex): List<T> {
    return KViewUtils.findViewsByDesc(this, targetType, containsDesc)
}

fun <T : View> ViewGroup.findViewsByDesc(targetType: Class<T>, containsDesc: String, ignoreCase: Boolean = false): List<T> {
    return KViewUtils.findViewsByDesc(this, targetType, containsDesc, ignoreCase)
}

fun <T : View> ViewGroup.findViewsByExact(targetType: Class<T>, logic: (it: T) -> Boolean): List<T> {
    return KViewUtils.findViewsExact(this, targetType, logic)
}

fun <T : View> View.findParentExact(targetType: Class<T>, deep: Int = 1): T? {
    return KViewUtils.findParentExact(this, targetType, deep)
}


val View.idName get() = KViewUtils.getIdName(this)