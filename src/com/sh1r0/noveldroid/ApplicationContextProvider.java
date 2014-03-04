package com.sh1r0.noveldroid;

import android.app.Application;
import android.content.Context;

public class ApplicationContextProvider extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getContext() {
        return appContext;
    }
}
