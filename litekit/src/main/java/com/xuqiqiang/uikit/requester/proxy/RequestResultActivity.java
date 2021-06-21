package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.xuqiqiang.uikit.requester.ActivityRequester;

public class RequestResultActivity extends Activity {
    private static final int REQUEST_ACTIVITY_RESULT = 0x005000;
    private static Intent mIntent;
    private static ActivityRequester.OnActivityResultListener mListener;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final Runnable mReleaseEvent = new Runnable() {
        @Override public void run() {
            if (mListener != null) {
                mListener.onActivityResult(Activity.RESULT_CANCELED, null);
            }
            mIntent = null;
            mListener = null;
        }
    };

    public static void start(Context context, Intent intent,
        ActivityRequester.OnActivityResultListener listener) {
        mHandler.postDelayed(mReleaseEvent, 1000);
        mIntent = intent;
        mListener = listener;
        Intent i = new Intent(context, RequestResultActivity.class);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler.removeCallbacksAndMessages(null);
        if (mIntent == null) {
            finish();
            return;
        }
        try {
            startActivityForResult(mIntent, REQUEST_ACTIVITY_RESULT);
        } catch (Exception e) {
            e.printStackTrace();
            mReleaseEvent.run();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACTIVITY_RESULT) {
            if (mListener != null) {
                mListener.onActivityResult(resultCode, data);
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mIntent = null;
        mListener = null;
        super.onDestroy();
    }
}
