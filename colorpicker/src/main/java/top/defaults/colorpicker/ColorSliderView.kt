package top.defaults.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

abstract class ColorSliderView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(
        context, attrs, defStyleAttr), ColorObservable, Updatable {
    protected var baseColor = Color.WHITE
    private val colorPaint: Paint
    private val borderPaint: Paint
    private val selectorPaint: Paint
    private val selectorPath: Path
    private val currentSelectorPath = Path()
    protected var selectorSize = 0f
    protected var currentValue = 1f
    private var onlyUpdateOnTouchEventUp = false
    private val emitter = ColorObservableEmitter()
    private val handler = ThrottledTouchEventHandler(this)

    private var keyDownCount = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        configurePaint(colorPaint)
        selectorPath.reset()
        selectorSize = h * 0.25f
        selectorPath.moveTo(0f, 0f)
        selectorPath.lineTo(selectorSize * 2, 0f)
        selectorPath.lineTo(selectorSize, selectorSize)
        selectorPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.drawRect(selectorSize + paddingLeft, selectorSize + paddingTop,
                width - selectorSize - paddingRight, height - paddingBottom - selectorSize,
                colorPaint)
        canvas.drawRect(selectorSize + paddingLeft, selectorSize + paddingTop,
                width - selectorSize - paddingRight, height - paddingBottom - selectorSize,
                borderPaint)
        selectorPath.offset(
                paddingLeft + currentValue * (width - 2 * selectorSize - paddingLeft - paddingRight),
                paddingTop.toFloat(), currentSelectorPath)
        canvas.drawPath(currentSelectorPath, selectorPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        return when (action) {
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
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            if (keyDownCount >= 15) {
                keyDownCount = 15
            } else {
                keyDownCount++
            }
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    currentValue -= 0.001f * keyDownCount
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    currentValue += 0.001f * keyDownCount
                    onKeyLongPress(keyCode, event)
                }
                else -> {
                    return super.onKeyDown(keyCode, event)
                }
            }
            updateCurrentValue()
            return true
        } else {
            if (event == null) {
                Log.e("----", "夭寿了，event居然是空")
            } else {
                Log.e("----", "夭寿了，action居然拦截出问题了" + event.action)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        Log.e("----", "夭寿了，触发长按")
        return super.onKeyLongPress(keyCode, event)
    }

    override fun update(event: MotionEvent) {
        updateValue(event.x)
        val isTouchUpEvent = event.actionMasked == MotionEvent.ACTION_UP
        if (!onlyUpdateOnTouchEventUp || isTouchUpEvent) {
            emitter.onColor(assembleColor(), true, isTouchUpEvent)
        }
    }

    fun setBaseColor(color: Int, fromUser: Boolean, shouldPropagate: Boolean) {
        baseColor = color
        configurePaint(colorPaint)
        var targetColor = color
        if (!fromUser) { // if not set by user (means programmatically), resolve currentValue from color value
            currentValue = resolveValue(color)
        } else {
            targetColor = assembleColor()
        }
        if (!onlyUpdateOnTouchEventUp) {
            emitter.onColor(targetColor, fromUser, shouldPropagate)
        } else if (shouldPropagate) {
            emitter.onColor(targetColor, fromUser, true)
        }
        invalidate()
    }

    private fun updateCurrentValue() {
        if (currentValue > 1f) {
            currentValue = 1f
        }
        if (currentValue < 0) {
            currentValue = 0f
        }
        emitter.onColor(assembleColor(), true, true)
        invalidate()
    }

    private fun updateValue(eventX: Float) {
        var eventX = eventX
        val left = paddingLeft + selectorSize
        val right = width - paddingRight - selectorSize
        if (eventX < left) {
            eventX = left
        }
        if (eventX > right) {
            eventX = right
        }
        currentValue = (eventX - left) / (right - left)
        invalidate()
    }

    protected abstract fun resolveValue(color: Int): Float
    protected abstract fun configurePaint(colorPaint: Paint)
    protected abstract fun assembleColor(): Int
    override fun subscribe(observer: ColorObserver?) {
        emitter.subscribe(observer)
    }

    override fun unsubscribe(observer: ColorObserver?) {
        emitter.unsubscribe(observer)
    }

    override val color: Int
        get() = emitter.color

    fun setOnlyUpdateOnTouchEventUp(onlyUpdateOnTouchEventUp: Boolean) {
        this.onlyUpdateOnTouchEventUp = onlyUpdateOnTouchEventUp
    }

    private val bindObserver: ColorObserver = object : ColorObserver {
        override fun onColor(color: Int, fromUser: Boolean, shouldPropagate: Boolean) {
            setBaseColor(color, fromUser, shouldPropagate)
        }
    }
    private var boundObservable: ColorObservable? = null
    fun bind(colorObservable: ColorObservable?) {
        if (colorObservable != null) {
            colorObservable.subscribe(bindObserver)
            setBaseColor(colorObservable.color, true, true)
        }
        boundObservable = colorObservable
    }

    fun unbind() {
        if (boundObservable != null) {
            boundObservable!!.unsubscribe(bindObserver)
            boundObservable = null
        }
    }

    init {
        setBackgroundResource(R.drawable.selector_red_btn_border_2019ver)
        isFocusable = true
        setPadding(6, 6, 6, 6)
        colorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 0f
        borderPaint.color = Color.BLACK
        selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        selectorPaint.color = Color.BLACK
        selectorPath = Path()
        selectorPath.fillType = Path.FillType.WINDING
    }
}