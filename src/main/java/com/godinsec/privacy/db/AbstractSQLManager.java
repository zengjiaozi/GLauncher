package com.godinsec.privacy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.godinsec.privacy.LauncherApplication;
import com.godinsec.privacy.utils.AppUtils;
import com.godinsec.privacy.utils.LogUtils;

/**
 * Created by dandy on 2016/4/7.
 */
public abstract class AbstractSQLManager {
    private static final String TAG = AbstractSQLManager.class.getSimpleName();

    private DatabaseHelper mDataBaseHelper;

    private SQLiteDatabase mSQLitedb;

    protected boolean isOpen = false;

    public AbstractSQLManager() {
        openDataBase(LauncherApplication.getInstance());
    }

    private void openDataBase(Context context){
        if(mDataBaseHelper == null){
            mDataBaseHelper = new DatabaseHelper(context, AppUtils.getVersionCode(context));
        }
        if(mSQLitedb == null){
            mSQLitedb = mDataBaseHelper.getWritableDatabase();
        }
        isOpen = true;
    }

    /**
     * @return
     */
    public final SQLiteDatabase sqliteDB(){
        if(mSQLitedb == null){
            openDataBase(LauncherApplication.getInstance());
        }
        return mSQLitedb;
    }

    /**
     * 关闭数据库
     */
    public void close(){
        try {
            if(mSQLitedb != null && mSQLitedb.isOpen()){
                mSQLitedb.close();
                mSQLitedb = null;
            }
            if(mDataBaseHelper != null){
                mDataBaseHelper.close();
                mDataBaseHelper = null;
            }
            isOpen = false;
        } catch (Exception e) {
            LogUtils.e(TAG, "sqlite close error.");
            e.printStackTrace();
        }
    }

    /**
     * 表查询条件
     */
    public String whereColumSelected(String colum, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(colum).append("='").append(value).append("'");
        LogUtils.i(TAG, "----------->whereColumSelected:" + sb.toString());
        return sb.toString();
    }

    /**
     * 表查询条件
     */
    public String createSelections(String... params) {
        StringBuilder sb = new StringBuilder();
        int length = params.length;
        for (int i = 0; i < length; i++) {
            sb.append(params[i]).append("=?");
            if (i != length - 1) {
                sb.append(" and ");
            }
        }
        LogUtils.i(TAG, "------>selections:" + sb.toString());
        return sb.toString();
    }
}
