package com.xuqiqiang.uikit.utils;

import android.util.Log;
import java.util.Arrays;

@SuppressWarnings("unused")
public class Logger {
    public static final String TAG = "UIKit";
    public static boolean mEnabled = true;//BuildConfig.DEBUG;

    private static boolean isPresent;

    static {
        try {
            isPresent = null != Class.forName("com.orhanobut.logger.Logger");
            if (isPresent) {
                com.orhanobut.logger.Logger.addLogAdapter(
                    new com.orhanobut.logger.AndroidLogAdapter(
                        com.orhanobut.logger.PrettyFormatStrategy.newBuilder()
                            .methodOffset(1)
                            .tag(TAG)
                            .build()
                    ));
            }
        } catch (Exception ignored) {
        }
    }

    public static void e(Object msg) {
        if (!mEnabled || msg == null) return;
        if (isPresent) {
            com.orhanobut.logger.Logger.e(msg.toString());
            return;
        }
        Log.e(TAG, msg.toString());
    }

    public static void e(Object msg, Throwable e) {
        if (!mEnabled || msg == null) return;
        if (isPresent) {
            com.orhanobut.logger.Logger.e(e, msg.toString());
            return;
        }
        Log.e(TAG, msg.toString(), e);
    }

    public static void d(Object msg) {
        if (!mEnabled || msg == null) return;
        if (isPresent) {
            com.orhanobut.logger.Logger.d(msg);
            return;
        }
        Log.d(TAG, msg.toString());
    }

    public static void d(Object... msg) {
        if (!mEnabled || ArrayUtils.isEmpty(msg)) return;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < msg.length; i += 1) {
            String toString = null;
            if (msg[i] == null) {
                toString = "null";
            } else if (msg[i].getClass().isArray()) {
                if (msg[i] instanceof int[]) {
                    toString = Arrays.toString((int[]) msg[i]);
                } else if (msg[i] instanceof float[]) {
                    toString = Arrays.toString((float[]) msg[i]);
                } else if (msg[i] instanceof double[]) {
                    toString = Arrays.toString((double[]) msg[i]);
                } else if (msg[i] instanceof byte[]) {
                    toString = Arrays.toString((byte[]) msg[i]);
                } else if (msg[i] instanceof char[]) {
                    toString = Arrays.toString((char[]) msg[i]);
                } else if (msg[i] instanceof long[]) {
                    toString = Arrays.toString((long[]) msg[i]);
                } else if (msg[i] instanceof Object[]) {
                    toString = Arrays.toString((Object[]) msg[i]);
                }
            } else {
                toString = msg[i].toString();
            }
            str.append(toString);
            if (i < msg.length - 1) {
                str.append(", ");
            }
        }
        Log.d(TAG, str.toString());
    }

    public static void d(Object msg, Throwable e) {
        if (!mEnabled || msg == null) return;
        if (isPresent) {
            com.orhanobut.logger.Logger.e(e, msg.toString());
            return;
        }
        Log.d(TAG, msg.toString(), e);
    }

    public static void d(Object msg, Object[] arr) {
        if (!mEnabled) return;
        Log.d(TAG, msg + ":" + Arrays.toString(arr));
    }

    public static void e(String tag, Object msg) {
        if (!mEnabled) return;
        Log.e(tag, msg.toString());
    }

    public static void e(String tag, Object msg, Throwable e) {
        if (!mEnabled) return;
        Log.e(tag, msg.toString(), e);
    }

    //    public static void d(String tag, String msg) {
    //        if (!enabled) return;
    //        Log.d(tag, msg);
    //    }

    public static void d(String tag, Object msg, Throwable e) {
        if (!mEnabled) return;
        Log.d(tag, msg.toString(), e);
    }

    public static void d(String tag, Object msg, Object[] arr) {
        if (!mEnabled) return;
        Log.d(tag, msg + ":" + Arrays.toString(arr));
    }
}
