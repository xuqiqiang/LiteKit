package com.xuqiqiang.uikit.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.xuqiqiang.uikit.utils.SingleTaskHandler;
import com.xuqiqiang.uikit.R;

import static android.view.GestureDetector.SimpleOnGestureListener;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.xuqiqiang.uikit.utils.DisplayUtils.attrData;

public class TouchRipple extends FrameLayout {

    private static final int     DEFAULT_DURATION        = 200;
    private static final int     DEFAULT_FADE_DURATION   = 300;//75;
    private static final float   DEFAULT_DIAMETER_DP     = 100;
    private static final float   DEFAULT_ALPHA           = 0.3f;
    private static final int     DEFAULT_COLOR           = Color.BLACK;
    private static final int     DEFAULT_BACKGROUND      = Color.TRANSPARENT;
    private static final boolean DEFAULT_HOVER           = true;
    private static final boolean DEFAULT_DELAY_CLICK     = false;
    private static final boolean DEFAULT_PERSISTENT      = false;
    private static final boolean DEFAULT_SEARCH_ADAPTER  = false;
    private static final boolean DEFAULT_RIPPLE_OVERLAY  = true;
    private static final int     DEFAULT_ROUNDED_CORNERS = 0;

    private static final int  FADE_EXTRA_DELAY = 50;
    private static final long HOVER_DURATION   = 400;

    private static boolean isCheckTapInScrollingContainer = true;

    private final Paint paint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect  bounds = new Rect();

    private final SingleTaskHandler mSingleTaskHandler = new SingleTaskHandler(Looper.getMainLooper());
    private int      rippleColor;
    private boolean  rippleOverlay;
    private boolean  rippleHover;
    private int      rippleDiameter;
    private int      rippleDuration;
    private int      rippleAlpha;
    private boolean  rippleDelayClick;
    private int      rippleFadeDuration;
    private boolean  ripplePersistent;
    private Drawable rippleBackground;
    private boolean  rippleInAdapter;
    private float    rippleRoundedCorners;

    private float    rippleRoundedCornersTopLeft;
    private float    rippleRoundedCornersTopRight;
    private float    rippleRoundedCornersBottomLeft;
    private float    rippleRoundedCornersBottomRight;

    private int mSecurityTime = 300;

    private float radius;

    private AdapterView parentAdapter;
    private View        childView;

    private AnimatorSet    rippleAnimator;
    private ObjectAnimator hoverAnimator;
    private ObjectAnimator hoverFadeAnimator;

    private Point currentCoords  = new Point();
    private Point previousCoords = new Point();

    private int layerType;

    private boolean eventCancelled;
    private boolean prepressed;
    private int     positionInAdapter;

    private GestureDetector   gestureDetector;
    private PerformClickEvent pendingClickEvent;
    private PressedEvent      pendingPressEvent;

    public static void setCheckTapInScrollingContainer(boolean enabled) {
        isCheckTapInScrollingContainer = enabled;
    }

    public TouchRipple(Context context) {
        this(context, null, 0);
    }

    public TouchRipple(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchRipple(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);
        gestureDetector = new GestureDetector(context, longClickListener, new Handler(Looper.getMainLooper()));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TouchRipple);

        rippleColor = a.getColor(R.styleable.TouchRipple_mrl_rippleColor, attrData(context, R.attr.colorControlHighlight)); // DEFAULT_COLOR
        rippleDiameter = a.getDimensionPixelSize(
                R.styleable.TouchRipple_mrl_rippleDimension,
                (int) dpToPx(getResources(), DEFAULT_DIAMETER_DP)
        );
        rippleOverlay = a.getBoolean(R.styleable.TouchRipple_mrl_rippleOverlay, DEFAULT_RIPPLE_OVERLAY);
        rippleHover = a.getBoolean(R.styleable.TouchRipple_mrl_rippleHover, DEFAULT_HOVER);
        rippleDuration = a.getInt(R.styleable.TouchRipple_mrl_rippleDuration, DEFAULT_DURATION);
        rippleAlpha = (int) (255 * a.getFloat(R.styleable.TouchRipple_mrl_rippleAlpha, DEFAULT_ALPHA));
        rippleDelayClick = a.getBoolean(R.styleable.TouchRipple_mrl_rippleDelayClick, DEFAULT_DELAY_CLICK);
        rippleFadeDuration = a.getInteger(R.styleable.TouchRipple_mrl_rippleFadeDuration, DEFAULT_FADE_DURATION);
        rippleBackground = new ColorDrawable(a.getColor(R.styleable.TouchRipple_mrl_rippleBackground, DEFAULT_BACKGROUND));
        ripplePersistent = a.getBoolean(R.styleable.TouchRipple_mrl_ripplePersistent, DEFAULT_PERSISTENT);
        rippleInAdapter = a.getBoolean(R.styleable.TouchRipple_mrl_rippleInAdapter, DEFAULT_SEARCH_ADAPTER);
        rippleRoundedCorners = a.getDimensionPixelSize(R.styleable.TouchRipple_mrl_rippleRoundedCorners, DEFAULT_ROUNDED_CORNERS);

