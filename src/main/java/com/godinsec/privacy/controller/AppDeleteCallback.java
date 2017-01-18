package com.godinsec.privacy.controller;

public interface AppDeleteCallback {

	//开始执行删除卸载动画
	void startDelete(int deletePosition);
	//删除卸载应用
	void finishDelete(int deletePosition);
}
