package com.godinsec.privacy.bean;

/**
 * Created by Seeker on 2016/9/13.
 */

public class AppEvent {

    public static final int ACTION_ADD = 0x001;

    public static final int ACTION_REMOVE = 0x002;

    public static final int ACTION_REFRESH = 0x003;

    private String pckName;

    private int screen;

    private int action;

    private int refreshIndex;

    public String getPckName() {
        return pckName;
    }

    public void setPckName(String pckName) {
        this.pckName = pckName;
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getRefreshIndex() {
        return refreshIndex;
    }

    public void setRefreshIndex(int refreshIndex) {
        this.refreshIndex = refreshIndex;
    }
}
