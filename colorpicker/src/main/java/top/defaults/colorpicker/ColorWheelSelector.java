package top.defaults.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import static top.defaults.colorpicker.Constants.SELECTOR_RADIUS_DP;

public class ColorWheelSelector extends View {

    private Paint selectorPaint;
    private float selectorRadiusPx = SELECTOR_RADIUS_DP * 3;
    private PointF currentPoint = new PointF();

    private Paint previewCirclePaint;
    private int color = Color.parseColor("#FFFFFF");

    public ColorWheelSelector(Context context) {
        this(context, null);
    }

    public ColorWheelSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorWheelSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setColor(Color.BLACK);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(2);

        previewCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewCirclePaint.setStyle(Paint.Style.FILL);
        previewCirclePaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawLine(currentPoint.x - selectorRadiusPx, currentPoint.y,
//                currentPoint.x + selectorRadiusPx, currentPoint.y, selectorPaint);
//        canvas.drawLine(currentPoint.x, currentPoint.y - selectorRadiusPx,
//                currentPoint.x, currentPoint.y + selectorRadiusPx, selectorPaint);
        canvas.drawCircle(currentPoint.x, currentPoint.y, selectorRadiusPx * 0.66f, selectorPaint);
        canvas.drawCircle(currentPoint.x, currentPoint.y, selectorRadiusPx * 0.66f, previewCirclePaint);
    }

    public void setSelectorRadiusPx(float selectorRadiusPx) {
        this.selectorRadiusPx = selectorRadiusPx;
    }

    public void setColor(int color) {
        this.color = color;
        previewCirclePaint.setColor(color);
        invalidate();
    }

    public void setCurrentPoint(PointF currentPoint, int color) {
        this.currentPoint = currentPoint;
        previewCirclePaint.setColor(color);
        invalidate();
    }
}
