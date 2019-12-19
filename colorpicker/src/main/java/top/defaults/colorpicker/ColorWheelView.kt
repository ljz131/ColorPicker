package top.defaults.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.*

/**
 * HSV color wheel
 */
class ColorWheelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(
        context, attrs, defStyleAttr), ColorObservable, Updatable {
    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var isOnControl = false
    private var selectorRadiusPx: Float = Constants.SELECTOR_RADIUS_DP * 3.toFloat()
    private val currentPoint = PointF()
    private var currentColor = Color.MAGENTA
    private var onlyUpdateOnTouchEventUp = false
    private var selector: ColorWheelSelector
    private val emitter = ColorObservableEmitter()
    private val handler = ThrottledTouchEventHandler(this)

    private var keyDownCount = 0;

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        height = min(maxWidth, maxHeight)
        width = height
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        radius = min(netWidth, netHeight) * 0.5f - selectorRadiusPx
        if (radius < 0) {
            return
        }
        centerX = netWidth * 0.5f
        centerY = netHeight * 0.5f
        setColor(currentColor, false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("----", "进入onTouchEvent")
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                handler.onTouchEvent(event)
                true
            }
            MotionEvent.ACTION_UP -> {
                update(event)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        keyDownCount = 0
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d("----", "进入keyDown")
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            Log.d("----", "进入按下$keyCode")
            var x = currentPoint.x
            var y = currentPoint.y
            if (isOnControl) {
                if (keyDownCount >= 15) {
                    keyDownCount = 15
                } else {
                    keyDownCount++
                }
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> x -= keyDownCount
                    KeyEvent.KEYCODE_DPAD_RIGHT -> x += keyDownCount
                    KeyEvent.KEYCODE_DPAD_UP -> y -= keyDownCount
                    KeyEvent.KEYCODE_DPAD_DOWN -> y += keyDownCount
                    KeyEvent.KEYCODE_DPAD_DOWN_LEFT -> {
                        x -= keyDownCount
                        y += keyDownCount
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN_RIGHT -> {
                        x += keyDownCount
                        y -= keyDownCount
                    }
                    KeyEvent.KEYCODE_DPAD_UP_LEFT -> {
                        x -= keyDownCount
                        y += keyDownCount
                    }
                    KeyEvent.KEYCODE_DPAD_UP_RIGHT -> {
                        x += keyDownCount
                        y += keyDownCount
                    }
                    KeyEvent.KEYCODE_BACK -> isOnControl = false
                    else -> return super.onKeyDown(keyCode, event)
                }
                Log.d("----", "同步信息")
                emitter.onColor(getColorAtPoint(x, y), true, true)
                updateSelector(x, y)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                isOnControl = true
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun update(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val isTouchUpEvent = event.actionMasked == MotionEvent.ACTION_UP
        if (!onlyUpdateOnTouchEventUp || isTouchUpEvent) {
            emitter.onColor(getColorAtPoint(x, y), true, isTouchUpEvent)
        }
        updateSelector(x, y)
    }

    private fun getColorAtPoint(eventX: Float, eventY: Float): Int {
        val x = eventX - centerX
        val y = eventY - centerY
        val r = sqrt(x * x + y * y.toDouble())
        val hsv = floatArrayOf(0f, 0f, 1f)
        hsv[0] = (atan2(y.toDouble(), -x.toDouble()) / Math.PI * 180f).toFloat() + 180
        hsv[1] = max(0f, min(1f, (r / radius).toFloat()))
        hsv[2] = 1f
        return Color.HSVToColor(hsv)
    }

    fun setOnlyUpdateOnTouchEventUp(onlyUpdateOnTouchEventUp: Boolean) {
        this.onlyUpdateOnTouchEventUp = onlyUpdateOnTouchEventUp
    }

    fun setColor(color: Int, shouldPropagate: Boolean) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val r = hsv[1] * radius
        val radian = (hsv[0] / 180f * Math.PI).toFloat()
        updateSelector((r * cos(radian.toDouble()) + centerX).toFloat(),
                (-r * sin(radian.toDouble()) + centerY).toFloat())
        currentColor = color
        if (!onlyUpdateOnTouchEventUp) {
            emitter.onColor(color, false, shouldPropagate)
        }
    }

    private fun updateSelector(eventX: Float, eventY: Float) {
        var x = eventX - centerX
        var y = eventY - centerY
        val r = sqrt(x * x + y * y.toDouble())
        if (r > radius) {
            x *= radius / r.toFloat()
            y *= radius / r.toFloat()
        }
        currentPoint.x = x + centerX
        currentPoint.y = y + centerY
        selector.setCurrentPoint(currentPoint, getColorAtPoint(currentPoint.x, currentPoint.y))
    }

    override fun subscribe(observer: ColorObserver?) {
        emitter.subscribe(observer)
    }

    override fun unsubscribe(observer: ColorObserver?) {
        emitter.unsubscribe(observer)
    }

    override val color: Int
        get() = emitter.color

    init {
        selectorRadiusPx = Constants.SELECTOR_RADIUS_DP * resources.displayMetrics.density

        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        val palette = ColorWheelPalette(context)
        val padding = selectorRadiusPx.toInt()
        palette.setPadding(padding, padding, padding, padding)
        addView(palette, layoutParams)


        selector = ColorWheelSelector(context)
        selector.setSelectorRadiusPx(selectorRadiusPx)
        addView(selector, layoutParams)
    }
}