        rippleRoundedCornersTopLeft = a.getDimensionPixelSize(R.styleable.TouchRipple_mrl_rippleRoundedCornersTopLeft, DEFAULT_ROUNDED_CORNERS);
        rippleRoundedCornersTopRight = a.getDimensionPixelSize(R.styleable.TouchRipple_mrl_rippleRoundedCornersTopRight, DEFAULT_ROUNDED_CORNERS);
        rippleRoundedCornersBottomLeft = a.getDimensionPixelSize(R.styleable.TouchRipple_mrl_rippleRoundedCornersBottomLeft, DEFAULT_ROUNDED_CORNERS);
        rippleRoundedCornersBottomRight = a.getDimensionPixelSize(R.styleable.TouchRipple_mrl_rippleRoundedCornersBottomRight, DEFAULT_ROUNDED_CORNERS);
        mSecurityTime = a.getInt(R.styleable.TouchRipple_ripple_time, mSecurityTime);

        a.recycle();

        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);

        enableClipPathSupportIfNecessary();
    }


    @SuppressWarnings("unchecked")
    public <T extends View> T getChildView() {
        return (T) childView;
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("TouchRipple can host only one child");
        }
        //noinspection unchecked
        childView = child;
        super.addView(child, index, params);
    }

    @Override
    public void setOnClickListener(final OnClickListener onClickListener) {
        if (childView == null) {
            throw new IllegalStateException("TouchRipple must have a child view to handle clicks");
        }
        childView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mSingleTaskHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onClickListener != null) onClickListener.onClick(TouchRipple.this);
                    }
                }, mSecurityTime);
            }
        });
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onClickListener) {
        if (childView == null) {
            throw new IllegalStateException("TouchRipple must have a child view to handle clicks");
        }
        childView.setOnLongClickListener(onClickListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !findClickableViewInChild(childView, (int) event.getX(), (int) event.getY());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superOnTouchEvent = super.onTouchEvent(event);

        if (!isEnabled() || !childView.isEnabled()) return superOnTouchEvent;

        boolean isEventInBounds = bounds.contains((int) event.getX(), (int) event.getY());

        if (isEventInBounds) {
            previousCoords.set(currentCoords.x, currentCoords.y);
            currentCoords.set((int) event.getX(), (int) event.getY());
        }

        boolean gestureResult = gestureDetector.onTouchEvent(event);
        if (gestureResult || hasPerformedLongPress) {
            return true;
        } else {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    pendingClickEvent = new PerformClickEvent();

                    if (prepressed) {
                        childView.setPressed(true);
                        postDelayed(
                                new Runnable() {
                                    @Override public void run() {
                                        childView.setPressed(false);
                                    }
                                }, ViewConfiguration.getPressedStateDuration());
                    }

                    if (isEventInBounds) {
                        startRipple(pendingClickEvent);
                    } else if (!rippleHover) {
                        setRadius(0);
                    }
                    if (!rippleDelayClick && isEventInBounds) {
                        pendingClickEvent.run();
                    }
                    cancelPressedEvent();
                    break;
                case MotionEvent.ACTION_DOWN:
                    setPositionInAdapter();
                    eventCancelled = false;
                    pendingPressEvent = new PressedEvent(event);
                    if (isCheckTapInScrollingContainer && isInScrollingContainer()) {
                        cancelPressedEvent();
                        prepressed = true;
                        postDelayed(pendingPressEvent, ViewConfiguration.getTapTimeout());
                    } else {
                        pendingPressEvent.run();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (rippleInAdapter) {
                        // dont use current coords in adapter since they tend to jump drastically on scroll
                        currentCoords.set(previousCoords.x, previousCoords.y);
                        previousCoords = new Point();
                    }
                    childView.onTouchEvent(event);
                    if (rippleHover) {
                        if (!prepressed) {
                            startRipple(null);
                        }
                    } else {
                        childView.setPressed(false);
                    }
                    cancelPressedEvent();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (rippleHover) {
                        if (isEventInBounds && !eventCancelled) {
                            invalidate();
                        } else if (!isEventInBounds) {
                            startRipple(null);
                        }
                    }

                    if (!isEventInBounds) {
                        cancelPressedEvent();
                        if (hoverAnimator != null) {
                            hoverAnimator.cancel();
                        }
                        if (hoverFadeAnimator != null) {
                            hoverFadeAnimator.cancel();
                        }
                        childView.onTouchEvent(event);
                        eventCancelled = true;
                    }
                    break;
            }
            return true;
        }
    }

    private void cancelPressedEvent() {
        if (pendingPressEvent != null) {
            removeCallbacks(pendingPressEvent);
            prepressed = false;
        }
    }

    private boolean hasPerformedLongPress;
    private SimpleOnGestureListener longClickListener = new GestureDetector.SimpleOnGestureListener() {
        public void onLongPress(MotionEvent e) {
            hasPerformedLongPress = childView.performLongClick();
            if (hasPerformedLongPress) {
                if (rippleHover) {
                    startRipple(null);
                }
                cancelPressedEvent();
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            hasPerformedLongPress = false;
            return super.onDown(e);
        }
    };

    private void startHover() {
        if (eventCancelled) return;

        if (hoverAnimator != null) {
            hoverAnimator.cancel();
        }
        final float radius = (float) (Math.sqrt(Math.pow(getWidth(), 2) + Math.pow(getHeight(), 2)) * 1.2f);
        hoverAnimator = ObjectAnimator.ofFloat(this, radiusProperty, rippleDiameter, radius)
                .setDuration(HOVER_DURATION);
        hoverAnimator.setInterpolator(new LinearInterpolator());
        hoverAnimator.start();

//        setRadius(radius);

        if (hoverFadeAnimator != null) {
            hoverFadeAnimator.cancel();
        }

        hoverFadeAnimator = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha / 3, rippleAlpha);
        hoverFadeAnimator.setDuration(300);
        hoverFadeAnimator.setInterpolator(new AccelerateInterpolator());
        hoverFadeAnimator.start();
    }

    private void startRipple(final Runnable animationEndRunnable) {
        if (eventCancelled) return;

        float endRadius = getEndRadius();

        cancelAnimations();

        rippleAnimator = new AnimatorSet();
        rippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (!ripplePersistent) {
                    setRadius(0);
                    setRippleAlpha(rippleAlpha);
                }
                if (animationEndRunnable != null && rippleDelayClick) {
                    animationEndRunnable.run();
                }
                childView.setPressed(false);
            }
        });

        ObjectAnimator ripple = ObjectAnimator.ofFloat(this, radiusProperty, radius, endRadius);
        ripple.setDuration(rippleDuration);
        ripple.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator fade = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha, 0);
        fade.setDuration(rippleFadeDuration);
        fade.setInterpolator(new AccelerateInterpolator());
        fade.setStartDelay(rippleDuration - rippleFadeDuration - FADE_EXTRA_DELAY);

        if (ripplePersistent) {
            rippleAnimator.play(ripple);
        } else if (getRadius() > endRadius) {
            fade.setStartDelay(0);
            rippleAnimator.play(fade);
        } else {
            rippleAnimator.playTogether(ripple, fade);
        }
        rippleAnimator.start();

