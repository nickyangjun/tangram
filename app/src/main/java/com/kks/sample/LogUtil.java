package com.kks.sample;

import android.util.Log;

/**
 * 打印日志工具类
 * app内所有日志打印需求统一用本类
 * 打包时关闭
 * Created by Seaky on 2017/3/10.
 */

public class LogUtil {
    private static final String TAG = "XLLog";
    private static String className;//类名
    private static String methodName;//方法名
    private static int lineNumber;//行数
    private static boolean isLoggable = Log.isLoggable(TAG, Log.DEBUG);


    public static void setLoggable() {
        isLoggable = true;
    }

    private LogUtil() {
    }

    /**
     * 是否可以输出日志，总开关
     *
     * @return
     */
    private static boolean isLoggable() {
        return BuildConfig.DEBUG || isLoggable;
    }

    /**
     * 是否可以输出log到控制台
     *
     * @return
     */
    private static boolean isLogToConsole() {
        return BuildConfig.DEBUG || isLoggable;
    }


    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[2].getFileName();
        methodName = sElements[2].getMethodName();
        lineNumber = sElements[2].getLineNumber();
    }

    public static String createLog(String log) {
        getMethodNames(new Throwable().getStackTrace());
        String str = methodName + "(" + className + ":" + lineNumber + ") --> " + log;
        if (Thread.currentThread().getId() == 1) {
            Log.e("ERROR", "主线程中调用logUtils, 请设置TAG, " + methodName + "(" + className + ":" + lineNumber + ")");
        }
        return str;
    }

    public static String e(final String message) {
        if (!isLoggable()) {
            return "";
        }

        String info = createLog(message);
        String tag = className;
        if (isLogToConsole()) {
            Log.e(tag, info);
        }
        return info;
    }


    public static String e(String tag, final String message) {
        if (!isLoggable()) {
            return message;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.e(tag, info);
        }
        return info;
    }

    public static String e(final String tag, final Throwable exception) {
        String info = Log.getStackTraceString(exception);
        if (!isLoggable()) {
            return info;
        }
        if (isLogToConsole()) {
            Log.e(tag, info);
        }
        return info;
    }

    public static String e(String tag, final String message, final Throwable exception) {
        String info = message + "\n" + Log.getStackTraceString(exception);
        if (!isLoggable()) {
            return info;
        }
        if (tag == null) {
            info = createLog(info);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.e(tag, info);
        }
        return info;
    }

    public static String e2f(final String message){
        return e(message);
    }

    public static String e2f(String tag, final String message){
        return e(tag, message);
    }

    public static String e2f(String tag, final String message, final Throwable exception){
        return e(tag, message, exception);
    }

    public static String d(final String message) {
        if (!isLoggable()) {
            return "";
        }
        String info = createLog(message);
        String tag = className;
        if (isLogToConsole()) {
            Log.d(tag, info);
        }
        return info;
    }

    public static String d(String tag, final String message) {
        if (!isLoggable()) {
            return message;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.d(tag, info);
        }
        return info;
    }

    public static void d(String tag, final String message, final Throwable exception) {
        if (!isLoggable()) {
            return;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.d(tag, info, exception);
        }
    }

    public static void i(final String message) {
        if (!isLoggable()) {
            return;
        }
        String info = createLog(message);
        String tag = className;
        if (isLogToConsole()) {
            Log.i(tag, info);
        }
    }

    public static String i(String tag, final String message) {
        if (!isLoggable()) {
            return message;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.i(tag, info);
        }
        return info;
    }


    public static void i(String tag, final String message, final Throwable exception) {
        if (!isLoggable()) {
            return;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.i(tag, info, exception);
        }
    }

    public static void w(final String message) {
        if (!isLoggable()) {
            return;
        }
        String info = createLog(message);
        String tag = className;
        if (isLogToConsole()) {
            Log.w(tag, info);
        }
    }

    public static String w(String tag, final String message) {
        if (!isLoggable()) {
            return message;
        }
        String info = message;
        if (tag == null) {
            info = createLog(message);
            tag = className;
        }
        if (isLogToConsole()) {
            Log.w(tag, info);
        }
        return info;
    }

    public static void w(final String tag, final Throwable e) {
        if (!isLoggable()) {
            return;
        }

        if (isLogToConsole()) {
            Log.w(tag, Log.getStackTraceString(e));
        }
    }
}
