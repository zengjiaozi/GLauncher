package com.godinsec.privacy.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.godinsec.launcher.R;

public class ShowDialog extends Dialog implements View.OnClickListener {

	private static final String TAG = "ShowDialog";
	
	private OnExitListener mLis;
	private PackageManager pm;
	private ApplicationInfo appInfo;

	private String title;
	private String content;
	private String btn_yes_msg, btn_no_msg;

	private TextView appTitle, message;
	private TextView tv_ok, tv_cancel;
	//是否显示
	//private boolean isShow;

	public ShowDialog(Context context, String title, String content,
					  String btn_yes_msg, String btn_no_msg,
					  OnExitListener lis) {
		super(context);
		this.title = title;
		this.content = content;
		this.btn_yes_msg = btn_yes_msg;
		this.btn_no_msg = btn_no_msg;
		this.mLis = lis;
	}

	public ShowDialog(Context c, String title) {
		super(c);
		this.title = title;
		pm = c.getPackageManager();
	}

	public Drawable getIcon() {
		try {
			appInfo = pm.getApplicationInfo("system",
					PackageManager.GET_META_DATA);
			return pm.getApplicationIcon(appInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.showdialog_layout);
		this.findViewById(R.id.show_dialog_ok).setOnClickListener(this);
		this.findViewById(R.id.show_dialog_cancel).setOnClickListener(this);
		appTitle = (TextView) this.findViewById(R.id.show_dialog_title);
		appTitle.setText(title);
		message = (TextView) this.findViewById(R.id.show_dialog_message);
		message.setText(content);
		tv_ok = (TextView) findViewById(R.id.show_dialog_ok);
		tv_cancel = (TextView) findViewById(R.id.show_dialog_cancel);
		tv_ok.setText(btn_yes_msg);
		tv_cancel.setText(btn_no_msg);
		Window window = this.getWindow();
		WindowManager.LayoutParams params = this.getWindow().getAttributes();
		window.setAttributes(params);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.show_dialog_ok:
				this.dismiss();
				mLis.onSure();
				break;
			case R.id.show_dialog_cancel:
				this.dismiss();
				mLis.onExit();
				break;
		}
	}

	public interface OnExitListener {
		public void onSure();

		public void onExit();
	}

	public TextView getAppTitle() {
		return appTitle;
	}

	public void setAppTitle(TextView appTitle) {
		this.appTitle = appTitle;
	}
}
