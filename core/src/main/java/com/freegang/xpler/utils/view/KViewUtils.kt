package com.freegang.xpler.utils.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
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
        try {
            val id = view.id
            if (id == View.NO_ID) {
                return "${View.NO_ID}"
            }
            val resources = view.resources
            val resourceName = resources.getResourceName(id)
            val type = resourceName.substringBefore('/')
            val entryName = resourceName.substringAfter('/')
            val entryId = resources.getIdentifier(entryName, type, resources.getResourcePackageName(id))
            return Integer.toHexString(entryId)
        } catch (e: Exception) {
            return "${View.NO_ID}"
        }
    }

    fun getIdName(view: View): String {
        val id = view.id
        return if (id == View.NO_ID) "${View.NO_ID}" else view.context.resources.getResourceEntryName(id)
    }

    fun getIdName(context: Context, @IdRes resId: Int): String {
        return if (resId == View.NO_ID) "${View.NO_ID}" else context.resources.getResourceEntryName(resId)
    }

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

    
    //
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

    fun <T : View> findViews(viewGroup: ViewGroup, targetType: Class<T>, exact: (v: T) -> Boolean): List<T> {
        val views = mutableListOf<T>()
        //如果符合目标, 添加
        if (targetType.isInstance(viewGroup) && exact.invoke(targetType.cast(viewGroup) as T)) {
            targetType.cast(viewGroup)
        }
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (targetType.isInstance(child) && exact.invoke(targetType.cast(viewGroup) as T)) {
                targetType.cast(child)
            } else if (child is ViewGroup) {
                views.addAll(findViews(child, targetType, exact))
            }
        }
        return views
    }

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
}