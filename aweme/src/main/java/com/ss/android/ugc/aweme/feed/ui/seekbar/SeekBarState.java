package com.ss.android.ugc.aweme.feed.ui.seekbar;

public class SeekBarState {
    public enum Action {
        PAUSE,
        RESUME,
        DRAG,
        RELEASE,
        TIMEOUT,
        FREEZE,
        UNFREEZE,
        VIDEO_CHANGE,
        RENDER_FIRST,
        SEARCH_SEEK,
        LONG_PRESS,
        RELEASE_LONG_PRESS,
        REFRESH_DEFAULT,
        CONTINUE_PLAY,
    }
}
