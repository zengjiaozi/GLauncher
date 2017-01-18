package com.godinsec.privacy.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.widget.CellLayout;
import java.util.LinkedList;
import java.util.List;

public class CellItemAnimController{

	private static final String TAG = "CellItemAnimController";

	private static final long ANIM_DURATION = 300;

	private CellLayout mCellLayout;

	private int deletePosition;//拖动的最后位置，删除卸载默认拖动到最后一个位置

	public CellItemAnimController(CellLayout cellLayout) {
		this.mCellLayout = cellLayout;
	}

	/**
	 * 创建移动动画
	 * @param view
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	/**
	 * 创建渐变动画
	 * @param view
	 */
	private AnimatorSet createShowAnimations(View view){
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f,1f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f,1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f,1f);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(alpha,scaleX,scaleY);
		return set;
	}

	/**
	 * 创建卸载应用时，被卸载的应用晃动效果
	 */
	public static void createShakeAnimations(View view){
		ObjectAnimator shake = ObjectAnimator.ofFloat(view, "rotation", 0f,180f);
		shake.setDuration(200);
		shake.setRepeatCount(10000);
		shake.start();
	}

	/**
	 * 创建删除区域显示动画
	 * @param view
	 */
	public static void createDeleteZoneVisibleAnims(View view){
		view.measure(0,0);
		TranslateAnimation translateAnimation = new TranslateAnimation(0,0,-view.getMeasuredHeight(),0);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setDuration(ANIM_DURATION);
		animationSet.setFillAfter(false);
		animationSet.setRepeatCount(1);
		animationSet.addAnimation(translateAnimation);
		view.startAnimation(animationSet);

	}

	/**
	 * item的交换动画效果
	 * @param oldPosition
	 * @param newPosition
	 * @param delete
	 */
	public void animateReorder(final int oldPosition, final int newPosition,boolean delete) {
		LogUtils.v(TAG,"[animateReorder] start!");
		deletePosition = newPosition;
		final int mNumColumns = mCellLayout.getNumColumns();
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = mCellLayout.getChildAt(pos - mCellLayout.getFirstVisiblePosition());
				if(view == null){
					continue;
				}
				if ((pos + 1) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view,
							- view.getWidth() * (mNumColumns - 1), 0,view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view,view.getWidth(), 0, 0, 0));
				}
			}
		} else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = mCellLayout.getChildAt(pos - mCellLayout.getFirstVisiblePosition());
				if(view == null){
					continue;
				}
				if ((pos + mNumColumns) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view,
							view.getWidth() * (mNumColumns - 1), 0,-view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view,-view.getWidth(), 0, 0, 0));
				}
			}
		}

		if(!delete){
			View view = mCellLayout.getChildAt(newPosition - mCellLayout.getFirstVisiblePosition());
			resultList.add(createShowAnimations(view));
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(ANIM_DURATION);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation) {
				LogUtils.v(TAG,"[animateReorder] onAnimationEnd!");
				final DeleteTargetController controller = mCellLayout.getDeleteTargetController();
				if(controller == null){
					return;
				}
				LogUtils.v(TAG,"[animateReorder] controller.isDelete = "+controller.isDelete());
				if(controller.isDelete()){
					controller.finishDelete(deletePosition);
				}
			}
		});
		resultSet.start();
	}
}
