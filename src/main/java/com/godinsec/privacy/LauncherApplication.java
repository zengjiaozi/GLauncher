package com.godinsec.privacy;

import android.app.Application;

import com.godinsec.privacy.utils.Configure;
import com.godinsec.privacy.utils.SharedPreferencesUtils;

/**
 * Created by Seeker on 2016/9/9.
 */

public class LauncherApplication extends Application{

    private static LauncherApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        LauncherApplication.instance = this;
        Configure.setUpItemSize();
        SharedPreferencesUtils.getInstance().init(this,"privacyLaucnher.xml");
    }

    public static LauncherApplication getInstance(){
        return LauncherApplication.instance;
    }


}
