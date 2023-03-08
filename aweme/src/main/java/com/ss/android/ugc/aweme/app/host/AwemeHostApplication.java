package com.ss.android.ugc.aweme.app.host;

import android.app.Application;

public class AwemeHostApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        throw new RuntimeException("sub!");
    }
}