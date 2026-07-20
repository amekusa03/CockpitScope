package com.kusa.cockpitscope

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class TelemetryScopeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private class DataSeries(
        val label: String,
        val maxValue: Float,
        @ColorInt initialColor: Int,
        val decimalPlaces: Int = 0
    ) {
        val points = mutableListOf<Float>()
        
        val linePaint = Paint().apply {
            color = initialColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }

        val glowPaint = Paint().apply {
            color = initialColor
            style = Paint.Style.STROKE
            strokeWidth = 12f
            isAntiAlias = true
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
        }

        val fillPaint = Paint().apply {
            color = initialColor
            alpha = 60 // 透過を抑えて重なりを見やすく
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // 残像用の透明度管理
        val trailAlphas = intArrayOf(40, 20)
    }

    private val seriesMap = mutableMapOf<String, DataSeries>()
    private var maxDataPoints = 300

    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 1f
        alpha = 60
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        setShadowLayer(3f, 2f, 2f, Color.BLACK)
    }

    private val linePath = Path()
    private val fillPath = Path()

    // BlurMaskFilterを使用するためにソフトウェアレンダリングを有効にする
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun addSeries(id: String, label: String, maxValue: Float, @ColorInt color: Int, decimalPlaces: Int = 0) {
        seriesMap[id] = DataSeries(label, maxValue, color, decimalPlaces)
    }

    fun clearSeries() {
        seriesMap.clear()
        postInvalidateOnAnimation()
    }

    fun addDataPoint(id: String, value: Float) {
        seriesMap[id]?.let { series ->
            series.points.add(value)
            if (series.points.size > maxDataPoints) {
                series.points.removeAt(0)
            }
        }
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxDataPoints = (w / 3).coerceAtLeast(100)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. 背景グリッド
        for (i in 0..4) {
            val y = h * i / 4
            canvas.drawLine(0f, y, w, y, gridPaint)
        }
        for (i in 0..10) {
            val x = w * i / 10
            canvas.drawLine(x, 0f, x, h, gridPaint)
        }

        // 2. 各データシリーズの描画
        seriesMap.values.forEach { series ->
            val points = series.points
            if (points.size < 2) return@forEach

            linePath.reset()
            fillPath.reset()
            
            val stepX = w / (maxDataPoints - 1)
            val latestValue = points.last()
            val intensity = (latestValue / series.maxValue).coerceIn(0f, 1f)
            
            for (i in points.indices) {
                val reverseIndex = points.size - 1 - i
                val x = w - (i * stepX)
                val value = points[reverseIndex]
                val y = h - (value / series.maxValue * h).coerceIn(0f, h)
                
                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                if (i == points.size - 1) {
                    fillPath.lineTo(x, h)
                    fillPath.close()
                }
            }

            // 残像表現 (Trail)
            series.trailAlphas.forEachIndexed { index, alpha ->
                val offset = (index + 1) * 4f
                canvas.save()
                canvas.translate(-offset, 0f)
                series.linePaint.alpha = alpha
                canvas.drawPath(linePath, series.linePaint)
                canvas.restore()
            }
            series.linePaint.alpha = 255

            // 塗りつぶし
            canvas.drawPath(fillPath, series.fillPaint)

            // グロー効果 (高負荷時/高回転時に強調)
            if (intensity > 0.7f) {
                series.glowPaint.alpha = ((intensity - 0.7f) / 0.3f * 200).toInt().coerceIn(0, 255)
                canvas.drawPath(linePath, series.glowPaint)
            }

            // メインのライン
            canvas.drawPath(linePath, series.linePaint)
        }

        // 3. ラベル表示
        var labelY = 60f
        seriesMap.values.forEach { series ->
            val lastValue = if (series.points.isNotEmpty()) series.points.last() else 0f
            val formattedValue = "%.${series.decimalPlaces}f".format(lastValue)
            
            textPaint.color = series.linePaint.color
            canvas.drawText("${series.label}: $formattedValue", 30f, labelY, textPaint)
            labelY += 50f
        }
    }
}
