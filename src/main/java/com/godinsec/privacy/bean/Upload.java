package com.godinsec.privacy.bean;

/**
 * Created by Seeker on 2016/9/13.
 */

public class Upload {

    private Request  Request;

    public Upload(Request  request){
        this.Request = request;
    }

    public com.godinsec.privacy.bean.Request getRequest() {
        return this.Request;
    }

    public void setRequest(com.godinsec.privacy.bean.Request request) {
        this.Request = request;
    }
}
