package com.godinsec.privacy.utils;

/**
 * Created by Seeker on 2016/9/9.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by dandy on 2016/5/19.
 */

public class SharedPreferencesUtils {

    private static SharedPreferencesUtils instance;

    private static final String DEFAULT_NAME = "privacyLaucnher.xml";

    private String name = DEFAULT_NAME;

    private Context mContext;

    public static SharedPreferencesUtils getInstance(){
        if(instance == null){
            synchronized (SharedPreferencesUtils.class){
                if(instance == null){
                    instance = new SharedPreferencesUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化上下文参数以及文件名
     * @param context，上下文
     * @param name，文件名
     */
    public void init(Context context,String name){
        this.mContext = context.getApplicationContext();
        this.name = name;
    }

    /**
     * 保存数据,泛型方法
     * @param key，键值
     * @param value，数据
     * @param <V>
     */
    public <V> void setValue(@NonNull String key,V value){
        SharedPreferences sp = mContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(value instanceof String){
            editor.putString(key,(String)value);
        }else if(value instanceof Integer){
            editor.putInt(key,(Integer)value);
        }else if(value instanceof Long){
            editor.putLong(key,(Long)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(key,(Boolean)value);
        }else if(value instanceof Float){
            editor.putFloat(key,(Float)value);
        }
        editor.commit();
    }

    /**
     * 读取数据,泛型方法
     * @param key，键值
     * @param defaultValue，默认值
     * @param <V>
     * @return
     */

    public <V> V getValue(@NonNull String key,V defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(name,Context.MODE_PRIVATE);
        Object value = defaultValue;
        if(defaultValue instanceof String){
            value = sp.getString(key,(String)defaultValue);
        }else if(defaultValue instanceof Integer){
            value = sp.getInt(key,(Integer) defaultValue);
        }else if(defaultValue instanceof Long){
            value = sp.getLong(key,(Long) defaultValue);
        }else if(defaultValue instanceof Boolean){
            value = sp.getBoolean(key, (Boolean) defaultValue);
        }else if(defaultValue instanceof Float){
            value = sp.getFloat(key, (Float) defaultValue);
        }
        return (V)value;
    }

    /**
     * 清除数据
     */
    public void clearData(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    public static final class Keys{
        public static final String HAS_LAUNCHER = "has_launcher";
    }


}
