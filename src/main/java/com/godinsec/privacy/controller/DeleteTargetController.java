package com.godinsec.privacy.controller;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.godinsec.privacy.bean.AppEvent;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.data.LauncherModer;
import com.godinsec.privacy.launcher.LauncherActivity;
import com.godinsec.launcher.R;
import com.godinsec.privacy.utils.AppUtils;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.widget.ShowDialog;

public class DeleteTargetController {

	private static final String TAG = "DeleteTargetController";

	private Handler mHandler = new Handler();

	/**
	 *删除区域提示
	 */
	private RelativeLayout deleteContainer;

	private AppDeleteCallback mAppDeleteCallback;

	private int currnentX,currentY;

	private AppInfo deleteApp;

	private Drawable normalDrawable,activedDrawable;

	private Rect deleteFrame;//删除卸载区域所占的rect

	private boolean delete;

	private LauncherActivity mHomeActivity;

	public DeleteTargetController(LauncherActivity homeActivity){
		this.mHomeActivity = homeActivity;
	}

	/**
	 * 设置卸载区域控件
	 */
	public void setDeleteContainer(RelativeLayout deleteContainer){
		this.deleteContainer = deleteContainer;
		initState(mHomeActivity.getResources());
	}

	public void setAppDoCallback(AppDeleteCallback callback){
		this.mAppDeleteCallback = callback;
	}

	/**
	 * 删除卸载完成
	 */
	public void finishDelete(int position){
		if(mAppDeleteCallback != null){
			mAppDeleteCallback.finishDelete(position);
		}
		delete = false;
	}

	/**
	 *初始化状态值
	 * @param mResources
	 */
	private void initState(Resources mResources){
		this.normalDrawable = mResources.getDrawable(R.drawable.delete_app_default_bg);
		this.activedDrawable = mResources.getDrawable(R.drawable.delete_app_active_bg);
	}

	private void initDeleteFrame(){
		if(deleteContainer == null){
			throw new NullPointerException("[initDeleteFrame]deleteTarget is null,You should set first.");
		}
		if(deleteFrame == null){
			deleteFrame = new Rect();
			deleteContainer.getHitRect(deleteFrame);
			LogUtils.i(TAG, "[initDeleteFrame] left = " + deleteFrame.left + ",top = " + deleteFrame.top
					+ ",right = " + deleteFrame.right + ",bottom = " + deleteFrame.bottom);
		}
	}

	public void onTouchEvent(MotionEvent ev){
		currnentX = (int) ev.getRawX();
		currentY = (int) ev.getRawY();
		if(ev.getAction() == MotionEvent.ACTION_UP){
			mHandler.post(mDeleteRunnable);
		}else{
			mHandler.post(mLocatedRunnable);
		}
	}

	/**
	 * 长按初始位置
	 */
	public void onItemLongClick(AppInfo info){
		deleteApp = info;
		LogUtils.d(TAG, "[onItemLongClick] deleteApp_packageName = " + deleteApp.getPckName());
		if(!LauncherModer.getInstance().isDefault(info)){
			showDeleteZone();
		}
	}

	/**
	 *显示删除卸载区域
	 */
	private void showDeleteZone(){
		if(deleteContainer == null){
			throw new NullPointerException("[showDeleteZone]deleteContainer is null,You should set first.");
		}
		deleteContainer.setVisibility(View.VISIBLE);
		CellItemAnimController.createDeleteZoneVisibleAnims(deleteContainer);
	}

	/**
	 *隐藏删除些的区域
	 */
	private void hideDelteZone(){
		if(deleteContainer == null){
			throw new NullPointerException("[hideDelteZone]deleteTarget is null,You should set first.");
		}
		deleteContainer.setVisibility(View.INVISIBLE);
		setDeleteState(normalDrawable);
	}

	/**
	 *设置删除区域的状态显示
	 */
	@SuppressLint("NewApi")
	public void setDeleteState(Drawable drawable){
		//deleteIcon.setBackground(drawable);
		deleteContainer.setBackground(drawable);
	}

