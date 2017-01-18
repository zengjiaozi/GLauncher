package com.godinsec.privacy.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.godinsec.privacy.LauncherApplication;
import com.godinsec.privacy.bean.Favorite;
import com.godinsec.launcher.R;
import com.godinsec.privacy.utils.AppUtils;
import com.godinsec.privacy.utils.Validate;


/**
 * Created by dandy on 2016/5/24.
 */
public class HotSetView extends LinearLayout implements View.OnClickListener{

    private static final String TAG = "HotSetView";

    private ImageView icon;

    private TextView title;

    private Drawable iconBack;

    private String titleText;

    private boolean canAlpha = true;

    private Favorite favorite;

    public HotSetView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.setOrientation(VERTICAL);
        this.setOnClickListener(this);
        setUp(attrs);
        addIcon();
        addTitle();
    }

    private void setUp(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HotSetView);
        if(typedArray != null){
            iconBack = typedArray.getDrawable(R.styleable.HotSetView_iconBack);
            titleText = typedArray.getString(R.styleable.HotSetView_titleText);
            typedArray.recycle();
        }
    }

    private void addIcon(){
        icon = new ImageView(getContext());
        icon.setScaleType(ImageView.ScaleType.FIT_XY);
        icon.setImageDrawable(iconBack);
        int iconSzie = getResources().getDimensionPixelSize(R.dimen.iconSize);
        addView(icon, new LayoutParams(iconSzie, iconSzie));
    }

    private void addTitle(){
        title = new TextView(getContext());
        title.setText(titleText);
        addView(title, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void setIcon(Drawable iconDrawable){
        iconBack = iconDrawable;
        icon.setImageDrawable(iconDrawable);
    }

    public void setTitle(CharSequence titleStr) {
        title.setText(titleStr);
    }

    public ImageView getIcon(){
        return icon;
    }

    public void setCanAlpha(boolean canAlpha){
        this.canAlpha = canAlpha;
    }

    public void setFavorite(Favorite favorite){
        Validate.notNull(favorite,"favorite");
        this.favorite = favorite;
        try {
            PackageManager packageManager = getContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(favorite.getPckName(),0);
            if(applicationInfo != null){
                icon.setImageDrawable(applicationInfo.loadIcon(packageManager));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(iconBack == null || !canAlpha){
            return super.onTouchEvent(event);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                icon.setAlpha(0.7f);
                break;
            default:
                icon.setAlpha(1.0f);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        Intent intent = AppUtils.getIntent(favorite.getPckName(),
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        LauncherApplication.getInstance().startActivity(intent);
    }
}

