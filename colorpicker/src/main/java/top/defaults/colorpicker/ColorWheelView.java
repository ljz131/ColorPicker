package top.defaults.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static top.defaults.colorpicker.Constants.SELECTOR_RADIUS_DP;

/**
 * HSV color wheel
 */
public class ColorWheelView extends FrameLayout implements ColorObservable, Updatable {

    private float radius;
    private float centerX;
    private float centerY;

    private boolean isOnControl = false;

    private float selectorRadiusPx = SELECTOR_RADIUS_DP * 3;

    private PointF currentPoint = new PointF();
    private int currentColor = Color.MAGENTA;
    private boolean onlyUpdateOnTouchEventUp;

    private ColorWheelSelector selector;

    private ColorObservableEmitter emitter = new ColorObservableEmitter();
    private ThrottledTouchEventHandler handler = new ThrottledTouchEventHandler(this);

    public ColorWheelView(Context context) {
        this(context, null);
    }

    public ColorWheelView(Context context,
                          @Nullable
                                  AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorWheelView(Context context,
                          @Nullable
                                  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        selectorRadiusPx = SELECTOR_RADIUS_DP * getResources().getDisplayMetrics().density;

        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ColorWheelPalette palette = new ColorWheelPalette(context);
            int padding = (int) selectorRadiusPx;
            palette.setPadding(padding, padding, padding, padding);
            addView(palette, layoutParams);
        }

        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            selector = new ColorWheelSelector(context);
            selector.setSelectorRadiusPx(selectorRadiusPx);
            addView(selector, layoutParams);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        width = height = Math.min(maxWidth, maxHeight);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int netWidth = w - getPaddingLeft() - getPaddingRight();
        int netHeight = h - getPaddingTop() - getPaddingBottom();
        radius = Math.min(netWidth, netHeight) * 0.5f - selectorRadiusPx;
        if (radius < 0) {
            return;
        }
        centerX = netWidth * 0.5f;
        centerY = netHeight * 0.5f;
        setColor(currentColor, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("----", "进入onTouchEvent");
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                handler.onTouchEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
                update(event);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("----", "进入keyDown");
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d("----", "进入按下" + keyCode);
            float x = currentPoint.x;
            float y = currentPoint.y;
            if (isOnControl) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        x--;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        x++;
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        y--;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        y++;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
                        x--;
                        y--;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
                        x++;
                        y--;
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP_LEFT:
                        x--;
                        y++;
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                        x++;
                        y++;
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        isOnControl = false;
                        break;
                    default:
                        return super.onKeyDown(keyCode, event);
                }
                Log.d("----", "同步信息");
                emitter.onColor(getColorAtPoint(x, y), true, true);
                updateSelector(x, y);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                isOnControl = true;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void update(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean isTouchUpEvent = event.getActionMasked() == MotionEvent.ACTION_UP;
        if (!onlyUpdateOnTouchEventUp || isTouchUpEvent) {
            emitter.onColor(getColorAtPoint(x, y), true, isTouchUpEvent);
        }
        updateSelector(x, y);
    }

    private int getColorAtPoint(float eventX, float eventY) {
        float x = eventX - centerX;
        float y = eventY - centerY;
        double r = Math.sqrt(x * x + y * y);
        float[] hsv = {0, 0, 1};
        hsv[0] = (float) (Math.atan2(y, -x) / Math.PI * 180f) + 180;
        hsv[1] = Math.max(0f, Math.min(1f, (float) (r / radius)));
        return Color.HSVToColor(hsv);
    }

    public void setOnlyUpdateOnTouchEventUp(boolean onlyUpdateOnTouchEventUp) {
        this.onlyUpdateOnTouchEventUp = onlyUpdateOnTouchEventUp;
    }

    public void setColor(int color, boolean shouldPropagate) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float r = hsv[1] * radius;
        float radian = (float) (hsv[0] / 180f * Math.PI);
        updateSelector((float) (r * Math.cos(radian) + centerX), (float) (-r * Math.sin(radian) + centerY));
        currentColor = color;
        if (!onlyUpdateOnTouchEventUp) {
            emitter.onColor(color, false, shouldPropagate);
        }
    }

    private void updateSelector(float eventX, float eventY) {
        float x = eventX - centerX;
        float y = eventY - centerY;
        double r = Math.sqrt(x * x + y * y);
        if (r > radius) {
            x *= radius / r;
            y *= radius / r;
        }
        currentPoint.x = x + centerX;
        currentPoint.y = y + centerY;
        selector.setCurrentPoint(currentPoint,getColorAtPoint(currentPoint.x,currentPoint.y));
    }

    @Override
    public void subscribe(ColorObserver observer) {
        emitter.subscribe(observer);
    }

    @Override
    public void unsubscribe(ColorObserver observer) {
        emitter.unsubscribe(observer);
    }

    @Override
    public int getColor() {
        return emitter.getColor();
    }
}
