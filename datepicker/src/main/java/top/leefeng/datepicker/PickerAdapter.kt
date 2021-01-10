package top.leefeng.datepicker

import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 简易适配器
 * Mail:    lilifeng@tongxue-inc.com
 * Blog:    https://leefeng.top
 * Develop Date：2020-01-10
 * cellHeight 单个cell的高度
 * dateShowSize 一屏cell的个数
 * txtsize cell文字的大小 px
 * textcolor cell文字的颜色
 */
class PickerAdapter(val cellHeight: Int,
                    val showSiz: Int,
                    val txtsize: Float,
                    val textcolor: Int):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val array = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(TextView(parent.context).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cellHeight)
            gravity = Gravity.CENTER// or Gravity.END
            setTextSize(TypedValue.COMPLEX_UNIT_PX, txtsize)
            setTextColor(textcolor)
        }) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as TextView).text = when (position) {
            in (0 until showSiz / 2) -> ""
            in itemCount - showSiz / 2 until itemCount -> ""
            else -> array[position + showSiz/2]
        }

    }

    override fun getItemCount(): Int {
        return array.size + showSiz -1
    }


    fun setData(list: List<String>) {
        array.clear()
        array.addAll(list)
        notifyDataSetChanged()
    }
}