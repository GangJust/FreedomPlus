package com.bytedance.ies.uikit.tabhost;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

/// 底部tabbar 视频、朋友、消息、我 等
public class FragmentTabHost extends TabHost {
    public FragmentTabHost(Context context) {
        super(context);
    }

    public FragmentTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FragmentTabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FragmentTabHost(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //切换
    public void onTabChanged(String s) {
        throw new RuntimeException("sub!");
    }
}