package com.ss.android.ugc.aweme.feed.model;

import com.ss.android.ugc.aweme.music.model.Music;
import com.ss.android.ugc.aweme.profile.model.User;
import com.ss.ugc.aweme.ImageUrlStruct;

import java.util.List;

public class Aweme {
    public String aid;

    public User author;

    public Long authorUserId;

    public int awemeType;

    public boolean canPlay;

    public String city;

    public long createTime;

    public String desc;

    public String ipAttribution;

    public String previewTitle;

    public Music music;

    public Video video;

    public List<ImageUrlStruct> images;

    public Aweme() {
        throw new RuntimeException("sub!");
    }
}
