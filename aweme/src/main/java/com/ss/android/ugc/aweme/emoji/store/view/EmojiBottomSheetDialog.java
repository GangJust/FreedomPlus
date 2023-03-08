package com.ss.android.ugc.aweme.emoji.store.view;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class EmojiBottomSheetDialog extends BottomSheetDialog {
    public EmojiBottomSheetDialog(@NonNull Context context) {
        super(context);
        throw new RuntimeException("sub!");
    }

    public EmojiBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        throw new RuntimeException("sub!");
    }

    protected EmojiBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        throw new RuntimeException("sub!");
    }
}
