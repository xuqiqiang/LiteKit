package com.xuqiqiang.uikit.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class Cache {
    private static final String TAG = Cache.class.getSimpleName();
    public static String rootName;
    public static String spName;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private FileInputStream fis;
    private DataInputStream dis;
    private FileOutputStream fos;
    private DataOutputStream dos;

    private Cache() {
    }

    public static void initialize(Context context, String name) {
        Cache.context = context.getApplicationContext();
        initSharedPreferences(name);
    }

    public static boolean hasInit() {
        return context != null;
    }

    public static Cache getInstance() {
        return new Cache();
    }

    public static void initSharedPreferences() {
        String spName = context.getPackageName();
        initSharedPreferences(spName);
    }

    public static void initSharedPreferences(String name) {
        if (editor != null) {
            editor.apply();
        }
        spName = name.replace(File.separator, "_");
        sharedPreferences = context.getSharedPreferences(spName,
            Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static int readInt(String name, int arg) {
        return sharedPreferences.getInt(name, arg);
    }

    public static void writeInt(String name, int a) {
        editor.putInt(name, a);
        editor.commit();
    }

    public static float readFloat(String name, float arg) {
        return sharedPreferences.getFloat(name, arg);
    }

    public static void writeFloat(String name, float a) {
        editor.putFloat(name, a);
        editor.commit();
    }

    public static double readDouble(String name, double arg) {
        double result = arg;
        try {
            String str = sharedPreferences.getString(name, arg + "");
            result = Double.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void writeDouble(String name, double a) {
        editor.putString(name, a + "");
        editor.commit();
    }

    public static String readString(String name, String arg) {
        return sharedPreferences.getString(name, arg);
    }

    public static void writeString(String name, String a) {
        editor.putString(name, a);
        editor.commit();
    }

    public static Boolean readBoolean(String name, Boolean arg) {
        return sharedPreferences.getBoolean(name, arg);
    }

    public static void writeBoolean(String name, Boolean a) {
        editor.putBoolean(name, a);
        editor.commit();
    }

    public static long readLong(String name, long arg) {
        return sharedPreferences.getLong(name, arg);
    }

    public static void writeLong(String name, long a) {
        editor.putLong(name, a);
        editor.commit();
    }

    public static void removeKey(String key) {
        editor.remove(key);
        editor.commit();
    }
}
