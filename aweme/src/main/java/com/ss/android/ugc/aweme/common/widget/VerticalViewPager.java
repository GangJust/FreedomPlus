package com.ss.android.ugc.aweme.common.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Scroller;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class VerticalViewPager extends ViewGroup {
    public VerticalViewPager(Context context) {
        super(context);
        throw new RuntimeException("stub!!");
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new RuntimeException("stub!!");
    }

    public VerticalViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new RuntimeException("stub!!");
    }

    public VerticalViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        throw new RuntimeException("stub!!");
    }

    private int getClientHeight() {
        throw new RuntimeException("stub!!");
    }

    private void setScrollingCacheEnabled(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void addFocusables(ArrayList a1, int i2, int i3) {
        throw new RuntimeException("stub!!");
    }

    public void addTouchables(ArrayList a1) {
        throw new RuntimeException("stub!!");
    }

    public void addView(View v1, int i2, LayoutParams l3) {
        throw new RuntimeException("stub!!");
    }

    public boolean canScrollVertically(int i1) {
        throw new RuntimeException("stub!!");
    }

    public boolean checkLayoutParams(LayoutParams l1) {
        throw new RuntimeException("stub!!");
    }

    public void computeScroll() {
        throw new RuntimeException("stub!!");
    }

    public boolean dispatchKeyEvent(KeyEvent k1) {
        throw new RuntimeException("stub!!");
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent a1) {
        throw new RuntimeException("stub!!");
    }

    public boolean dispatchStatusBarTap() {
        throw new RuntimeException("stub!!");
    }

    public boolean dispatchTouchEvent(MotionEvent m1) {
        throw new RuntimeException("stub!!");
    }

    public void draw(Canvas c1) {
        super.draw(c1);
        throw new RuntimeException("stub!!");
    }

    public void drawableStateChanged() {
        super.drawableStateChanged();
        throw new RuntimeException("stub!!");
    }

    public LayoutParams generateDefaultLayoutParams() {
        throw new RuntimeException("stub!!");
    }

    public LayoutParams generateLayoutParams(AttributeSet a1) {
        throw new RuntimeException("stub!!");
    }

    public LayoutParams generateLayoutParams(LayoutParams l1) {
        throw new RuntimeException("stub!!");
    }

    public PagerAdapter getAdapter() {
        throw new RuntimeException("stub!!");
    }

    public int getChildDrawingOrder(int i1, int i2) {
        throw new RuntimeException("stub!!");
    }

    public int getCurrentAnimDuration() {
        throw new RuntimeException("stub!!");
    }

    public int getCurrentItem() {
        throw new RuntimeException("stub!!");
    }

    public int getExpectedAdapterCount() {
        throw new RuntimeException("stub!!");
    }

    public boolean getFirstLayout() {
        throw new RuntimeException("stub!!");
    }

    public int getOffscreenPageLimit() {
        throw new RuntimeException("stub!!");
    }

    public int getPageMargin() {
        throw new RuntimeException("stub!!");
    }

    public int getScrollState() {
        throw new RuntimeException("stub!!");
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        throw new RuntimeException("stub!!");
    }

    public void onConfigurationChanged(Configuration c1) {
        throw new RuntimeException("stub!!");
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        throw new RuntimeException("stub!!");
    }

    public void onDraw(Canvas c1) {
        throw new RuntimeException("stub!!");
    }

    public boolean onInterceptTouchEvent(MotionEvent m1) {
        throw new RuntimeException("stub!!");
    }

    public void onLayout(boolean b1, int i2, int i3, int i4, int i5) {
        throw new RuntimeException("stub!!");
    }

    public void onMeasure(int i1, int i2) {
        throw new RuntimeException("stub!!");
    }

    public boolean onRequestFocusInDescendants(int i1, Rect r2) {
        throw new RuntimeException("stub!!");
    }

    public void onRestoreInstanceState(Parcelable p1) {
        super.onRestoreInstanceState(p1);
        throw new RuntimeException("stub!!");
    }

    public Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        throw new RuntimeException("stub!!");
    }

    public void onScrollChanged(int i1, int i2, int i3, int i4) {
        throw new RuntimeException("stub!!");
    }

    public void onSizeChanged(int i1, int i2, int i3, int i4) {
        throw new RuntimeException("stub!!");
    }

    public boolean onTouchEvent(MotionEvent m1) {
        throw new RuntimeException("stub!!");
    }

    public void removeView(View v1) {
        throw new RuntimeException("stub!!");
    }

    public void scrollTo(int i1, int i2) {
        throw new RuntimeException("stub!!");
    }

    public void setAdapter(PagerAdapter p1) {
        throw new RuntimeException("stub!!");
    }

    public void setCanTouch(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setChildrenDrawingOrderEnabledCompat(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setCurrentItem(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setCurrentItemWithDefaultVelocity(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setDisableScroll(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setEnablePrefetchView(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setEnablePrepopulate(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setEventType(String s1) {
        throw new RuntimeException("stub!!");
    }

    public void setLivePreviewScrollableAngle(double d1) {
        throw new RuntimeException("stub!!");
    }

    public void setOffscreenPageLimit(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setPageMargin(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setPageMarginDrawable(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setPageMarginDrawable(Drawable d1) {
        throw new RuntimeException("stub!!");
    }

    public void setScrollState(int i1) {
        throw new RuntimeException("stub!!");
    }

    public void setScroller(Scroller s1) {
        throw new RuntimeException("stub!!");
    }

    public boolean verifyDrawable(Drawable d1) {
        super.verifyDrawable(d1);
        throw new RuntimeException("stub!!");
    }
}