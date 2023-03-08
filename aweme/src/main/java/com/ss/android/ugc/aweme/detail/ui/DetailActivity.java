package com.ss.android.ugc.aweme.detail.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bytedance.ies.uikit.base.AbsActivity;

public class DetailActivity extends AbsActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        throw new RuntimeException("sub!");
    }
}
