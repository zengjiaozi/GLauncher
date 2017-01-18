package com.godinsec.privacy.utils;

import android.content.Context;
import android.view.WindowManager;

import com.godinsec.privacy.LauncherApplication;

/**
 * Created by Seeker on 2016/9/9.
 */

public final class Configure {

    public static final int APPADD = 0x001989;
    public static final int APPREMOVE = 0x001990;

    public static int currentColumn;

    public static int currentRow;

    /**每个item的大小**/
    public static int itemWidth,itemHeight,iconSize;

    /***/

    /**
     * 初始化大小，分布
     */
    public static void setUpItemSize(){

        currentColumn = DefaultSingleScreenAppCount.column;
        currentRow = DefaultSingleScreenAppCount.row;

        WindowManager wm = (WindowManager) LauncherApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        iconSize = wm.getDefaultDisplay().getWidth() / currentColumn /(currentColumn -1);
    }

    public static void setUpItemSize(int parentWidth,int parentHeight){
        itemWidth = parentWidth / currentColumn;
        itemHeight = (int)(parentHeight / (currentRow + 0.6));
        iconSize = itemWidth / (currentColumn -1);
    }

    /**
     * 每屏显示的个数
     */
    public static final class DefaultSingleScreenAppCount{
        public static final int column = 3;
        public static final int row = 4;
    }


}
