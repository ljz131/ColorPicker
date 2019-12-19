package top.defaults.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class ColorWheelSelector @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(
        context, attrs, defStyleAttr) {
    private val selectorBorderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorColorPreviewCirclePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectorRadiusPx: Float = Constants.SELECTOR_RADIUS_DP * 3.toFloat()
    private var currentPoint = PointF()

    private var color:Int = Color.parseColor("#FFFFFF")

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        selectorColorPreviewCirclePaint.style = Paint.Style.FILL
        selectorColorPreviewCirclePaint.color = color
        selectorColorPreviewCirclePaint.setShadowLayer(100f, 5f, 5f, Color.GRAY)

        selectorBorderPaint.color = Color.BLACK
        selectorBorderPaint.style = Paint.Style.STROKE
        selectorBorderPaint.strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas) {
        //        canvas.drawLine(currentPoint.x - selectorRadiusPx, currentPoint.y,
        //                currentPoint.x + selectorRadiusPx, currentPoint.y, selectorPaint)
        //        canvas.drawLine(currentPoint.x, currentPoint.y - selectorRadiusPx, currentPoint.x,
        //                currentPoint.y + selectorRadiusPx, selectorPaint)
        canvas.drawCircle(currentPoint.x, currentPoint.y, selectorRadiusPx * 0.66f, selectorBorderPaint)
        canvas.drawCircle(currentPoint.x, currentPoint.y, selectorRadiusPx * 0.66f, selectorColorPreviewCirclePaint)
    }

    fun setColor(color: Int) {
        this.color = color
        selectorColorPreviewCirclePaint.color = color
        invalidate()
    }

    fun setSelectorRadiusPx(radius: Float) {
        selectorRadiusPx = radius
        invalidate()
    }

    fun setCurrentPoint(currentPoint: PointF?, color: Int) {
        this.currentPoint = currentPoint!!
        selectorColorPreviewCirclePaint.color = color
        invalidate()
    }
}