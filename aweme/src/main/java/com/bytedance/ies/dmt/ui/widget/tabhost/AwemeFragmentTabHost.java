package com.bytedance.ies.dmt.ui.widget.tabhost;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bytedance.ies.uikit.tabhost.FragmentTabHost;

public class AwemeFragmentTabHost extends FragmentTabHost {

    public AwemeFragmentTabHost(@NonNull Context context) {
        super(context);
        throw new RuntimeException("sub!");
    }
}