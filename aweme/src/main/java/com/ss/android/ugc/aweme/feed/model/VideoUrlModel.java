package com.ss.android.ugc.aweme.feed.model;

import com.ss.android.ugc.aweme.base.model.UrlModel;

public class VideoUrlModel extends UrlModel {
    public float aspectRatio;
    public String bitrateFormat;
    public long cdnUrlExpired;
    public int codecType;
    public long createTime;
    public double duration;
    public Boolean hasDashBitrate;
    public String mDashVideoId;
    public String mDashVideoModelString;
    public boolean mVr;
    public String meta;
    public String ratio;
    public String ratioUri;
    public String sourceId;

    public VideoUrlModel() {
        throw new RuntimeException("stub!!");
    }
}
