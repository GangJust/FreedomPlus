package dmt.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class DmtViewPager extends ViewGroup {

    public DmtViewPager(Context context) {
        super(context);
        throw new RuntimeException("sub!");
    }

    public DmtViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        throw new RuntimeException("sub!");
    }

    public DmtViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        throw new RuntimeException("sub!");
    }

    public DmtViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        throw new RuntimeException("sub!");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        throw new RuntimeException("sub!");
    }
}
