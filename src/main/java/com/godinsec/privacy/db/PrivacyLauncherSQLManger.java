package com.godinsec.privacy.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.ItemInfo;
import com.godinsec.privacy.utils.LogUtils;

import java.util.HashMap;

/**
 * Created by dandy on 2016/4/7.
 */
public final class PrivacyLauncherSQLManger extends AbstractSQLManager {

    private static final String TAG = "PrivacyLauncherSQLManger";

    private static final class SingleFactory {
        private static final PrivacyLauncherSQLManger instance = new PrivacyLauncherSQLManger();
    }

    public static PrivacyLauncherSQLManger getInstance() {
        return SingleFactory.instance;
    }

    /**
     * 更新位置信息，如果没有此包则插入信息，如果有则更新位置信息
     *
     * @param appInfo
     */
    public synchronized boolean updateApplicationInfo(AppInfo appInfo) {
        if (appInfo.getItemType().equals(ItemInfo.Type.EMPTY)) {
            return false;
        }
        Cursor cursor = sqliteDB().query(DatabaseHelper.TABLES_NAME_ITEM_INFO, null,
                DatabaseHelper.ItemInfoColumn.PKG_NAME + "=?", new String[]{appInfo.getPckName()}, null, null, null);
        long result = -1;

        try {
            if (cursor != null && cursor.getCount() > 0) {   //若存在包则更新操作
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.ItemInfoColumn.SCREEN, appInfo.getScreen());
                values.put(DatabaseHelper.ItemInfoColumn.POSITION, appInfo.getPosition());
                result = sqliteDB().update(DatabaseHelper.TABLES_NAME_ITEM_INFO, values,
                        whereColumSelected(DatabaseHelper.ItemInfoColumn.PKG_NAME, appInfo.getPckName()), null);

            } else {                                         //若不存在则插入
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.ItemInfoColumn.SCREEN, appInfo.getScreen());
                values.put(DatabaseHelper.ItemInfoColumn.POSITION, appInfo.getPosition());
                values.put(DatabaseHelper.ItemInfoColumn.PKG_NAME, appInfo.getPckName());
                values.put(DatabaseHelper.ItemInfoColumn.ITEM_TYPE, appInfo.getItemType());
                result = sqliteDB().insert(DatabaseHelper.TABLES_NAME_ITEM_INFO, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        LogUtils.i(TAG, "[updateApplicationInfo]update " + appInfo.getTitle() + " finish!");

        return result > 0;
    }

    /**
     * 查询所有包的位置信息
     *
     * @return
     */
    public HashMap<String,AppInfo> queryApplicationInfoList() {
        HashMap<String,AppInfo> appInfoList = new HashMap<>();
        Cursor cursor = sqliteDB().query(DatabaseHelper.TABLES_NAME_ITEM_INFO, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String pck = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.PKG_NAME));
                AppInfo appInfo = new AppInfo();
                appInfo.setScreen(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.SCREEN)));
                appInfo.setPosition(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.POSITION)));
                appInfo.setPckName(pck);
                appInfo.setItemType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.ITEM_TYPE)));
                appInfoList.put(pck,appInfo);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return appInfoList;
    }

    /**
     * 通过包名查询位置
     *
     * @param pkgName
     * @return
     */
     public synchronized AppInfo queryApplicationInfo(String pkgName) {
        Cursor cursor = sqliteDB().query(DatabaseHelper.TABLES_NAME_ITEM_INFO, null,
                DatabaseHelper.ItemInfoColumn.PKG_NAME + "=?", new String[]{pkgName}, null, null, null);
        AppInfo appInfo = null;
        if (cursor != null && cursor.getCount() > 0) {
            appInfo = new AppInfo();
            if (cursor.moveToFirst()) {
                appInfo.setScreen(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.SCREEN)));
                appInfo.setPosition(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.POSITION)));
                appInfo.setPckName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.PKG_NAME)));
                appInfo.setItemType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ItemInfoColumn.ITEM_TYPE)));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return appInfo;
    }

    /**
     * 通过包名删除位置信息
     *
     * @param pkgName
     */
    public void deleteApplicationInfo(String pkgName) {
        sqliteDB().delete(DatabaseHelper.TABLES_NAME_ITEM_INFO, DatabaseHelper.ItemInfoColumn.PKG_NAME + "=?",
                new String[]{pkgName});
    }


}