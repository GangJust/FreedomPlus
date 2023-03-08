package com.freegang.xpler.utils.view;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/// View 工具类
@Deprecated(since = "KViewUtils完善中, 该类即将作废!")
public class GViewUtils {

    @IntDef({VISIBLE, INVISIBLE, GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
        ///
    }

    private GViewUtils() {
        ///
    }

    public static void setVisibleAll(ViewGroup viewGroup) {
        setVisibilityAll(viewGroup, VISIBLE);
    }

    public static void setGoneAll(ViewGroup viewGroup) {
        setVisibilityAll(viewGroup, GONE);
    }

    public static void setInvisibleAll(ViewGroup viewGroup) {
        setVisibilityAll(viewGroup, INVISIBLE);
    }

    /**
     * 对某个 ViewGroup 下的所有子视图进行 Visibility 设置
     *
     * @param viewGroup
     * @param visibility
     */
    public static void setVisibilityAll(ViewGroup viewGroup, @Visibility int visibility) {
        int childCount = viewGroup.getChildCount();
        if (childCount == 0) return;
        // 先递归遍历设置所有子视图的 Visibility
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                setVisibilityAll((ViewGroup) childAt, visibility);
            }
            childAt.setVisibility(visibility);
        }
        // 再设置当前视图的 Visibility
        viewGroup.setVisibility(visibility);
    }


    /**
     * 判断某个视图是否可见
     *
     * @param view
     * @return
     */
    public static boolean isVisible(View view) {
        return view.getVisibility() == VISIBLE;
    }

    /**
     * 判断某个视图是否隐藏
     *
     * @param view
     * @return
     */
    public static boolean isGon(View view) {
        return view.getVisibility() == GONE;
    }

    /**
     * 判断某个视图是否不可见, 但是仍然占位
     *
     * @param view
     * @return
     */
    public static boolean isInvisible(View view) {
        return view.getVisibility() == INVISIBLE;
    }


    /**
     * 某个父视图下的所有视图是否可见
     *
     * @param viewGroup
     * @return
     */
    public static boolean isVisibleAll(ViewGroup viewGroup) {
        List<Integer> resultList = new ArrayList<>();
        _traverseVisibilityAll(viewGroup, resultList);
        return !resultList.contains(GONE); //不包含 GONE
    }

    /**
     * 某个父视图下的所有视图是否隐藏
     *
     * @param viewGroup
     * @return
     */
    public static boolean isGonAll(ViewGroup viewGroup) {
        List<Integer> resultList = new ArrayList<>();
        _traverseVisibilityAll(viewGroup, resultList);
        return !resultList.contains(VISIBLE);  //不包含 VISIBLE
    }

    /**
     * 某个父视图下的所有视图是否不可见, 但仍然占位
     *
     * @param viewGroup
     * @return
     */
    public static boolean isInvisibleAll(ViewGroup viewGroup) {
        List<Integer> resultList = new ArrayList<>();
        _traverseVisibilityAll(viewGroup, resultList);
        return !(resultList.contains(GONE) || resultList.contains(VISIBLE));  //不包含 GONE 或 VISIBLE
    }

    /**
     * 递归遍历某个 ViewGroup 的 Visibility
     *
     * @param viewGroup
     * @param resultList
     */
    private static void _traverseVisibilityAll(ViewGroup viewGroup, List<Integer> resultList) {
        if (viewGroup.getChildCount() == 0) {
            resultList.add(viewGroup.getVisibility());
            return;
        }

        // 先递归获取所有子 View、ViewGroup 的 Visibility
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                _traverseVisibilityAll((ViewGroup) childAt, resultList);
            } else {
                resultList.add(childAt.getVisibility());
            }
        }

        // 再获取当前 ViewGroup 的 Visibility
        resultList.add(viewGroup.getVisibility());
    }


    /**
     * 某个视图是否可用
     *
     * @param view
     * @return
     */
    public static boolean isEnabled(View view) {
        if (view == null) return false;
        return view.isEnabled();
    }

    /**
     * 某个父视图下的所有视图是否可用
     *
     * @param viewGroup
     * @return
     */
    public static boolean isEnabledAll(ViewGroup viewGroup) {
        if (viewGroup.getChildCount() == 0) {
            return viewGroup.isEnabled();
        }

        // 先递归获取所有子 View、ViewGroup
        List<Boolean> resultList = new ArrayList<>();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                //对子视图做判断, 同理 return !resultList.contains(false);
                resultList.add(isEnabledAll((ViewGroup) childAt));
            }
            resultList.add(childAt.isEnabled());
        }
        // 再获取当前 ViewGroup
        resultList.add(viewGroup.isEnabled());

        return !resultList.contains(false); //不包含false
    }


    /**
     * 获取某个视图的IdName
     *
     * @param view 需要获取IdName的视图
     * @return 该视图的IdName, 若没有, 返回 `-1`
     */
    public static String getIdName(View view) {
        int id = view.getId();
        if (id == -1) return "-1";
        return view.getContext().getResources().getResourceEntryName(id);
    }

    /**
     * 获取某个id对应的idName
     *
     * @param context 视图上下文
     * @param resId   id值, 应该是一个id资源值, 可以是 @drawable、@string、@layout 等一系列Resource
     * @return 对应的idName
     */
    public static String getIdName(Context context, @IdRes int resId) {
        if (resId == -1) return "-1";
        return context.getResources().getResourceEntryName(resId);
    }

    /**
     * 遍历ViewGroup 获取所有子View,
     * 该方法会遍历xml节点树,
     * 将指定对象获取到线性数组中(前序遍历将会彻底打乱层级, 但不会打乱顺序)
     *
     * @param viewGroup 父视图
     * @return 所有子视图, 包括子视图中的 ViewGroup
     */
    public static List<View> deepViewGroup(ViewGroup viewGroup) {
        List<View> list = new ArrayList<>();
        list.add(viewGroup);

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                list.addAll(deepViewGroup((ViewGroup) childAt));
            } else {
                //如果符合目标, 添加
                list.add(childAt);
            }
        }
        return list;
    }

    /**
     * 遍历ViewGroup, 查找指定类型的子View,
     * 该方法会遍历xml节点树,
     * 将指定对象获取到线性数组中(前序遍历将会彻底打乱层级, 但不会打乱顺序)
     *
     * @param viewGroup  父视图
     * @param targetType 目标视图, 可以是View
     * @param <T>        T extend View
     * @return 找到的所有 T 型 View
     */
    public static <T extends View> List<T> findViews(ViewGroup viewGroup, Class<T> targetType) {
        List<T> listViews = new ArrayList<>();
        //如果符合目标, 添加
        if (targetType.isInstance(viewGroup)) {
            listViews.add(targetType.cast(viewGroup));
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                listViews.addAll(findViews((ViewGroup) childAt, targetType));
            } else {
                //如果符合目标, 添加
                if (targetType.isInstance(childAt)) {
                    listViews.add(targetType.cast(childAt));
                }
            }
        }

        return listViews;
    }

    /**
     * 遍历ViewGroup, 查找指定类型的子View,
     * 该方法会遍历xml节点树,
     * 并且当 getContentDescription().toString().contains(containsDesc) 文本时,
     * 将指定对象获取到线性数组中(前序遍历将会彻底打乱层级, 但不会打乱顺序)
     *
     * @param viewGroup    父视图
     * @param targetType   目标视图, 可以是View
     * @param containsDesc 被包含的描述文本
     * @param <T>          T extend View
     * @return 找到的所有 T 型 View
     */
    public static <T extends View> List<T> findViewsByDesc(ViewGroup viewGroup, Class<T> targetType, String containsDesc) {
        List<T> listViews = new ArrayList<>();
        if (containsDesc == null) return listViews;

        //如果符合目标, 添加
        CharSequence parentDesc = viewGroup.getContentDescription();
        if (targetType.isInstance(viewGroup) && parentDesc != null && parentDesc.toString().contains(containsDesc)) {
            listViews.add(targetType.cast(viewGroup));
        }

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                listViews.addAll(findViewsByDesc((ViewGroup) childAt, targetType, containsDesc));
            } else {
                //如果符合目标, 添加
                CharSequence childDesc = childAt.getContentDescription();
                if (targetType.isInstance(childAt) && childDesc != null && childDesc.toString().contains(containsDesc)) {
                    listViews.add(targetType.cast(childAt));
                }
            }
        }
        return listViews;
    }

    /**
     * 遍历ViewGroup, 查找指定类型的子View,
     * 该方法会遍历xml节点树,
     * 并且当 IDName 匹配时(值得注意的是: IDName是可以相同的[当include时或视图动态添加时, IDName相同的情况多了去了], 不可相同的是IDHex值)
     * 将指定对象获取到线性数组中(前序遍历将会彻底打乱层级, 但不会打乱顺序)
     * (更简明来说: ListView->ItemView, RecyclerView->ItemView等等.. IDName也是相同的)
     *
     * @param viewGroup  父视图
     * @param targetType 目标视图, 可以是View
     * @param idName     目标IDName
     * @param <T>        T extend View
     * @return 找到的所有 T 型 View
     */
    public static <T extends View> List<T> findViewsByIdName(ViewGroup viewGroup, Class<T> targetType, String idName) {
        List<T> listViews = new ArrayList<>();
        if (idName == null || idName.trim().isEmpty() || idName.equals("-1")) return listViews;

        //如果符合目标, 添加
        String parentIdName = getIdName(viewGroup);
        if (targetType.isInstance(viewGroup) && parentIdName.equals(idName)) {
            listViews.add(targetType.cast(viewGroup));
        }

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                listViews.addAll(findViewsByIdName((ViewGroup) childAt, targetType, idName));
            } else {
                //如果符合目标, 添加
                String childIdName = getIdName(childAt);
                if (targetType.isInstance(childAt) && childIdName.equals(idName)) {
                    listViews.add(targetType.cast(childAt));
                }
            }
        }
        return listViews;
    }

    /**
     * 遍历ViewGroup, 精确查找指定类型的的某些子View,
     * <p>
     * 例子：
     * List<TextView> textViews = GViewUtils.findViewExact(root, TextView.class, tv -> {
     * String text = tv.getText().toString();
     * CharSequence desc = tv.getContentDescription();
     * return text.contains("张三") && desc == null;
     * });
     *
     * @param viewGroup  父视图
     * @param targetType 目标视图, 可以是View
     * @param exact      自定义逻辑, 该参数是一个函数式接口,
     *                   回调所有找到的 @{targetType}, 需要你自行进行相关逻辑判断,
     *                   根据自定义逻辑`true` or `false` 返回匹配到的所有视图
     * @param <T>        T extend View
     * @return 精确找到的所有视图
     */
    public static <T extends View> List<T> findViewsExact(ViewGroup viewGroup, Class<T> targetType, FindViewExactFunction<T> exact) {
        List<T> listViews = new ArrayList<>();

        // 如果符合目标, 添加
        if (targetType.isInstance(viewGroup) && exact.logic(targetType.cast(viewGroup))) {
            listViews.add(targetType.cast(viewGroup));
        }

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                listViews.addAll(findViewsExact((ViewGroup) childAt, targetType, exact));
            } else {
                // 如果符合目标, 添加
                if (targetType.isInstance(childAt) && exact.logic(targetType.cast(childAt))) {
                    listViews.add(targetType.cast(childAt));
                }
            }
        }
        return listViews;
    }


    /**
     * 将某个ViewGroup 转换为 视图多叉树
     *
     * @param viewGroup 目标根视图
     * @return 多叉树root节点
     */
    public static GViewNode getViewTree(ViewGroup viewGroup) {
        //当前视图作为树根
        GViewNode rootNode = new GViewNode();
        rootNode.parent = null;  //rootNode.parent = (ViewGroup) viewGroup.getParent();
        rootNode.view = viewGroup;
        rootNode.depth = 1;
        rootNode.children = new ArrayList<>();
        //构建子树
        return _buildViewNodeChild(viewGroup, rootNode, rootNode.depth + 1);
    }

    private static GViewNode _buildViewNodeChild(ViewGroup viewGroup, GViewNode rootNode, int depth) {
        //获取root视图下的所有子视图
        int childCount = viewGroup.getChildCount();
        if (childCount == 0) return rootNode;
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            GViewNode childNode = new GViewNode();
            childNode.parent = viewGroup;
            childNode.view = childAt;
            childNode.depth = depth;
            childNode.children = new ArrayList<>();
            //如果该子视图是ViewGroup, 将它作为下一个根遍历
            if (childAt instanceof ViewGroup) {
                //接入主树
                rootNode.children.add(_buildViewNodeChild((ViewGroup) childAt, childNode, childNode.depth + 1));
            } else {
                //接入主树
                rootNode.children.add(childNode);
            }
        }
        return rootNode;
    }

    /**
     * 测试视图多叉树
     *
     * @param viewNode 需要测试的树节点, 允许是子节点, 也可以是根节点.
     *                 该方法将某个节点下的所有子节点作层级打印
     */
    public static void testViewTree(GViewNode viewNode) {
        System.out.println("┌────────────────────────────────────────────────────────");
        System.out.println("├" + viewNode.toSimpleString());
        _printlnViewTree(viewNode, "---");
        System.out.println("└────────────────────────────────────────────────────────");
    }

    private static void _printlnViewTree(GViewNode viewNode, String indent) {
        if (viewNode.children.isEmpty()) return;
        for (GViewNode child : viewNode.children) {
            System.out.println("├" + indent + child.toSimpleString());
            if (!child.children.isEmpty()) {
                _printlnViewTree(child, indent + "---");
            }
        }
    }

    /**
     * 摧毁视图多叉树
     *
     * @param viewNode 多叉树节点, 允许是子节点, 也可以是根节点.
     *                 该方法将某个节点下的所有子节点释放销毁
     */
    public static void destroyViewTree(GViewNode viewNode) {
        if (viewNode == null) return;
        viewNode.destroy();
    }


    /// 反射获取事件监听器 View$ListenerInfo 内部类
    private static <T extends View> Object getListenerInfo(T view) {
        try {
            Field mListenerInfoField = findFieldRecursiveImpl(view.getClass(), "mListenerInfo");
            mListenerInfoField.setAccessible(true);
            return mListenerInfoField.get(view);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取点击事件, 如果有, 否则返回NULL
     *
     * @param view 需要获取点击事件的视图
     * @return 返回该点击事件的具体实现, 需要响应点击, 请手动调用 onClick 方法.
     */
    public static <T extends View> View.OnClickListener getOnClickListener(T view) {
        Object listenerInfo = getListenerInfo(view);
        if (listenerInfo == null) return null;
        try {
            Field mOnClickListenerField = listenerInfo.getClass().getDeclaredField("mOnClickListener");
            mOnClickListenerField.setAccessible(true);
            Object mOnClickListener = mOnClickListenerField.get(listenerInfo);
            if (mOnClickListener instanceof View.OnClickListener) {
                return (View.OnClickListener) mOnClickListener;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取点击事件, 如果有, 否则返回NULL
     *
     * @param view 需要获取点击事件的视图
     * @return 返回该点击事件的具体实现, 需要响应长按, 请手动调用 onLongClick 方法.
     */
    public static <T extends View> View.OnLongClickListener getOnLongClickListener(T view) {
        Object listenerInfo = getListenerInfo(view);
        if (listenerInfo == null) return null;
        try {
            Field mOnLongClickListenerField = listenerInfo.getClass().getDeclaredField("mOnLongClickListener");
            mOnLongClickListenerField.setAccessible(true);
            Object mOnLongClickListener = mOnLongClickListenerField.get(listenerInfo);
            if (mOnLongClickListener instanceof View.OnLongClickListener) {
                return (View.OnLongClickListener) mOnLongClickListener;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取触摸事件, 如果有, 否则返回NULL
     *
     * @param view 需要获取触摸事件的视图
     * @return 返回该点击事件的具体实现, 需要响应触摸, 请手动调用 onTouch 方法.
     */
    public static <T extends View> View.OnTouchListener getOnTouchListener(T view) {
        Object listenerInfo = getListenerInfo(view);
        if (listenerInfo == null) return null;
        try {
            Field mOnTouchListenerField = listenerInfo.getClass().getDeclaredField("mOnTouchListener");
            mOnTouchListenerField.setAccessible(true);
            Object mOnTouchListener = mOnTouchListenerField.get(listenerInfo);
            if (mOnTouchListener instanceof View.OnTouchListener) {
                return (View.OnTouchListener) mOnTouchListener;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 参照: XposedHelpers#findFieldRecursiveImpl
     * see at: GReflectUtils.java
     *
     * @param clazz     目标class
     * @param fieldName 字段名
     * @return 找到的字段
     * @throws NoSuchFieldException 未找到
     */
    private static Field findFieldRecursiveImpl(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class))
                    break;

                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    //精确查找视图扩展, 函数式接口
    public interface FindViewExactFunction<T> {
        boolean logic(T o);
    }

    //视图节点多叉树
    public static class GViewNode {
        //父视图
        public ViewGroup parent;
        //当前视图
        public View view;
        //当前树的深度
        public int depth;
        //子节点
        public List<GViewNode> children;

        //销毁节点树
        public void destroy() {
            destroyChildren(this);
        }

        private void destroyChildren(GViewNode viewNode) {
            viewNode.parent = null;
            viewNode.view = null;
            viewNode.depth = 0;
            if (!viewNode.children.isEmpty()) {
                for (GViewNode child : viewNode.children) {
                    destroyChildren(child);
                }
            }
            viewNode.children.clear();
        }

        public String toSimpleString() {
            if (view == null) {
                return "ViewNode{parent=" + parent + ", view=null, depth=" + depth + ", hashCode=" + this.hashCode() + "}";
            }

            try {
                return "ViewNode{view=" + view.getClass().getName() +
                        ", idHex=" + (view.getId() == -1 ? "-1" : "0x" + Integer.toHexString(view.getId())) +
                        ", idName=" + (view.getId() == -1 ? "-1" : "@id/" + view.getContext().getResources().getResourceEntryName(view.getId())) +
                        ", depth=" + depth +
                        ", descr=" + view.getContentDescription() +
                        ", childrenSize=" + children.size() +
                        ", hashCode=" + this.hashCode() +
                        "}";
            } catch (Exception e) {
                e.printStackTrace();
                return "ViewNode{view=" + view.getClass().getName() +
                        ", idHex=" + (view.getId() == -1 ? "-1" : "0x" + Integer.toHexString(view.getId())) +
                        ", idName=获取失败" +
                        ", depth=" + depth +
                        ", descr=" + view.getContentDescription() +
                        ", childrenSize=" + children.size() +
                        "}";
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "ViewNode{" +
                    "parent=" + parent +
                    ", view=" + view +
                    ", depth=" + depth +
                    ", children=" + children +
                    '}';
        }
    }
}