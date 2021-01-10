package top.leefeng.datepicker

import android.content.Context
import android.util.AttributeSet

class SimplePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PickerView(context, attrs, defStyleAttr) {

    init {
        val it = context.obtainStyledAttributes(attrs, R.styleable.SimplePickerView)
        it.recycle()
    }
}