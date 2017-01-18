package com.godinsec.privacy.bean;

import android.content.Intent;
import com.godinsec.privacy.utils.AppUtils;

/**
 * Created by dandy on 2016/4/6.
 */
public class ShortCutInfo extends ItemInfo{

    private static final String TAG  = "ShortCutInfo";

    /**
     * The intent used to start the application.
     */
    private Intent intent;


    /**
     * bug点：intent获取有可能是null
     * @param pckame
     * @param launchFlags
     */

    public void setActivity(String pckame,int launchFlags){
       intent = AppUtils.getIntent(pckame,launchFlags);
    }

    public Intent getIntent(){
        return intent;
    }

}
