package com.ss.android.ugc.aweme.lego.lazy;

import android.util.SparseArray;

import androidx.viewpager.widget.PagerAdapter;

public abstract class LazyPagerAdapter<T> extends PagerAdapter {
    public T mCurrentItem;
    public SparseArray<T> mLazyItems = new SparseArray<>();
}
