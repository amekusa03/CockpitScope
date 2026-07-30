package com.kusa.cockpitscope

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import kotlin.math.cos
import kotlin.math.sin

class TelemetryMeterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private class MeterSeries(
        val label: String,
        val maxValue: Float,
        @ColorInt val color: Int,
        val decimalPlaces: Int = 0
    ) {
        var currentValue: Float = 0f
        var animatedValue: Float = 0f
        private var animator: ValueAnimator? = null

        fun updateValue(newValue: Float) {
            currentValue = newValue
            animator?.cancel()
            animator = ValueAnimator.ofFloat(animatedValue, newValue).apply {
                duration = 150
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    animatedValue = it.animatedValue as Float
                }
                start()
            }
        }
    }

    private val seriesMap = mutableLinkedHashMapOf<String, MeterSeries>()

    private val gaugePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        color = Color.DKGRAY
    }

    private val needlePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val valuePaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    fun addSeries(id: String, label: String, maxValue: Float, @ColorInt color: Int, decimalPlaces: Int = 0) {
        seriesMap[id] = MeterSeries(label, maxValue, color, decimalPlaces)
        postInvalidateOnAnimation()
    }

    fun clearSeries() {
        seriesMap.clear()
        postInvalidateOnAnimation()
    }

    fun addDataPoint(id: String, value: Float) {
        seriesMap[id]?.let {
            it.updateValue(value)
        }
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (seriesMap.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        
        // メーターの配置計算 (グリッド状)
        val count = seriesMap.size
        val cols = if (w > h) 3 else 2
        val rows = (count + cols - 1) / cols
        
        val cellW = w / cols
        val cellH = h / rows
        val radius = (minOf(cellW, cellH) * 0.4f)

        var index = 0
        seriesMap.values.forEach { series ->
            val col = index % cols
            val row = index / cols
            
            val centerX = col * cellW + cellW / 2
            val centerY = row * cellH + cellH / 2
            
            drawGauge(canvas, centerX, centerY, radius, series)
            index++
        }
        
        invalidate() // アニメーション継続のため
    }

    private fun drawGauge(canvas: Canvas, cx: Float, cy: Float, radius: Float, series: MeterSeries) {
        val startAngle = 135f
        val sweepAngle = 270f
        
        // 1. 背景の弧
        gaugePaint.color = Color.DKGRAY
        gaugePaint.alpha = 100
        canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, startAngle, sweepAngle, false, gaugePaint)
        
        // 2. 現在値の弧（カラー）
        val progress = (series.animatedValue / series.maxValue).coerceIn(0f, 1f)
        gaugePaint.color = series.color
        gaugePaint.alpha = 255
        canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, startAngle, sweepAngle * progress, false, gaugePaint)
        
        // 3. 針
        val angleRad = Math.toRadians((startAngle + sweepAngle * progress).toDouble())
        val needleLen = radius * 0.9f
        val nx = cx + needleLen * cos(angleRad).toFloat()
        val ny = cy + needleLen * sin(angleRad).toFloat()
        
        needlePaint.color = Color.WHITE
        canvas.drawLine(cx, cy, nx, ny, needlePaint)
        
        // 4. 中央のポッチ
        canvas.drawCircle(cx, cy, 10f, needlePaint)

        // 5. ラベルと値
        textPaint.color = Color.GRAY
        canvas.drawText(series.label, cx, cy + radius * 0.5f, textPaint)
        
        val formattedValue = "%.${series.decimalPlaces}f".format(series.animatedValue)
        valuePaint.color = series.color
        canvas.drawText(formattedValue, cx, cy + radius * 0.85f, valuePaint)
    }
}
