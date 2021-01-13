package top.leefeng.datepicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.pow

/**
 * 滚轮View
 * Mail:    lilifeng@tongxue-inc.com
 * Blog:    https://leefeng.top
 * Develop Date：2020-01-10
 */
open class PickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    init {
        layoutManager = LinearLayoutManager(context)
        overScrollMode = ViewGroup.OVER_SCROLL_NEVER
        LinearSnapHelper().attachToRecyclerView(this)
    }

    var enableAlpha = true
    override fun dispatchDraw(canvas: Canvas?) {
        children.forEach {
            val cellCenter = (it.top + it.bottom) / 2f
            var f = cellCenter / (measuredHeight / 2f)
            val revert = f > 1
            if (revert) f = 2 - f
            val scale = 0.7f + f * 0.3f
            if (scale.isNaN()) return
            it.scaleX = scale
            it.scaleY = scale
            if (enableAlpha)
                it.alpha = 0.3f + f * 0.7f
            val degree = 90 - f * 90
            it.rotation
            it.rotationX = if (revert) -degree else degree
            if (degree < 90) {
                val s = degree.toInt() / 90f
                it.translationY = (if (revert) -(s.pow(3.0f)) else (s).pow(3.0f)) * it.height
            } else {
                it.translationY = if (revert) it.height / 1f else -it.height / 1f
            }
            (it as? PickerTextView)?.let {
                if (it.pTextColor == it.pTextSelectColor) return@let
                val currentTop = (measuredHeight - it.height) / 2 - resources.displayMetrics.density
                val currentBottom = currentTop + it.height + 2 * resources.displayMetrics.density
                if (it.top < currentTop && it.bottom > currentTop) {
                    it.setPosition(true, currentTop - it.top, true)
                } else if (it.top < currentBottom && it.bottom > currentBottom) {
                    it.setPosition(false, currentBottom - it.top, true)

                } else if (it.bottom < currentTop || it.top > currentBottom) {
                    it.setPosition(true, 0f, false)
                } else {
                    it.setPosition(false, 0f, false)
                }
            }
            drawChild(canvas, it, drawingTime)
        }
    }


    interface DrawListener {
        fun drawBelow(canvas: Canvas?, width: Int, height: Int, cellHeight: Int)
        fun drawOver(canvas: Canvas?, width: Int, height: Int, cellHeight: Int)
    }
}