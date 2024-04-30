package com.ss.android.ugc.aweme.profile.model;

import com.ss.android.ugc.aweme.base.model.UrlModel;

public class User {

    public String accountInfoUrl;

    public int accountType;

    public String adOrderId;


    public int age;

    public Integer appleAccount;

    public UrlModel avatar168x168;

    public UrlModel avatar300x300;

    public UrlModel avatarLarger;

    public UrlModel avatarMedium;

    public UrlModel avatarPendantLarger;

    public UrlModel avatarPendantMedium;

    public UrlModel avatarPendantThumb;

    public UrlModel avatarThumb;

    public String avatarUri;

    public UrlModel avatarVideoUri;

    public int awemeCount;

    public String category;

    public String city;

    public String cityName;

    public String country;

    public String district;

    public Long createTime;

    public String email;

    public int followerCount;

    public int friendCount;

    public int gender;

    public String ipLocation;

    public String nickname;

    public int onlineStatus;

    public long playCount;

    public String shortId;

    public String uniqueId;

    public Integer userAge;

    public String uid;

    //1: 已关注, 0: 未关注
    public int getFollowStatus() {
        throw new RuntimeException("Stub!");
    }
}
