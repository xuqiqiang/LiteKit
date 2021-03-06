package com.xuqiqiang.uikit.requester;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.xuqiqiang.uikit.requester.proxy.RequestResultActivity;
import com.xuqiqiang.uikit.utils.Utils;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

public class ActivityRequester {

    public static final int DESTROY_ON_DESTROY = 0, DESTROY_ON_STOP = 1, DESTROY_ON_PAUSE = 2;
    private static final List<Event> mEventList = new LinkedList<>();

    // region Activity
    public static void startActivityForResult(Context context, Intent intent,
        OnActivityResultListener listener) {
        RequestResultActivity.start(context, intent, listener);
    }

    public static ActivityLifecycleAdapter postOnResume(Runnable r) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return null;
        return postOnResume(topActivity, r);
    }

    public static ActivityLifecycleAdapter postOnResume(Activity a, Runnable r) {
        ActivityOnResumeAdapter adapter = new ActivityOnResumeAdapter(a, r);
        a.getApplication().registerActivityLifecycleCallbacks(adapter);
        return adapter;
    }

    public static ActivityLifecycleAdapter postOnDestroyed(Runnable r) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return null;
        return postOnDestroyed(topActivity, r);
    }

    public static ActivityLifecycleAdapter postOnDestroyed(Activity a, Runnable r) {
        return postOnDestroyed(a, DESTROY_ON_DESTROY, r);
    }

    public static ActivityLifecycleAdapter postOnDestroyed(Activity a, int executeTime,
        Runnable r) {
        ActivityOnDestroyAdapter adapter = new ActivityOnDestroyAdapter(a, r, executeTime);
        a.getApplication().registerActivityLifecycleCallbacks(adapter);
        return adapter;
    }

    /**
     * @see ActivityRequester#postDelayed(Activity, Runnable, long)
     */
    @Deprecated
    public static boolean postDelayed(Runnable r, long delayMillis) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return false;
        return postDelayed(topActivity, r, delayMillis);
    }

    public static boolean postDelayed(@NonNull Activity a, Runnable r, long delayMillis) {
        if (r == null || a.isFinishing()) return false;
        Event event = new DelayedEvent(r);
        if (mMainHandler.postDelayed(event, delayMillis)) {
            mEventList.add(event);
            event.setActivityLifecycleAdapter(postOnDestroyed(a, new ClearRunnable(event)));
            return true;
        }
        return false;
    }

    public static void removeCallbacks(@NonNull Runnable r) {
        Event event = new Event(r);
        int index = mEventList.indexOf(event);
        if (index >= 0) {
            event = mEventList.get(index);
            event.clear();
            mEventList.remove(event);
            mMainHandler.removeCallbacks(event);
        }
    }
    // endregion

    // region Adapter

    public interface OnActivityResultListener {
        void onActivityResult(int resultCode, @Nullable Intent data);
    }

    private static class ActivityOnResumeAdapter extends ActivityLifecycleAdapter {

        public ActivityOnResumeAdapter(Activity activity, Runnable runnable) {
            super(activity, runnable);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            handleEvent(activity);
        }
    }

    private static class ActivityOnDestroyAdapter extends ActivityLifecycleAdapter {

        private final int executeTime;

        public ActivityOnDestroyAdapter(Activity activity, Runnable runnable, int executeTime) {
            super(activity, runnable);
            this.executeTime = executeTime;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            if (executeTime == DESTROY_ON_PAUSE && activity.isFinishing()) {
                handleEvent(activity);
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (executeTime == DESTROY_ON_STOP && activity.isFinishing()) {
                handleEvent(activity);
            }
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            if (executeTime == DESTROY_ON_DESTROY) {
                handleEvent(activity);
            }
        }
    }

    public static class ActivityLifecycleAdapter implements Application.ActivityLifecycleCallbacks {
        final WeakReference<Activity> rActivity;
        private Event event;
        //        private final WeakReference<Event> rEvent;

        public ActivityLifecycleAdapter(Activity activity, Runnable runnable) {
            this.rActivity = new WeakReference<>(activity);
            this.event = new Event(runnable);
            event.setActivityLifecycleAdapter(this);
            mEventList.add(event);
            //            this.rEvent = new WeakReference<>(event);
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity,
            @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity,
            @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            Activity a = rActivity.get();
            if (a == null || a == activity) {
                clear(activity);
            }
        }

        protected void handleEvent(@NonNull Activity activity) {
            Activity a = rActivity.get();
            if (a == activity) {
                if (event != null) event.run();
                clear(activity);
            } else if (a == null) {
                clear(activity);
            }
        }

        protected void clear(Activity activity) {
            if (activity == null) {
                Utils.getApp().unregisterActivityLifecycleCallbacks(this);
            } else {
                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }
            rActivity.clear();
            if (event != null) {
                event.clearReference();
                mEventList.remove(event);
                event = null;
            }
        }
    }

    private static class ClearRunnable implements Runnable {

        private Event event;

        public ClearRunnable(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            mEventList.remove(event);
            mMainHandler.removeCallbacks(event);
            event = null;
        }
    }

    public static class DelayedEvent extends Event {

        public DelayedEvent(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void run() {
            super.run();
            clear();
        }
    }

    public static class Event implements Runnable {
        private final SoftReference<Runnable> rRunnable;
        private SoftReference<ActivityLifecycleAdapter> rActivityAdapter;

        public Event(Runnable runnable) {
            this.rRunnable = new SoftReference<>(runnable);
        }

        public void setActivityLifecycleAdapter(ActivityLifecycleAdapter activityLifecycleAdapter) {
            this.rActivityAdapter = new SoftReference<>(activityLifecycleAdapter);
        }

        @Override
        public void run() {
            mEventList.remove(this);
            Runnable r = rRunnable.get();
            clearReference();
            if (r != null) r.run();
        }

        public void clear() {
            if (this.rActivityAdapter != null) {
                ActivityLifecycleAdapter adapter = rActivityAdapter.get();
                if (adapter != null) {
                    adapter.clear(adapter.rActivity.get());
                }
            }
            clearReference();
        }

        public void clearReference() {
            rRunnable.clear();
            if (this.rActivityAdapter != null) rActivityAdapter.clear();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;
            Event event = (Event) o;
            return rRunnable.get() == event.rRunnable.get();
        }

        @Override
        public int hashCode() {
            Runnable r = rRunnable.get();
            if (r != null) return r.hashCode();
            return super.hashCode();
        }
    }
    // endregion
}