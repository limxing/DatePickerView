package top.leefeng.datepicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

/**
 * 选择器 可变色TextView
 * Mail:    lilifeng@tongxue-inc.com
 * Blog:    https://leefeng.top
 * Develop Date：2020-01-10
 */
class PickerTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    var pTextSelectColor = Color.BLACK
    var pTextColor = Color.BLACK
    private val rectf = RectF()
    override fun onDraw(canvas: Canvas?) {
        if (pTextColor == pTextSelectColor) {
            super.onDraw(canvas)
            return
        }
        val parentCenter = (parent as View).measuredHeight / 2f
        val topY = parentCenter - measuredHeight / 2f
        val bottomY = parentCenter + measuredHeight / 2f
        if (y < topY && y + measuredHeight > topY) {
            rectf.set(0f, 0f, measuredWidth / 1f, topY - y)
        } else if (y < bottomY && y + measuredHeight > bottomY) {
            rectf.set(0f, bottomY - y, measuredWidth / 1f, measuredHeight / 1f)
        } else if (y == topY && y + measuredHeight == bottomY) {
            rectf.set(0f, 0f, 0f, 0f)
        } else {
            rectf.set(0f, 0f, measuredWidth / 1f, measuredHeight / 1f)
        }
        canvas?.save()
        canvas?.clipRect(rectf)
        super.onDraw(canvas)
        canvas?.restore()
        canvas?.save()
        rectf.set(0f, 0f, 0f, 0f)
        if (y < topY && y + measuredHeight > topY) {
            rectf.set(0f, topY - y, measuredWidth / 1f, measuredHeight / 1f)
        } else if (y < bottomY && y + measuredHeight > bottomY) {
            rectf.set(0f, 0f, measuredWidth / 1f, bottomY - y)
        } else if (y == topY && y + measuredHeight == bottomY) {
            rectf.set(0f, 0f, measuredWidth / 1f, measuredHeight / 1f)
        }
        canvas?.clipRect(rectf)
        val method =
            TextView::class.java.getDeclaredMethod("getVerticalOffset", Boolean::class.java)
        method.isAccessible = true
        val offsetY = "${method.invoke(this, false)}".toFloat()
        canvas?.translate(0f, extendedPaddingTop + offsetY)
        paint.color = pTextSelectColor
        layout.draw(canvas)
        canvas?.restore()
    }
}