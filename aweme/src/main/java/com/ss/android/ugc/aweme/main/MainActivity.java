package com.ss.android.ugc.aweme.main;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bytedance.ies.uikit.base.AbsActivity;

public  class MainActivity extends AbsActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        throw new RuntimeException("sub!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        throw new RuntimeException("sub!");
    }
}