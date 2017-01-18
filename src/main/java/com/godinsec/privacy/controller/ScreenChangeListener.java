package com.godinsec.privacy.controller;

import com.godinsec.privacy.widget.PageIndicator;
import com.godinsec.privacy.widget.WorkSpace;

public class ScreenChangeListener implements WorkSpace.OnPageChangeListener{

	private PageIndicator mPageIndicator;
	
	private boolean isScrolling = false;

	public ScreenChangeListener(PageIndicator indicator) {
		this.mPageIndicator = indicator;
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		if(arg0 != 0){
			isScrolling = true;
		}else{
			isScrolling = false;
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		float percent = positionOffset;
		if(isScrolling && percent > 0.1f && percent < 0.9f){
			mPageIndicator.setScrollProgress(position,percent);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		mPageIndicator.setCircleActivedIndex(arg0);
	}
}