	/**
	 *是否是删除操作
	 */
	public boolean isDelete(){
		return delete;
	}

	/**
	 *设置删除状态
	 */
	public void setDeleteFalse(){
		this.delete = false;
	}

	/**
	 * 获取删除的app
	 */
	public AppInfo getDeleteApp(){
		return deleteApp;
	}

	/**
	 * 判断是否滑动到了删除卸载区域
	 */
	private boolean isDeleteZone(int deleteX,int deleteY){
		initDeleteFrame();

		boolean half = Math.abs(deleteFrame.bottom - deleteFrame.top) >= Math.abs(deleteFrame.bottom - deleteY);

		return deleteFrame.contains(deleteX, Math.abs(deleteY)) || half;
	}

	/**
	 * 接收卸载之后的消息,是否需要开启卸载动画
	 * @param unInstallAppEvent
	 * @return boolean
	 */
	public boolean onEventMainThreadDirectDelete(AppEvent unInstallAppEvent){
		LogUtils.d(TAG, "[onEventMainThreadDirectDelete] unInstallAppEvent:remove_packageName = " + unInstallAppEvent.getPckName());
		if(deleteApp == null){
			LogUtils.e(TAG,"[onEventMainThreadDirectDelete] deleteApp == null");
			return true;
		}

		LogUtils.d(TAG, "[onEventMainThreadDirectDelete] deleteApp:remove_packageName = " + deleteApp.getPckName());

		if(unInstallAppEvent.getPckName().equals(deleteApp.getPckName())){
			deleteApp.setInstallState(AppInfo.UNINSTALL_STATE);
			/**
			 * 由于是静默卸载，所以暂时下载完成之后直接开启动画,否则会走onResume(activity)方法
			 *  开启卸载动画
			 */
			mHandler.post(deleteAnimRunnable);
			LogUtils.d(TAG, "[onEventMainThreadDirectDelete] start delete animation!");
			return false;
		}
		return true;
	}

	//销毁
	public void destroy(){
		deleteApp = null;
		normalDrawable = null;
		activedDrawable = null;
	}

	/**
	 * 设置卸载区域状态
	 */
	private Runnable mLocatedRunnable = new Runnable() {

		@Override
		public void run() {
			final boolean located = isDeleteZone(currnentX, currentY);
			if(located){
				delete = true;
				setDeleteState(activedDrawable);
			}else{
				delete = false;
				setDeleteState(normalDrawable);
			}
			mHandler.removeCallbacks(this);
		}
	};

	/**
	 * 用来处理是否滑动到了删除卸载区域
	 */
	private Runnable mDeleteRunnable = new Runnable() {

		@Override
		public void run() {
			final boolean canDelete = isDeleteZone(currnentX, currentY);
			final boolean isDefault = LauncherModer.getInstance().isDefault(deleteApp);
			if(canDelete && !isDefault){
				Resources resources = mHomeActivity.getResources();
				new ShowDialog(mHomeActivity, deleteApp.getTitle().toString(),
						resources.getString(R.string.uninstall_tip),
						resources.getString(R.string.confirm),
						resources.getString(R.string.cancle),
						new ShowDialog.OnExitListener() {
							@Override
							public void onSure() {
								AppUtils.uninstall(mHomeActivity,deleteApp.getPckName());
							}

							@Override
							public void onExit() {

							}
						}).show();
			}
			hideDelteZone();
			mHandler.removeCallbacks(this);
		}
	};

	/**
	 * 用来处理卸载之后的动画效果
	 */
	private Runnable deleteAnimRunnable = new Runnable() {

		@Override
		public void run() {
			if(deleteApp != null && deleteApp.getInstallState() == AppInfo.UNINSTALL_STATE
					&& mAppDeleteCallback != null){
				mAppDeleteCallback.startDelete(deleteApp.getPosition());
				deleteApp = null;
			}
		}
	};
}
