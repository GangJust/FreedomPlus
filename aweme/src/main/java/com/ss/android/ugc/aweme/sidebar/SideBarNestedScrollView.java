package com.ss.android.ugc.aweme.sidebar;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class SideBarNestedScrollView extends NestedScrollView {
    public SideBarNestedScrollView(@NonNull Context context) {
        super(context);
        throw new RuntimeException("sub!");
    }

    public SideBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        throw new RuntimeException("sub!");
    }

    public SideBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new RuntimeException("sub!");
    }
}
