package com.godinsec.privacy.bean;

import android.graphics.drawable.Drawable;

public class ItemInfo {

	private static final int NO_ID = -1;

	/**
	 * 在第几屏 
	 */
	private int screen;

	/**
	 * 在第几行 
	 */
	private int landX;

	/**
	 * 在第几列 
	 */
	private int portY;

	/**
	 * 贼celllayout位置 
	 */
	private int position;

	/**
	 * 图标 
	 */
	private Drawable icon;

	/**
	 * 标题 
	 */
	private CharSequence title;

	private int defaultIcon;

	private String itemType;

	private long id = NO_ID;

	public int getScreen() {
		return screen;
	}

	public void setScreen(int screen) {
		this.screen = screen;
	}

	public int getLandX() {
		return landX;
	}

	public void setLandX(int landX) {
		this.landX = landX;
	}

	public int getPortY() {
		return portY;
	}

	public void setPortY(int portY) {
		this.portY = portY;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public CharSequence getTitle() {
		return title;
	}

	public void setTitle(CharSequence title) {
		this.title = title;
	}

	public int getDefaultIcon() {
		return defaultIcon;
	}

	public void setDefaultIcon(int defaultIcon) {
		this.defaultIcon = defaultIcon;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}



	public  static final class Type{
		/**
		 * 应用
		 */
		public static final String  APPLICATION = "application";

		/**
		 * 文件夹
		 */
		public static final String FILE = "folder";

		/**
		 * 快捷方式
		 */
		public static final String SHORTCUT = "shortcut";

		/**
		 * 占位
		 */
		public static final String SOLID = "solid";

		/**
		 * 空值
		 */
		public static final String EMPTY = "empty";
	}
}
