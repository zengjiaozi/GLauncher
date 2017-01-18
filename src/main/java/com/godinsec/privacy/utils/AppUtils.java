package com.godinsec.privacy.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import com.godinsec.privacy.LauncherApplication;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.ItemInfo;

public class AppUtils {
	
	private static final String TAG = AppUtils.class.getSimpleName();

	/**
	 * 获取应用当前版本号
	 * @param mContext 
	 */
	public static int getVersionCode(Context mContext) {
		int versionCode = 1;
		if(mContext == null) {
			return versionCode;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);
			versionCode = packageInfo.versionCode;
			LogUtils.i(TAG, "versionCode = "+versionCode);
		} catch (NameNotFoundException e) {
			LogUtils.e(TAG, "get versioncode error.");
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 根据包名获取预置应用信息
	 *
	 * @param info
	 * @param appInfo
	 * @return
	 */
	public static AppInfo getAppInfoByPck(ApplicationInfo info, AppInfo appInfo) {
		if (info == null) {
			return null;
		}
		Context mContext = LauncherApplication.getInstance();
		String pck = info.packageName;
		if (pck == null && pck.equals(mContext.getPackageName())) {
			return null;
		}
		PackageManager manager = mContext.getPackageManager();
		appInfo.setInstallState(AppInfo.INSTALL_STATE);
		appInfo.setItemType(ItemInfo.Type.APPLICATION);
		appInfo.setTitle(info.loadLabel(manager).toString());
		appInfo.setPckName(pck);
		appInfo.setIcon(info.loadIcon(manager));
		appInfo.setActivity(pck, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return appInfo;
	}

	/**
	 * 根据包名获取应用信息
	 *
	 * @param info
	 * @return
	 */
	public static AppInfo getAppInfoByPck(ApplicationInfo info) {
		return getAppInfoByPck(info,new AppInfo());
	}

	/**
	 * 卸载指定包名的应用
	 *
	 * @param context
	 * @param packageName
	 */
	public static void uninstall(Context context,String packageName) {
		if (checkApplication(context,packageName)) {
			Uri packageURI = Uri.parse("package:" + packageName);
			Intent intent = new Intent(Intent.ACTION_DELETE);
			intent.setData(packageURI);
			context.startActivity(intent);
		}
	}

	/**
	 * 判断该包名的应用是否安装
	 *
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean checkApplication(Context context,String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 获取状态栏的高度
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context){
		int statusHeight;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight){
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 * @param packageName
	 * @param launchFlags
	 * @return
     */
	public static Intent getIntent(String packageName,int launchFlags){
		Validate.notNull(packageName,"packageName");

		Intent intent = LauncherApplication.getInstance().getPackageManager().getLaunchIntentForPackage(packageName);
		if(intent == null){
			LogUtils.e(TAG,"according to pckName["+packageName+"],get intent is null!");
			return null;
		}
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setAction(Intent.ACTION_MAIN);
		intent.setFlags(launchFlags);

		Bundle bundle = new Bundle();
		bundle.putString("FLAG","glauncher");
		intent.putExtras(bundle);
		return intent;
	}

}
