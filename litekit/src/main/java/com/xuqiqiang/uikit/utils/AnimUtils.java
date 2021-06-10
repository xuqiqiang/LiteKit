package com.xuqiqiang.uikit.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import com.xuqiqiang.uikit.R;
import java.lang.reflect.Method;

import static android.animation.PropertyValuesHolder.ofFloat;

public class AnimUtils {
    public static final int ANIM_DURING = 300;
    private static boolean mHasSetHiddenApiExemptions;

    public static void show(View view) {
        show(view, null);
    }

    public static void show(View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(
            R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
            ofFloat("alpha", 1f));
        objectAnimator.setDuration(ANIM_DURING);
        fixDurationScale(objectAnimator);
        objectAnimator.start();
        if (onComplete != null) {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onComplete.run();
                }
            });
        }

        view.setVisibility(View.VISIBLE);
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }

    public static void hide(View view, long duration) {
        hide(view, duration, null);
    }

    public static void hide(final View view, long duration, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(
            R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
            ofFloat("alpha", 0f));
        objectAnimator.setDuration(duration);
        fixDurationScale(objectAnimator);
        objectAnimator.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
                if (onComplete != null) onComplete.run();
            }
        });
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }

    public static void showScale(View view) {
        showScale(view, null);
    }

    public static void showScale(View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(
            R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
            ofFloat("scaleX", 1f),
            ofFloat("scaleY", 1f),
            ofFloat("alpha", 1f));
        objectAnimator.setDuration(ANIM_DURING);
        fixDurationScale(objectAnimator);
        objectAnimator.start();
        if (onComplete != null) {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onComplete.run();
                }
            });
        }

        view.setVisibility(View.VISIBLE);
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }

    public static void hideScale(View view) {
        hideScale(view, null);
    }

    public static void hideScale(final View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(
            R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
            ofFloat("scaleX", 0.7f),
            ofFloat("scaleY", 0.7f),
            ofFloat("alpha", 0f));
        objectAnimator.setDuration(ANIM_DURING);
        fixDurationScale(objectAnimator);
        objectAnimator.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            }
        });
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }

    public static void setAlpha(View view, float alpha, final Runnable... onComplete) {
        if (view.getTag(R.id.tag_anim_alpha) != null && view.getTag(
            R.id.tag_anim_alpha) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_alpha);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", alpha);
        objectAnimator.setDuration(ANIM_DURING);
        fixDurationScale(objectAnimator);
        objectAnimator.start();
        if (onComplete != null) {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    for (Runnable r : onComplete)
                        r.run();
                }
            });
        }
        view.setTag(R.id.tag_anim_alpha, objectAnimator);
    }

    public static void fixDurationScale(ValueAnimator animator) {
        try {
            // 10.0 以上系统提示: Accessing hidden method*********
            // 诸如此类的问题主要是Google限制了反射调用隐藏方法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (!mHasSetHiddenApiExemptions) {
                    try {
                        Method forName = Class.class.getDeclaredMethod("forName", String.class);
                        Method getDeclaredMethod =
                            Class.class.getDeclaredMethod("getDeclaredMethod", String.class,
                                Class[].class);
                        Class<?> vmRuntimeClass =
                            (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                        Method getRuntime =
                            (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                        Method setHiddenApiExemptions =
                            (Method) getDeclaredMethod.invoke(vmRuntimeClass,
                                "setHiddenApiExemptions",
                                new Class[] {String[].class});
                        Object sVmRuntime = getRuntime.invoke(null);
                        setHiddenApiExemptions.invoke(sVmRuntime,
                            new Object[] {new String[] {"L"}});
                        mHasSetHiddenApiExemptions = true;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            Method overrideDurationScale = animator.getClass()
                .getMethod(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                        "overrideDurationScale" : "setDurationScale", float.class);
            overrideDurationScale.setAccessible(true);
            overrideDurationScale.invoke(animator, 1f);
            Logger.d("fixDurationScale", "overrideDurationScale 1f");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
