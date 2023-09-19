package com.ss.android.ugc.aweme.lego.lazy;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class LazyFragmentPagerAdapter extends LazyPagerAdapter<Fragment> {
    public FragmentTransaction mCurTransaction;
    public FragmentManager mFragmentManager;
    public boolean shouldCheckAddBeforeDetach;
    public boolean shouldRemoveFragment;

    public static String makeFragmentName(int i1, long l2) {
        throw new RuntimeException("stub!!");
    }

    public Fragment addLazyItem(ViewGroup v1, int i2) {
        throw new RuntimeException("stub!!");
    }

    public void destroyItem(ViewGroup v1, int i2, Object o3) {
        throw new RuntimeException("stub!!");
    }

    public void finishUpdate(ViewGroup v1) {
        throw new RuntimeException("stub!!");
    }

    public long getItemId(int i1) {
        throw new RuntimeException("stub!!");
    }

    public Object instantiateItem(ViewGroup v1, int i2) {
        throw new RuntimeException("stub!!");
    }

    public boolean isViewFromObject(View v1, Object o2) {
        throw new RuntimeException("stub!!");
    }

    public void setPrimaryItem(ViewGroup v1, int i2, Object o3) {
        throw new RuntimeException("stub!!");
    }

    public void setShouldCheckAddBeforeDetach(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void setShouldRemoveFragment(boolean b1) {
        throw new RuntimeException("stub!!");
    }

    public void startUpdate(ViewGroup v1) {
        throw new RuntimeException("stub!!");
    }
}
