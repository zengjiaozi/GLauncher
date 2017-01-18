package com.godinsec.privacy.bean;

/**
 * Created by Seeker on 2016/9/9.
 */

public class Favorite {

    private String pckName;

    private int position;

    private boolean isHotSetView;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPckName() {
        return pckName;
    }

    public void setPckName(String pckName) {
        this.pckName = pckName;
    }

    public boolean isHotSetView() {
        return isHotSetView;
    }

    public void setHotSetView(boolean hotSetView) {
        isHotSetView = hotSetView;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Favorite:pckName = [").append(pckName).append("]")
                .append("position = [").append(position).append("]")
                .append("isHotSetView = [").append(isHotSetView).append("]");


        return sb.toString();
    }
}
