package com.godinsec.privacy.utils;

import android.util.Log;

public class LogUtils {


    /**
     * 如果true打印log,false不打印log
     */


//
    private static boolean isDebug = true;

    private static int level=1;

    private static boolean VERBOSE_LEVEL=true;
    private static boolean DEBUG_LEVEL=true;
    private static boolean INFO_LEVEL=true;
    private static boolean WARN_LEVEL=true;
    private static boolean ERROR_LEVEL=true;
/*    private static boolean VERBOSE_LEVEL=(2>=level);
    private static boolean DEBUG_LEVEL=(3>=level);
    private static boolean INFO_LEVEL=(4>=level);
    private static boolean WARN_LEVEL=(5>=level);
    private static boolean ERROR_LEVEL=(6>=level);*/

    /**
     * 设置是否debug
     * @param isDebug
     */
    public static void setIsDebug(boolean isDebug) {
        LogUtils.isDebug = isDebug;
    }

    /**
     * 设置严重程度
     * @param level_n
     */
    public static void setLevel(int level_n) {
        LogUtils.level = level_n;
        VERBOSE_LEVEL=(2>=level);
        DEBUG_LEVEL=(3>=level);
        INFO_LEVEL=(4>=level);
        WARN_LEVEL=(5>=level);
        ERROR_LEVEL=(6>=level);
    }

    private static final String HEAD_TAG = "PrivacyLauncher_";

    public static void v(Class<?> clazz, String logInfo) {
        v(clazz.getSimpleName(), logInfo);
    }

    public static void v(String tag, String logInfo) {
        printLogInfo(tag, logInfo, Log.VERBOSE);
    }

    public static void i(Class<?> clazz, String logInfo) {
        i(clazz.getSimpleName(), logInfo);
    }

    public static void i(String tag, String logInfo) {
        printLogInfo(tag, logInfo, Log.INFO);
    }

    public static void d(Class<?> clazz, String logInfo) {
        d(clazz.getSimpleName(), logInfo);
    }

    public static void d(String tag, String logInfo) {
        printLogInfo(tag, logInfo, Log.DEBUG);
    }

    public static void w(Class<?> clazz, String logInfo) {
        w(clazz.getSimpleName(), logInfo);
    }

    public static void w(String tag, String logInfo) {
        printLogInfo(tag, logInfo, Log.WARN);
    }

    public static void e(Class<?> clazz, String logInfo) {
        e(clazz.getSimpleName(), logInfo);
    }

    public static void e(String tag, String logInfo) {
        printLogInfo(tag, logInfo, Log.ERROR);
    }

    public static void printLogInfo(String tag, String logInfo, int style) {
        if (!isDebug)
            return;
        switch (style) {
            case Log.VERBOSE:
                if (VERBOSE_LEVEL){
                    Log.v(HEAD_TAG + tag, logInfo);
                }
                break;
            case Log.INFO:
                if (INFO_LEVEL){
                    Log.i(HEAD_TAG + tag, logInfo);
                }
                break;
            case Log.DEBUG:
                if (DEBUG_LEVEL){
                    Log.d(HEAD_TAG + tag, logInfo);
                }
                break;
            case Log.ERROR:
                if (ERROR_LEVEL){
                    Log.e(HEAD_TAG + tag, logInfo);
                }
                break;
            case Log.WARN:
                if (WARN_LEVEL){
                    Log.w(HEAD_TAG + tag, logInfo);
                }
                break;
        }
    }
}
