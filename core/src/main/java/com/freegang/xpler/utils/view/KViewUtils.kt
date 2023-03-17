package com.freegang.xpler.utils.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayDeque

object KViewUtils {

    fun showAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.VISIBLE)
    }

    fun hideAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.GONE)
    }

    fun invisibleAll(viewGroup: ViewGroup) {
        setVisibilityAll(viewGroup, View.INVISIBLE)
    }

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
    fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }

    fun isGon(view: View): Boolean {
        return view.visibility == View.GONE
    }

    fun isInvisible(view: View): Boolean {
        return view.visibility == View.INVISIBLE
    }

    fun isVisibleAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return !resultList.contains(View.GONE) && !resultList.contains(View.INVISIBLE)
    }

    fun isGoneAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return resultList.contains(View.GONE) && !resultList.contains(View.VISIBLE)
    }

    fun isInvisibleAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseVisibilityAll(viewGroup)
        return !resultList.contains(View.GONE) && resultList.contains(View.INVISIBLE)
    }

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
    fun isEnabledAll(viewGroup: ViewGroup): Boolean {
        val resultList = traverseEnabledAll(viewGroup)
        return resultList.all { it }
    }

    private fun traverseEnabledAll(viewGroup: ViewGroup): MutableList<Boolean> {
        val resultList = mutableListOf<Boolean>()
        if (!viewGroup.isEnabled) {
            resultList.add(false)
            return resultList
        }
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                resultList.addAll(traverseEnabledAll(child))
            } else {
                resultList.add(child.isEnabled)
            }
        }
        return resultList
    }


    //
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
    @Throws(NoSuchFieldException::class)
    private fun findFieldRecursiveImpl(clazz: Class<*>, fieldName: String): Field? {
        var cla: Class<*>? = clazz
        return try {
            cla?.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            while (true) {
                cla = cla?.superclass
                if (cla == null || cla == Any::class.java) break
                try {
                    return cla.getDeclaredField(fieldName)
                } catch (ignored: NoSuchFieldException) {
                }
            }
            throw e
        }
    }

    private fun <T : View> getListenerInfo(view: T): Any? {
        try {
            val mListenerInfoField = findFieldRecursiveImpl(view::class.java, "mListenerInfo")
            mListenerInfoField?.isAccessible = true
            return mListenerInfoField?.get(view)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取点击事件, 如果有则返回, 否则返回null
     * @param view View
     * @return View.OnClickListener
     */
    fun <T : View> getOnClickListener(view: T): View.OnClickListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnClickListenerField = listenerInfo.javaClass.getDeclaredField("mOnClickListener")
            mOnClickListenerField.isAccessible = true
            val onClickListener = mOnClickListenerField.get(view)
            return if (onClickListener is View.OnClickListener) onClickListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取长按事件, 如果有则返回, 否则返回null
     * @param view View
     * @return View.OnLongClickListener
     */
    fun <T : View> getOnLongClickListener(view: T): View.OnLongClickListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnLongClickListenerField =
                listenerInfo.javaClass.getDeclaredField("mOnLongClickListener")
            mOnLongClickListenerField.isAccessible = true
            val onLongClickListener = mOnLongClickListenerField.get(view)
            return if (onLongClickListener is View.OnLongClickListener) onLongClickListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取触摸事件, 如果有则返回, 否则返回null
     * @param view View
     * @return View.OnTouchListener
     */
    fun <T : View> getOnTouchListener(view: T): View.OnTouchListener? {
        try {
            val listenerInfo = getListenerInfo(view) ?: return null
            val mOnTouchListenerField = listenerInfo.javaClass.getDeclaredField("mOnTouchListener")
            mOnTouchListenerField.isAccessible = true
            val onTouchListener = mOnTouchListenerField.get(view)
            return if (onTouchListener is View.OnTouchListener) onTouchListener else null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    //
    /**
     * 解构ViewGroup视图树, 返回一个[View]线性列表
     * @param viewGroup ViewGroup
     * @return List<View>
     */
    fun deepViewGroup(viewGroup: ViewGroup): List<View> {
        val views = mutableListOf<View>()
        val stack = Stack<View>()
        stack.push(viewGroup)

        while (!stack.isEmpty()) {
            val current = stack.pop()
            views.add(current)
            if (current is ViewGroup) {
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
    fun <T : View> findViewsByDesc(
        viewGroup: ViewGroup,
        targetType: Class<T>,
        containsDesc: Regex
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


    //
    /**
     * 构建View节点树
     * @param viewGroup ViewGroup
     * @return ViewNode viewGroup作为根节点
     */
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


    ///
    fun ViewGroup.getViewTree(): ViewNode {
        return buildViewTree(this)
    }

    fun <T : View> ViewGroup.findViewsByType(targetType: Class<T>): List<T> {
        return findViews(this, targetType)
    }

    fun <T : View> ViewGroup.findViewsByExact(
        targetType: Class<T>,
        logic: (it: T) -> Boolean
    ): List<T> {
        return findViewsExact(this, targetType, logic)
    }

}