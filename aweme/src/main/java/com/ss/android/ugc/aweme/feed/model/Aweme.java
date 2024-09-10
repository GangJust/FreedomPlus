package com.ss.android.ugc.aweme.feed.model;

import com.ss.android.ugc.aweme.comment.model.Comment;
import com.ss.android.ugc.aweme.music.model.Music;
import com.ss.android.ugc.aweme.profile.model.User;
import com.ss.ugc.aweme.ImageUrlStruct;

import java.util.List;

public abstract class Aweme {
    public String aid;

    public User author;

    public Long authorUserId;

    public int anchorType;

    public int awemeType;

    public boolean canPlay;

    public String city;

    public long createTime;

    public String desc;

    public Integer duration;

    public String ipAttribution;

    public String previewTitle;

    public String extra;

    public String extraInfo;

    public Music music;

    public Video video;

    public List<ImageUrlStruct> images;

    public String shareUrl;

    public Comment commentFeedOuterComment;

    public Aweme() {
        throw new RuntimeException("stub!!");
    }

    // stub methods

    // this.externalType == 1 || this.awemeType == 107
    public abstract boolean isAwemeFromXiGua();

    // this.awemeType == 122
    public abstract boolean isCloseMoment();

    // this.awemeType != 127 && this.awemeType != 128
    public abstract boolean isCopyRightLongVideo();

    // this.awemeType == 115
    public abstract boolean isExplainReplay();

    // this.awemeType == 13
    public abstract boolean isForwardAweme();

    // this.awemeType == 104
    public abstract boolean isHotSpotRankCard();

    // this.awemeType == 2
    public abstract boolean isImage();

    // this.awemeType == 101
    public abstract boolean isLive();

    // this.awemeType == 68
    public abstract boolean isMultiImage();

    // this.awemeType == 117
    public abstract boolean isMusicVideo();

    // this.awemeType == 3002
    public abstract boolean isPoiOperate();

    // this.awemeType == 112
    public abstract boolean isProductCard();

    // this.awemeType == 33 || this.adAwemeSource == 1
    public abstract boolean isUserPost();

    // this.awemeType == 105
    public abstract boolean isVS();

    // this.awemeType == 1 || this.awemeType == 29 || this.awemeType == 30 || this.awemeType == 32 || this.awemeType == 33 || this.awemeType == 201
    public abstract boolean commerceVideoTypeAllowDuetReact();

    // this.awemeType == 140
    public abstract boolean isLifeSpuCard();

    // this.awemeType != 0 && this.awemeType != 51 && this.awemeType != 52 && this.awemeType != 58 && this.awemeType != 54 && this.awemeType != 53 && this.awemeType != 61 && this.awemeType != 109 && this.awemeType != 55 && this.awemeType != 56 && this.awemeType != 62 && this.awemeType != 66 && this.awemeType != 67 && !commerceVideoTypeAllowDuetReact() && this.awemeType != 110 && this.awemeType != 117 && this.awemeType != 118 && this.awemeType != 68 && this.awemeType != 122
    public abstract boolean canDuetVideoType();

    // this.awemeType != 52 && this.awemeType != 0 && this.awemeType != 51 && this.awemeType != 58 && this.awemeType != 54 && this.awemeType != 53 && this.awemeType != 61 && this.awemeType != 109 && this.awemeType != 55 && this.awemeType != 56 && this.awemeType != 62 && this.awemeType != 66 && this.awemeType != 67 && !commerceVideoTypeAllowDuetReact() && this.awemeType != 110 && this.awemeType != 118
    public abstract boolean canReactVideoType();

    // this.awemeType != 0 && this.awemeType != 51 && this.awemeType != 52 && this.awemeType != 54 && this.awemeType != 53 && this.awemeType != 61 && this.awemeType != 109 && this.awemeType != 55 && this.awemeType != 56 && this.awemeType != 62 && this.awemeType != 66 && this.awemeType != 67 && !commerceVideoTypeAllowDuetReact() && this.awemeType != 110
    public abstract boolean canStitchVideoType();

    public abstract boolean existsInCollectionFolder();

    public abstract long getCreatedTimeInMillSec();

    public abstract boolean hasCloudGame();

    public abstract boolean isAdNearbyCard();

    public abstract boolean isSupportGameChallenge();

    public abstract boolean needPreloadAdLink();

    public abstract boolean isAd();

    public abstract boolean isTeenVideo();
}
