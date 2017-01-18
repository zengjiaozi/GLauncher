package com.godinsec.privacy.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.godinsec.privacy.widget.PagerAdapter;

import java.util.List;

public class WorkSpaceAdapter extends PagerAdapter {

	private static final String TAG = "WorkSpaceAdapter";

	private List<View> mCellLayouts;

	public WorkSpaceAdapter(List<View> views) {
		this.mCellLayouts = views;
	}
	
	@Override
	public int getCount() {
		return mCellLayouts.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mCellLayouts.get(position), 0);
		return mCellLayouts.get(position);
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
}
