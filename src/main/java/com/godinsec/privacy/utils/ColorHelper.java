package com.godinsec.privacy.utils;

import android.animation.ArgbEvaluator;
import android.graphics.Color;

/**
 * Created by Seeker on 2016/9/9.
 */

public final class ColorHelper {


    private static final String TAG = "ColorHelper";

    private static ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    public static Integer evaluate(float fraction,int start,int end ){
        return (Integer) colorEvaluator.evaluate(fraction, start, end);
    }

    public static int getColor(float percent,int start,int end){

        int alpha_start = Color.alpha(start);
        float green_start = Color.green(start);
        float red_start = Color.red(start);
        float biue_start = Color.blue(start);

        int alpha_offset = Color.alpha(end) - alpha_start;
        float green_offset = Color.green(end) - green_start;
        float red_offset = Color.red(end) - red_start;
        float biue_offset = Color.blue(end) - biue_start;
        return Color.argb((int) (alpha_start + alpha_offset * percent),
                (int) (red_start + red_offset * percent),
                (int) (green_start + green_offset * percent),
                (int) (biue_start + biue_offset * percent));
    }

}