//        ObjectAnimator fade = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha, 0);
//        fade.setDuration(rippleFadeDuration);
//        fade.setInterpolator(new AccelerateInterpolator());
//        rippleAnimator.play(fade);
//        rippleAnimator.start();
    }

    private void cancelAnimations() {
        if (rippleAnimator != null) {
            rippleAnimator.cancel();
            rippleAnimator.removeAllListeners();
        }

        if (hoverAnimator != null) {
            hoverAnimator.cancel();
        }
        if (hoverFadeAnimator != null) {
            hoverFadeAnimator.cancel();
        }
    }

    private float getEndRadius() {
        final int width = getWidth();
        final int height = getHeight();

        final int halfWidth = width / 2;
        final int halfHeight = height / 2;

        final float radiusX = halfWidth > currentCoords.x ? width - currentCoords.x : currentCoords.x;
        final float radiusY = halfHeight > currentCoords.y ? height - currentCoords.y : currentCoords.y;

        return (float) Math.sqrt(Math.pow(radiusX, 2) + Math.pow(radiusY, 2)) * 1.2f;
    }

    private boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    private AdapterView findParentAdapterView() {
        if (parentAdapter != null) {
            return parentAdapter;
        }
        ViewParent current = getParent();
        while (true) {
            if (current instanceof AdapterView) {
                parentAdapter = (AdapterView) current;
                return parentAdapter;
            } else {
                try {
                    current = current.getParent();
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not find a parent AdapterView");
                }
            }
        }
    }

    private void setPositionInAdapter() {
        if (rippleInAdapter) {
            positionInAdapter = findParentAdapterView().getPositionForView(TouchRipple.this);
        }
    }

    private boolean adapterPositionChanged() {
        if (rippleInAdapter) {
            int newPosition = findParentAdapterView().getPositionForView(TouchRipple.this);
            final boolean changed = newPosition != positionInAdapter;
            positionInAdapter = newPosition;
            if (changed) {
                cancelPressedEvent();
                cancelAnimations();
                childView.setPressed(false);
                setRadius(0);
            }
            return changed;
        }
        return false;
    }

    private boolean findClickableViewInChild(View view, int x, int y) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                final Rect rect = new Rect();
                child.getHitRect(rect);

                final boolean contains = rect.contains(x, y);
                if (contains) {
                    return findClickableViewInChild(child, x - rect.left, y - rect.top);
                }
            }
        } else if (view != childView) {
            return (view.isEnabled() && (view.isClickable() || view.isLongClickable() || view.isFocusableInTouchMode()));
        }

        return view.isFocusableInTouchMode();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(0, 0, w, h);
        rippleBackground.setBounds(bounds);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    /*
     * Drawing
     */
    @Override
    public void draw(Canvas canvas) {
        final boolean positionChanged = adapterPositionChanged();
        if (rippleOverlay) {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
            }
            super.draw(canvas);
            if (!positionChanged) {
                if (rippleRoundedCorners != 0
                        || rippleRoundedCornersTopLeft + rippleRoundedCornersTopRight
                        + rippleRoundedCornersBottomRight + rippleRoundedCornersBottomLeft > 0) {
                    Path clipPath = new Path();
                    RectF rect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
                    if (rippleRoundedCorners != 0) {
                        clipPath.addRoundRect(rect, rippleRoundedCorners, rippleRoundedCorners, Path.Direction.CW);
                    } else {
                        float[] radiusArray = { rippleRoundedCornersTopLeft, rippleRoundedCornersTopLeft,
                                rippleRoundedCornersTopRight, rippleRoundedCornersTopRight,
                                rippleRoundedCornersBottomRight, rippleRoundedCornersBottomRight,
                                rippleRoundedCornersBottomLeft, rippleRoundedCornersBottomLeft };
                        clipPath.addRoundRect(rect, radiusArray, Path.Direction.CW);
                    }
                    canvas.clipPath(clipPath);
                }
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
        } else {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
            super.draw(canvas);
        }
    }

    /*
     * Animations
     */
    private Property<TouchRipple, Float> radiusProperty
            = new Property<TouchRipple, Float>(Float.class, "radius") {
        @Override
        public Float get(TouchRipple object) {
            return object.getRadius();
        }

        @Override
        public void set(TouchRipple object, Float value) {
            object.setRadius(value);
        }
    };

    private float getRadius() {
        return radius;
    }


    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    private Property<TouchRipple, Integer> circleAlphaProperty
            = new Property<TouchRipple, Integer>(Integer.class, "rippleAlpha") {
        @Override
        public Integer get(TouchRipple object) {
            return object.getRippleAlpha();
        }

        @Override
        public void set(TouchRipple object, Integer value) {
            object.setRippleAlpha(value);
        }
    };

    public int getRippleAlpha() {
        return paint.getAlpha();
    }

    public void setRippleAlpha(Integer rippleAlpha) {
        paint.setAlpha(rippleAlpha);
        invalidate();
    }

    /**
     * {@link Canvas#clipPath(Path)} is not supported in hardware accelerated layers
     * before API 18. Use software layer instead
     * <p/>
     * https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported
     */
    private void enableClipPathSupportIfNecessary() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (rippleRoundedCorners != 0
                    || rippleRoundedCornersTopLeft + rippleRoundedCornersTopRight
                    + rippleRoundedCornersBottomRight + rippleRoundedCornersBottomLeft > 0) {
                layerType = getLayerType();
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            } else {
                setLayerType(layerType, null);
            }
        }
    }

    /*
     * Helper
     */
    private class PerformClickEvent implements Runnable {

        @Override public void run() {
            if (hasPerformedLongPress) return;

            // if parent is an AdapterView, try to call its ItemClickListener
            if (getParent() instanceof AdapterView) {
                // try clicking direct child first
                if (!childView.performClick())
                    // if it did not handle it dispatch to adapterView
                    clickAdapterView((AdapterView) getParent());
            } else if (rippleInAdapter) {
                // find adapter view
                clickAdapterView(findParentAdapterView());
            } else {
                // otherwise, just perform click on child
                childView.performClick();
            }
        }

        private void clickAdapterView(AdapterView parent) {
            final int position = parent.getPositionForView(TouchRipple.this);
            final long itemId = parent.getAdapter() != null
                    ? parent.getAdapter().getItemId(position)
                    : 0;
            if (position != AdapterView.INVALID_POSITION) {
                parent.performItemClick(TouchRipple.this, position, itemId);
            }
        }
    }

    private final class PressedEvent implements Runnable {

        private final MotionEvent event;

        public PressedEvent(MotionEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            prepressed = false;
            childView.setLongClickable(false);//prevent the child's long click,let's the ripple layout call it's performLongClick
            childView.onTouchEvent(event);
            childView.setPressed(true);
            if (rippleHover) {
                startHover();
            }
        }
    }

    static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}