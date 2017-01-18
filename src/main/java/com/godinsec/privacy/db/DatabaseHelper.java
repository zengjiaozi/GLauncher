package com.godinsec.privacy.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

/**
 * Created by ubuntu64 on 16-4-5.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String NAME = "glauncher.db";

    protected  static final String TABLES_NAME_ITEM_INFO = "item_info";
    protected  static final String TABLES_NAME_UNREGIST_USER = "unregist_user";//收集未注册用户信息
    protected  static final String TABLES_NAME_CONTACTS="contacts_info";
    protected  static final String TABLES_NAME_CONTACTID_MAP="contactsid_map" ;
    protected  static final String TABLES_NAME_GESTURE_STORE="gesture";//存储手势密码信息的表

    protected  static final String APPS_STATUS = "apps_status";     //框架层的应用安装状态
    protected  static final String TABLES_VIRTUAL_CALLS = "calls_v"; //用于存储虚拟手机拦截到播出的通话记录
    private static DatabaseHelper sSingleton = null;

    public DatabaseHelper(Context context, int version) {
        super(context, NAME, null, version);
    }

    public static synchronized DatabaseHelper getInstance(Context context, int version) {
        if (sSingleton == null) {
            sSingleton = new DatabaseHelper(context, version);
        }
        return sSingleton;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        ContentResolver.requestSync(null, "glauncher", new Bundle());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    protected void createTables(SQLiteDatabase db){
        createTableForItemInfo(db);
        createTableForUnRegistUser(db);
        createTableForCotactsInfo(db);
        createTableForContactIdMap(db);
        createVirtualSmsTable(db);
        createVirtualCallTable(db);
        createGestureCodeTable(db);
    }

    /**非注册用户信息收集
     * IS_REGIST 是否注册过 默认0 注册1
     * IS_REQUEST_SERVER 是否请求过服务器  默认0 请求1
     * */
    private void createTableForUnRegistUser(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS "
                + TABLES_NAME_UNREGIST_USER
                + " ( "
                + UnRegistUser.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UnRegistUser.PHONE_IMEI + " TEXT NOT NULL, "
                + UnRegistUser.IS_REGIST + " INTEGER default 0, "
                + UnRegistUser.IS_REQUEST_SERVER + " INTEGER default 0"
                + ")";

        db.execSQL(sql);
        ContentValues values = new ContentValues();
        //values.put(DatabaseHelper.UnRegistUser.PHONE_IMEI, Global.getDevicNO());
        values.put(UnRegistUser.PHONE_IMEI, "0");
        values.put(UnRegistUser.IS_REQUEST_SERVER, "0");
        values.put(UnRegistUser.IS_REGIST, "0");
        db.insert(DatabaseHelper.TABLES_NAME_UNREGIST_USER, null, values);
    }

    protected void createTableForItemInfo(SQLiteDatabase db){
        String sql = "CREATE TABLE IF NOT EXISTS "
                + TABLES_NAME_ITEM_INFO
                + " ( "
                + ItemInfoColumn.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemInfoColumn.SCREEN + " INTEGER, "
                + ItemInfoColumn.POSITION + " INTEGER, "
                + ItemInfoColumn.PKG_NAME + " TEXT, "
                + ItemInfoColumn.ITEM_TYPE + " TEXT NOT NULL"
                + ")";

        db.execSQL(sql);
    }
    //创建导入联系人信息表
    protected void createTableForCotactsInfo(SQLiteDatabase db){
        String sql="CREATE TABLE IF NOT EXISTS "
                +TABLES_NAME_CONTACTS
                +"("
                +ContactInfo.ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ContactInfo.CONTACT_ID+" LONG, "
                +ContactInfo.DISPLAY_NAME+" TEXT, "
                +ContactInfo.PHONE_NUMBER+" TEXT, "
                +ContactInfo.SORT_KEY+" TEXT"
                +")";
        db.execSQL(sql);
    }

    //创建联系人ID的Map表  isSecretContact
    protected void createTableForContactIdMap(SQLiteDatabase db){
        String sql="CREATE TABLE IF NOT EXISTS "
                +TABLES_NAME_CONTACTID_MAP
                +"("
                +ContactIdMap.contact3Id+" LONG default -1, "
                +ContactIdMap.ownContactId+" LONG default -1, "
                +ContactIdMap.isFromSysContact+" LONG default -1, "
                +ContactIdMap.isSecretContact+" LONG default -1"
                +")";
        db.execSQL(sql);
    }

    //框架层的应用状态数据
    protected void createVirtualSmsTable(SQLiteDatabase db){
        String sql="CREATE TABLE IF NOT EXISTS "
                + APPS_STATUS
                + "("
                + AppStatus.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AppStatus.PKGNAME + " TEXT, "
                + AppStatus.STATUS + " INTEGER"
                + ")";
        db.execSQL(sql);
    }

    //创建虚拟手机拦截到的通话记录表
    protected void createVirtualCallTable(SQLiteDatabase db){
        String sql="CREATE TABLE IF NOT EXISTS "
                + TABLES_VIRTUAL_CALLS
                + "("
                + VirtualColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VirtualColumns.NUMBER + " TEXT, "
                + VirtualColumns.DATE + " LONG"
                + ")";
        db.execSQL(sql);
    }

    protected void createGestureCodeTable(SQLiteDatabase db){
        String sql="CREATE TABLE IF NOT EXISTS "
                + TABLES_NAME_GESTURE_STORE
                + "("
                + GestureStore.PWD+" TEXT, "
                + GestureStore.FAKE_PWD+" TEXT, "
                + GestureStore.IS_FOREGROUND+" TEXT, "
                + GestureStore.IS_LOCKON+" TEXT, "
                + GestureStore.FAKE_IS_LOCKON+" TEXT, "
                + GestureStore.PWD_SETTED+" TEXT, "
                + GestureStore.FAKE_PWD_SETTED+" TEXT, "
                + GestureStore.SCREEN_OFF+" TEXT, "
                + GestureStore.UNLOCKED+" TEXT"
                + ")";
        db.execSQL(sql);
    }

    //存储三列数据，ID 号码 日期，便于查询和匹配拦截的短信或电话
    protected static class VirtualColumns{
        public static final String ID="id";
        public static final String NUMBER="number";
        public static final String DATE="date";
        public static final String BODY="body";
    }

    //存储安装应用的包名、安装状态
    protected static class AppStatus{
        public static final String ID="id";
        public static final String PKGNAME="pkgname";
        public static final String STATUS="status";
    }

    protected static class ItemInfoColumn{
        public static final String ID = "id";
        public static final String SCREEN = "screen";
        public static final String POSITION = "position";
        public static final String PKG_NAME = "pkg_name";
        public static final String ITEM_TYPE = "item_type";
    }
    /**未注册用户信息的收集*/
    protected static class UnRegistUser{
        public static final String ID = "id";
        public static final String PHONE_IMEI = "phone_imei";
        public static final String IS_REGIST = "is_regist";
        public static final String IS_REQUEST_SERVER = "is_request_server";
    }
    /*导入联系人的信息表*/
    protected static class ContactInfo{
        public static final String ID="_id";
        public static final String CONTACT_ID="contact_id";
        public static final String DISPLAY_NAME="display_name";
        public static final String PHONE_NUMBER="phone_number";
        public static final String SORT_KEY="sort_key";
    }
    /*自己数据库和contacts3.db数据库联系人ID的一个map表*/
    protected static class ContactIdMap{
        public static final String contact3Id="contact3_id";//raw_contacid glauncher
        public static final String ownContactId="own_contact_id";//系统手机的contactid
        public static final String isSecretContact="secret_contact";//
        public static final String isFromSysContact="is_from_sys_contact";//
    }
    /*手势密码信息表*/
    protected static class GestureStore{
        public static final String PWD="pwd";
        public static final String FAKE_PWD="fake_pwd";
        public static final String IS_FOREGROUND="is_foreground";
        public static final String IS_LOCKON="is_lockon";
        public static final String FAKE_IS_LOCKON="fake_is_lockon";
        public static final String PWD_SETTED="pwd_setted";
        public static final String FAKE_PWD_SETTED="fake_pwd_setted";
        public static final String SCREEN_OFF="screenOff";
        public static final String UNLOCKED="unlocked";

    }
}
















