package com.ss.android.ugc.aweme.feed.model;

import com.ss.android.ugc.aweme.base.model.UrlModel;

public class Video {

    public UrlModel aicover;

    public UrlModel cover;

    public UrlModel downloadAddr;

    public String miscDownloadAddrs;

    public UrlModel newDownloadAddr;

    public VideoUrlModel h264PlayAddr;

    public VideoUrlModel playAddr;

    public VideoUrlModel playAddrH265;

    public UrlModel playAddrBytevc1;

    public UrlModel playAddrLowbr;

    public String playAuthToken;

    public int height;

    public int width;

    private Video() {
        throw new RuntimeException("sub!");
    }
}
