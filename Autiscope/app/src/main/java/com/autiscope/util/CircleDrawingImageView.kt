package com.autiscope.util


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class CircleDrawingImageView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs) {
    private val circles = mutableListOf<PointF>()
    private val paint: Paint = Paint()

    init {
        paint.color = Color.YELLOW
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (circle in circles) {
            canvas.drawCircle(
                circle.x,
                circle.y,
                50f,
                paint
            ) // Adjust the radius of the circle as needed
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                circles.add(PointF(touchX, touchY))
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
