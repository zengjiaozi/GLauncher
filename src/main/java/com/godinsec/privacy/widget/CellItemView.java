package com.godinsec.privacy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by dandy on 2016/5/25.
 */
public class CellItemView extends RelativeLayout{

    private static final String TAG = "CellItemView";

    private ImageView icon;

    public CellItemView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        icon = (ImageView)getChildAt(0);
    }

    public void handlerOnTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                icon.setAlpha(0.7f);
                break;
            default:
                icon.setAlpha(1.0f);
                break;
        }
    }
}
