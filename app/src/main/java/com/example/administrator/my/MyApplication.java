package com.example.administrator.my;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/6/8/008.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext () {
        return mContext;
    }
}
