package com.example.devicemonitor;

import android.graphics.drawable.Drawable;

public class AppModel {

    public String appName;
    public String packageName;
    public Drawable appIcon;
    public boolean isSystemApp;
    public String installSource;

    public AppModel(String appName,
                    String packageName,
                    Drawable appIcon,
                    boolean isSystemApp,
                    String installSource) {

        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isSystemApp = isSystemApp;
        this.installSource = installSource;
    }
}