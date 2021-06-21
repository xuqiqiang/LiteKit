package com.xuqiqiang.uikit.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.xuqiqiang.uikit.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";

    private static final String KEY_CRASH_TRACE = "crashTrace";
    public static final boolean SHOW_ALERT = false;
    public static final boolean SAVE_TO_STORAGE = true;
    @SuppressLint("StaticFieldLeak")
    private static CrashHandler mInstance;
    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    private Class<?> mMainActivity;
    private List<String> mFilterStrs;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context, Class<?> mainActivity) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
            mMainActivity = mainActivity;
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    public void init(@NonNull Activity mainActivity, CrashHandler.OnHandleCrashListener listener) {
        init(mainActivity, null, listener);
    }

    public void init(@NonNull
        Activity mainActivity, List<String> filterStrs,
        CrashHandler.OnHandleCrashListener listener) {
        if (mContext == null) {
            mContext = mainActivity.getApplicationContext();
            mMainActivity = mainActivity.getClass();
            mFilterStrs = filterStrs;
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
            handleCrash(listener);
        }
    }

    private void handleCrash(final CrashHandler.OnHandleCrashListener listener) {
        if (SAVE_TO_STORAGE) {
            new Thread() {
                @Override
                public void run() {
                    final String crashInfo = Cache.readString(KEY_CRASH_TRACE, null);
                    Log.d(TAG, "crashTrace : " + (crashInfo != null));
                    if (!TextUtils.isEmpty(crashInfo)) {
                        Cache.writeString(KEY_CRASH_TRACE, null);
                        if (!checkFilter(crashInfo)) {
                            File dir = mContext.getExternalFilesDir("crash");
                            if (!dir.exists() && !dir.mkdirs()) {
                                return;
                            }

                            String fileName =
                                "crash_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA)
                                    .format(System.currentTimeMillis()) + ".txt";
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(new File(dir, fileName));
                                fos.write(crashInfo.getBytes(StandardCharsets.UTF_8));
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (fos != null) {
                                        fos.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (listener != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override public void run() {
                                        listener.onHandleCrash(crashInfo);
                                    }
                                });
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private boolean checkFilter(String crashInfo) {
        if (!ArrayUtils.isEmpty(mFilterStrs)) {
            for (String str : mFilterStrs) {
                if (crashInfo.contains(str)) return true;
            }
        }
        return false;
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        if (ex == null) {
            Intent intent = new Intent(mContext,
                mMainActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", "Unknown crash");
            mContext.startActivity(intent);
            android.os.Process
                .killProcess(android.os.Process
                    .myPid());
            return;
        }
        Log.e(TAG, "uncaughtException:" + ex.toString());
        if (SHOW_ALERT) {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        new AlertDialog.Builder(mContext)
                            .setTitle(R.string.prompt)
                            .setMessage(R.string.feedback_handle_crash)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(
                                    DialogInterface dialog,
                                    int id) {
                                    dialog.cancel();
                                    handleException(ex);
                                }
                            })
                            .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        handleException(ex);
                    }
                    Looper.loop();
                }
            }.start();
        } else {
            handleException(ex);
        }
    }

    private void handleException(Throwable ex) {
        try {
            String crashInfo = readCrashInfo(ex);
            if (crashInfo.length() > 3000) crashInfo = crashInfo.substring(0, 3000);
            Cache.writeString(KEY_CRASH_TRACE, crashInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(mContext,
            mMainActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        intent.putExtra("crash", crashInfo);
        mContext.startActivity(intent);
        android.os.Process
            .killProcess(android.os.Process
                .myPid());
    }

    private String readCrashInfo(Throwable ex) {
        if (ex == null) {
            return "UnknownException";
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        Log.e(TAG, result);
        return result;
    }

    public interface OnHandleCrashListener {
        void onHandleCrash(String crashInfo);
    }
}