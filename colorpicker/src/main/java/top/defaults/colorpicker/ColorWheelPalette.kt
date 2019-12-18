package top.defaults.colorpicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ColorWheelPalette @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(
        context, attrs, defStyleAttr) {
    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f
    private val huePaint: Paint
    private val saturationPaint: Paint
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        radius = Math.min(netWidth, netHeight) * 0.5f
        if (radius < 0) {
            return
        }
        centerX = w * 0.5f
        centerY = h * 0.5f
        val hueShader: Shader = SweepGradient(centerX, centerY,
                intArrayOf(Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN,
                        Color.YELLOW, Color.RED), null)
        huePaint.shader = hueShader
        val saturationShader: Shader = RadialGradient(centerX, centerY, radius, Color.WHITE,
                0x00FFFFFF, Shader.TileMode.CLAMP)
        saturationPaint.shader = saturationShader
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, huePaint)
        canvas.drawCircle(centerX, centerY, radius, saturationPaint)
    }

    init {
        huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        saturationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }
}