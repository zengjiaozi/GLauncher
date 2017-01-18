package com.godinsec.privacy.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Seeker on 2016/9/13.
 */

public class Request {

    private Head head;

    private Body body;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public static class Head implements Parcelable{

        private String os_type;

        private String app_type;

        public Head(){

        }

        public Head(String os_type,String app_type){
            this.os_type = os_type;
            this.app_type = app_type;
        }

        public String getApp_type() {
            return app_type;
        }

        public void setApp_type(String app_type) {
            this.app_type = app_type;
        }

        public String getOs_type() {
            return os_type;
        }

        public void setOs_type(String os_type) {
            this.os_type = os_type;
        }

        public static final Creator<Head> CREATOR = new Creator<Head>() {
            @Override
            public Head createFromParcel(Parcel in) {
                Head head = new Head();
                head.setOs_type(in.readString());
                head.setApp_type(in.readString());
                return head;
            }

            @Override
            public Head[] newArray(int size) {
                return new Head[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(os_type);
            parcel.writeString(app_type);
        }
    }

    public static class Body implements Parcelable{

        private String phoneimei;

        private String package_name;

        private String app_name;

        public Body(){

        }

        public Body(String phoneimei,String package_name,String app_name){
            this.phoneimei = phoneimei;
            this.package_name = package_name;
            this.app_name = app_name;
        }

        public String getPhoneimei() {
            return phoneimei;
        }

        public void setPhoneimei(String phoneimei) {
            this.phoneimei = phoneimei;
        }

        public String getPackage_name() {
            return package_name;
        }

        public void setPackage_name(String package_name) {
            this.package_name = package_name;
        }

        public String getApp_name() {
            return app_name;
        }

        public void setApp_name(String app_name) {
            this.app_name = app_name;
        }

        public static final Creator<Body> CREATOR = new Creator<Body>() {
            @Override
            public Body createFromParcel(Parcel in) {
                Body body = new Body();
                body.phoneimei = in.readString();
                body.package_name = in.readString();
                body.app_name = in.readString();
                return body;
            }

            @Override
            public Body[] newArray(int size) {
                return new Body[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(phoneimei);
            parcel.writeString(package_name);
            parcel.writeString(app_name);
        }
    }

}
