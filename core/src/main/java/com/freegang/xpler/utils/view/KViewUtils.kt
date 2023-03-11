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
            val mOnLongClickListenerField = listenerInfo.javaClass.getDeclaredField("mOnLongClickListener")
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
     * 解构ViewGroup视图树, 返回一个[View]列表
     * @param viewGroup ViewGroup
     * @return List<View>
     */
    fun deepViewGroup(viewGroup: ViewGroup): List<View> {
        val list = mutableListOf<View>()
        list.add(viewGroup)

        for (i in 0 until viewGroup.childCount) {
            val childAt = viewGroup.getChildAt(i)
            if (childAt is ViewGroup) {
                list.addAll(deepViewGroup(childAt))
            } else {
                // 如果符合目标，添加
                list.add(childAt)
            }
        }
        return list
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @return List<T> T extends View
     */
    fun <T : View> findViews(viewGroup: ViewGroup, targetType: Class<T>): List<T> {
        val views = mutableListOf<T>()
        //如果符合目标, 添加
        if (targetType.isInstance(viewGroup)) {
            targetType.cast(viewGroup)?.let { views.add(it) }
        }
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (targetType.isInstance(child)) {
                targetType.cast(child)?.let { views.add(it) }
            } else if (child is ViewGroup) {
                views.addAll(findViews(child, targetType))
            }
        }
        return views
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * 并且该ChildView的[contentDescription]值包含了指定正则表达式[containsDesc]中的内容
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param containsDesc 指定正则表达式
     * @return List<T> T extends View
     */
    fun <T : View> findViewsByDesc(viewGroup: ViewGroup, targetType: Class<T>, containsDesc: Regex): List<T> {
        val listViews = mutableListOf<T>()

        val stack = Stack<ViewGroup>()
        stack.push(viewGroup)

        while (!stack.empty()) {
            val currentView = stack.pop()
            val childCount = currentView.childCount
            for (i in 0 until childCount) {
                val childView = currentView.getChildAt(i)
                if (childView is ViewGroup) {
                    stack.push(childView)
                } else if (targetType.isInstance(childView)) {
                    val childDesc = childView.contentDescription ?: ""
                    if (childDesc.contains(containsDesc)) {
                        listViews.add(targetType.cast(childView) as T)
                    }
                }
            }
            if (targetType.isInstance(currentView)) {
                val parentDesc = currentView.contentDescription ?: ""
                if (parentDesc.contains(containsDesc)) {
                    listViews.add(targetType.cast(currentView) as T)
                }
            }
        }
        return listViews
    }

    /**
     * 获取某个ViewGroup视图树中所有指定类型的 ChildView
     * 并且该ChildView的[idName]等于指定的[idName]
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param idName idName
     * @return List<T> T extends View
     */
    fun <T : View> findViewsByIdName(viewGroup: ViewGroup, targetType: Class<T>, idName: String): List<T> {
        val listViews = mutableListOf<T>()

        val stack = Stack<ViewGroup>()
        stack.push(viewGroup)

        while (!stack.empty()) {
            val currentView = stack.pop()
            val childCount = currentView.childCount
            for (i in 0 until childCount) {
                val childView = currentView.getChildAt(i)
                if (childView is ViewGroup) {
                    stack.push(childView)
                } else if (targetType.isInstance(childView) && getIdName(currentView) == idName) {
                    listViews.add(targetType.cast(childView) as T)
                }
            }

            if (targetType.isInstance(currentView) && getIdName(currentView) == idName) {
                listViews.add(targetType.cast(currentView) as T)
            }
        }
        return listViews
    }

    /**
     * 获取某个ViewGroup视图树中所有满足指定逻辑的ChildView
     * @param viewGroup ViewGroup
     * @param targetType 某个View类型, 可以是 View.class
     * @param logic 回调方法, 该方法参数会传入所有被遍历的view, 返回一个[Boolean]
     * @return List<T> T extends View
     */
    fun <T : View> findViewsExact(viewGroup: ViewGroup, targetType: Class<T>, logic: (it: T) -> Boolean): List<T> {
        val listViews = mutableListOf<T>()

        val stack = Stack<ViewGroup>()
        stack.push(viewGroup)

        while (!stack.empty()) {
            val currentView = stack.pop()
            val childCount = currentView.childCount
            for (i in 0 until childCount) {
                val childView = currentView.getChildAt(i)
                if (childView is ViewGroup) {
                    stack.push(childView)
                } else if (targetType.isInstance(childView) && logic.invoke(targetType.cast(currentView)!!)) {
                    listViews.add(targetType.cast(childView) as T)
                }
            }

            if (targetType.isInstance(currentView) && logic.invoke(targetType.cast(currentView)!!)) {
                listViews.add(targetType.cast(currentView) as T)
            }
        }
        return listViews
    }


    //
    /**
     * 构建View节点树
     * @param viewGroup ViewGroup
     * @return ViewNode viewGroup作为根节点
     */
    fun buildViewTree(viewGroup: ViewGroup): ViewNode {
        //当前视图作为树根
        val rootNode = ViewNode()
        rootNode.parent = null //rootNode.parent = (ViewGroup) viewGroup.getParent();
        rootNode.view = viewGroup
        rootNode.depth = 1
        rootNode.children = ArrayList()
        //构建子树
        return buildViewNodeChild(viewGroup, rootNode, rootNode.depth + 1)
    }

    private fun buildViewNodeChild(viewGroup: ViewGroup, rootNode: ViewNode, depth: Int): ViewNode {
        //获取root视图下的所有子视图
        val childCount = viewGroup.childCount
        if (childCount == 0) return rootNode
        for (i in 0 until childCount) {
            val childAt = viewGroup.getChildAt(i)
            val childNode = ViewNode()
            childNode.parent = viewGroup
            childNode.view = childAt
            childNode.depth = depth
            childNode.children = ArrayList()
            //如果该子视图是ViewGroup, 将它作为下一个根遍历
            if (childAt is ViewGroup) {
                //接入主树
                rootNode.children.add(buildViewNodeChild(childAt, childNode, childNode.depth + 1))
            } else {
                //接入主树
                rootNode.children.add(childNode)
            }
        }
        return rootNode
    }

    data class ViewNode(
        var parent: ViewGroup? = null,  //父视图
        var view: View? = null,  //当前视图
        var depth: Int = 0,  //当前树的深度
        var children: MutableList<ViewNode> = mutableListOf(), //子节点
    ) {

        fun destroy() {
            destroyChildren(this)
        }

        private fun destroyChildren(viewNode: ViewNode) {
            viewNode.parent = null
            viewNode.view = null
            viewNode.depth = 0
            if (viewNode.children.isNotEmpty()) {
                for (child in viewNode.children) {
                    destroyChildren(child)
                }
            }
            viewNode.children.clear()
        }

        fun deep(block: (node: ViewNode) -> Unit) {
            deepChildren(this, block)
        }

        private fun deepChildren(viewNode: ViewNode, block: (node: ViewNode) -> Unit) {
            block.invoke(this)
            if (this.children.isNotEmpty()) {
                for (node in viewNode.children) {
                    deepChildren(node, block)
                }
            }
        }

        fun toSimpleString(): String {
            val view = view ?: return "ViewNode{parent=${parent}, view=null, depth=${depth}, hashCode=${this.hashCode()}}"

            return "ViewNode{view=${view.javaClass.name}" +
                    ", idHex=${getIdHex(view)}" +
                    ", idName=${getIdName(view)}" +
                    ", depth=${depth}" +
                    ", descr=${view.contentDescription}" +
                    ", childrenSize=${children.size}" +
                    ", hashCode=${this.hashCode()}" +
                    "}"
        }

        override fun toString(): String {
            return "ViewNode{" +
                    "parent=${parent}" +
                    ", view=${view}" +
                    ", depth=${depth}" +
                    ", children=${children}" +
                    '}'
        }
    }


    ///
    fun ViewGroup.getViewTree(): ViewNode {
        return buildViewTree(this)
    }

    fun <T : View> ViewGroup.findViewsByType(targetType: Class<T>): List<T> {
        return findViews(this, targetType)
    }

    fun <T : View> ViewGroup.findViewsByExact(targetType: Class<T>, logic: (it: T) -> Boolean): List<T> {
        return findViewsExact(this, targetType, logic)
    }

}