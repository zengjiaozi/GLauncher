package com.godinsec.privacy.adapter;

public interface DragGridBaseAdapter {

	public static final int DOSTATE_NOTHING = -1;

	public static final int DOSTATE_SWAP = 0;

	public static final int DOSTATE_DELETE = 1;

	/**
	 * 重新排列数据
	 * @param oldPosition
	 * @param newPosition
	 * @param delete 是否是因为删除而坐的交换
	 * @return boolean
	 */
	public boolean reorderItems(int oldPosition, int newPosition, boolean delete);


	/**
	 * 设置某个item隐藏
	 * @param hidePosition
	 */
	public void setHideItem(int hidePosition);

	/**
	 * 设置上次操作的状态：0，数据交换，1删除卸载 
	 */
	public void setLastDoState(int state);

}
