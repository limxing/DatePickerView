package top.leefeng.datepicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView

/**
 *
 * Created by lilifeng on 1/12/21
 *
 * Copyright www.putaoabc.com
 *
 */
class PickerTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    var pTextColor = Color.BLACK
    var pTextSelectColor = Color.BLACK
    private var line = 0f
    private var isTopLine = false
    private var needUpdate = false

    fun setPosition(isTopLine: Boolean, lineHeight: Float,update:Boolean) {
        line = lineHeight
        this.isTopLine = isTopLine
        needUpdate = update
        invalidate()
    }


    override fun draw(canvas: Canvas?) {

        if (!needUpdate) {
            setTextColor(if (isTopLine) pTextColor else pTextSelectColor)
            super.draw(canvas)
            return
        }

        canvas?.save()
        setTextColor(if (isTopLine) pTextColor else pTextSelectColor)
        canvas?.clipRect(0f, 0f, measuredWidth / 1f, line)
        super.draw(canvas)
        canvas?.restore()

        canvas?.save()
        setTextColor(if (isTopLine) pTextSelectColor else pTextColor)
        canvas?.clipRect(0f, line, measuredWidth/1f, measuredHeight / 1f)
        super.draw(canvas)
        canvas?.restore()

    }
}