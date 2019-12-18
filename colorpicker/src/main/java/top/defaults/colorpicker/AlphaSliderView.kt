package top.defaults.colorpicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import top.defaults.checkerboarddrawable.CheckerboardDrawable

class AlphaSliderView : ColorSliderView {
    private var backgroundBitmap: Bitmap? = null
    private var backgroundCanvas: Canvas? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundBitmap = Bitmap.createBitmap((w - 2 * selectorSize).toInt(),
                (h - selectorSize).toInt(), Bitmap.Config.ARGB_8888)
        backgroundCanvas = Canvas(backgroundBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable: Drawable = CheckerboardDrawable.create()
        drawable.setBounds(0, 0, backgroundCanvas!!.width, backgroundCanvas!!.height)
        drawable.draw(backgroundCanvas)
        canvas.drawBitmap(backgroundBitmap, selectorSize, selectorSize, null)
        super.onDraw(canvas)
    }

    override fun resolveValue(color: Int): Float {
        return Color.alpha(color) / 255f
    }

    override fun configurePaint(colorPaint: Paint) {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        val startColor = Color.HSVToColor(0, hsv)
        val endColor = Color.HSVToColor(255, hsv)
        val shader: Shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), startColor, endColor,
                Shader.TileMode.CLAMP)
        colorPaint.shader = shader
    }

    override fun assembleColor(): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        val alpha = (currentValue * 255).toInt()
        return Color.HSVToColor(alpha, hsv)
    }
}