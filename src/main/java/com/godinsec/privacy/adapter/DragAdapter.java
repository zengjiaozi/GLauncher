package com.godinsec.privacy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.godinsec.launcher.R;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.ItemInfo;
import com.godinsec.privacy.db.PrivacyLauncherSQLManger;
import com.godinsec.privacy.utils.Configure;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.utils.RxJavaHelper;
import com.godinsec.privacy.widget.RoundImageView;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter{

    private static final String TAG = "DragAdapter";

    private List<AppInfo> list;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;
    private int lastDoState = DOSTATE_NOTHING;//上次操作是数据交换的操作

    private RxJavaHelper rxJavaHelper = RxJavaHelper.getInstance();

    private int appPosition = 0;

    public DragAdapter(Context context, List<AppInfo> list){
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 由于复用convertView导致某些item消失了，所以这里不复用item，
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.grid_item, null);
        RoundImageView icon = (RoundImageView) convertView.findViewById(R.id.item_image);
        TextView title = (TextView) convertView.findViewById(R.id.item_text);
        convertView.setLayoutParams(new GridView.LayoutParams(Configure.itemWidth,Configure.itemHeight));

        ItemInfo info = list.get(position);
        icon.setImageDrawable(info.getIcon());
        title.setText(info.getTitle());
        if(position == mHidePosition){
            if(lastDoState == DOSTATE_DELETE){
                LogUtils.d(TAG,"position = "+position+",mHidePosition = "+mHidePosition);
                convertView.setVisibility(View.INVISIBLE);
            }
        }
        LogUtils.e(TAG,"title = "+info.getTitle());
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        return convertView;
    }


    @Override
    public boolean reorderItems(int oldPosition, int newPosition,boolean delete) {
        if(oldPosition >= list.size() || newPosition >= list.size()){
            return false;
        }

        /**
         * 最新的位置，如果为空或者占位，则不予交换
         */
        if(list.get(newPosition).getItemType().equals(ItemInfo.Type.EMPTY) && !delete){
            return false;
        }

        AppInfo info = list.get(oldPosition);
        if(oldPosition < newPosition){
            for(int i = oldPosition; i < newPosition; i++){
                Collections.swap(list, i, i + 1);
            }
        }else if(oldPosition > newPosition){
            for(int i = oldPosition; i > newPosition; i--){
                Collections.swap(list, i, i - 1);
            }
        }
        list.set(newPosition, info);

        appPosition = 0;

        rxJavaHelper.setThread(Observable.from(list))
                    .map(new Func1<AppInfo,AppInfo>() {
                        @Override
                        public AppInfo call(AppInfo appInfo) {
                            appInfo.setPosition(appPosition);
                            PrivacyLauncherSQLManger.getInstance().updateApplicationInfo(appInfo);
                            appPosition ++;
                            return appInfo;
                        }
                    })
                    .subscribe(new Action1<AppInfo>() {
                        @Override
                        public void call(AppInfo appInfo) {
                            StringBuilder sb = new StringBuilder(appInfo.toString());
                            sb.append("update postion:newPosition = ").append(appInfo.getPosition());
                            LogUtils.d(TAG,sb.toString());
                        }
                    });
        return true;
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    @Override
    public void setLastDoState(int state) {
        this.lastDoState = state;
    }
}

