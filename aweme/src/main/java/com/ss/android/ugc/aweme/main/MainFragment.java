package com.ss.android.ugc.aweme.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.ies.uikit.base.AbsFragment;

public class MainFragment extends AbsFragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        throw new RuntimeException("sub!");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        throw new RuntimeException("sub!");
    }

    @Override
    public void onPause() {
        super.onPause();
        throw new RuntimeException("sub!");
    }
}
