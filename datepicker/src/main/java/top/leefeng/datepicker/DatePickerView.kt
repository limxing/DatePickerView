package top.leefeng.datepicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期选择器
 * lilifeng@tongxue-inc.com
 */
class DatePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var datePaddingEnd: Int
    private var datePaddingStart: Int
    private var backColor: Int
    private var lineColor: Int
    private var unitMarginStart: Int
    private var endDate: List<String>
    private var starDate: List<String>
    private var oneRecyclerW: Int = 0
    private var unitScroll: Boolean = false
    private var cellHeight: Int = 0
    private var dateShowSize: Int = 5
    private var sizeHeight: Int = 0
    private var sizeWidth: Int = 0
    private val units = arrayOf("年", "月", "日")
    private val sdf = SimpleDateFormat("yyy-MM-dd", Locale.CHINA)
    private var isScrolling = false

    private val listenet = object : RecyclerView.OnScrollListener() {
        private var lastTag: Any = 0
        private var lastValue = ""
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            if (newState == SCROLL_STATE_DRAGGING) isScrolling = true
//            else if (newState == SCROLL_STATE_IDLE) isScrolling = false
            if (newState == SCROLL_STATE_IDLE) {
                val value = getCurrentText(recyclerView)
                println("lefeng onScrollStateChanged  ${lastTag == recyclerView.tag} ==$lastValue $value  ${lastValue == value}")
                if (recyclerView.tag == lastTag && lastValue == value) return
                lastTag = recyclerView.tag
                lastValue = value
                val dayR = findViewWithTag<RecyclerView>(2)
                val yearR = findViewWithTag<RecyclerView>(0)
                val monR = findViewWithTag<RecyclerView>(1)
                val posDate =
                    listOf(getCurrentText(yearR), getCurrentText(monR), getCurrentText(dayR))
                when (recyclerView.tag) {
                    0 -> {
                        caculateMonth(monR.adapter as DateAdapter, posDate){
                            monR.dpvUpdate()
                        }
                        monR.post {
                            caculateDay(
                                dayR.adapter as DateAdapter,
                                listOf(
                                    getCurrentText(yearR),
                                    getCurrentText(monR),
                                    getCurrentText(dayR)
                                )
                            ){
                                dayR.dpvUpdate()
                            }
                        }
                    }
                    1 -> {
                        monR.post {
                            caculateDay(dayR.adapter as DateAdapter, posDate){
                                dayR.dpvUpdate()
                            }
                        }
                    }
                }
            }
        }

        private fun getCurrentText(recyclerView: RecyclerView): String {
            var text =
                (recyclerView.findViewHolderForAdapterPosition((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() + dateShowSize / 2)?.itemView as? TextView)?.text.toString()
            if (unitScroll) text = text.dropLast(1)
            return if (text.length == 1) "0$text" else text


        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        }
    }

    private val paint = Paint()
    private val rectf = RectF()

    init {
        setWillNotDraw(false)
        val it = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView)
        unitScroll = it.getBoolean(R.styleable.DatePickerView_dpvUnitScroll, false)
        val textColor = it.getColor(R.styleable.DatePickerView_dpvUnitTextColor, Color.BLACK)
        val textSize = it.getDimension(
            R.styleable.DatePickerView_dpvDateTextSize,
            20 * resources.displayMetrics.density
        )
        val unitTextSize = it.getDimension(
            R.styleable.DatePickerView_dpvUnitTextSize,
            18 * resources.displayMetrics.density
        )
        val dateLoop = it.getBoolean(R.styleable.DatePickerView_dpvDateLoop, false)
        val dateStar = it.getString(R.styleable.DatePickerView_dpvDateStar) ?: "1970-01-01"
        val dateEnd = it.getString(R.styleable.DatePickerView_dpvDateEnd) ?: sdf.format(Date())
        val datePosition = it.getString(R.styleable.DatePickerView_dpvDatePosition) ?: dateEnd
        try {
            if (sdf.parse(dateStar)!!.time > sdf.parse(dateEnd)!!.time) {
                throw Throwable("dateStar can not bigger than dateEnd")
            }
        } catch (e: Throwable) {
            throw Throwable("dateStar or dateEnd format error please check for yyyy-MM-dd")
        }
        dateShowSize = it.getInt(R.styleable.DatePickerView_dpvDateSize, 5)
        unitMarginStart = it.getDimension(
            R.styleable.DatePickerView_dpvUnitMarginStart,
            resources.displayMetrics.density * 2
        ).toInt()

        paint.strokeWidth = it.getDimension(
            R.styleable.DatePickerView_dpvLineWidth,
            resources.displayMetrics.density
        )
        lineColor = it.getColor(R.styleable.DatePickerView_dpvLineColor, Color.TRANSPARENT)
        backColor = it.getColor(R.styleable.DatePickerView_dpvBackgroundColor, Color.TRANSPARENT)
        datePaddingStart =
            it.getDimension(R.styleable.DatePickerView_dpvDatePaddingStart, 0f).toInt()
        datePaddingEnd = it.getDimension(R.styleable.DatePickerView_dpvDatePaddingEnd, 0f).toInt()
        it.recycle()
        starDate = dateStar.split("-")
        val positionDate = datePosition.split("-")
        endDate = dateEnd.split("-")

        repeat(3) {
            addView(RecyclerView(context).apply {
//                    adapter =
                addItemDecoration(object:ItemDecoration(){
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: State
                    ) {

                    }

                })
                layoutManager = object : LinearLayoutManager(context) {
                    override fun scrollVerticallyBy(
                        dy: Int,
                        recycler: Recycler?,
                        state: State?
                    ): Int {
                        dpvUpdate()

                        return super.scrollVerticallyBy(dy, recycler, state)
                    }


                }
                tag = it
                overScrollMode = OVER_SCROLL_NEVER
                LinearSnapHelper().attachToRecyclerView(this)
                addOnScrollListener(listenet)
            })
            if (!unitScroll)
                addView(TextView(context).apply {
                    text = units[it]
                    setTextColor(textColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, unitTextSize)
                })
        }
        post {
            children.filter { it is RecyclerView }.forEachIndexed { index, view ->
                (view as RecyclerView).apply {
                    adapter = DateAdapter(
                        cellHeight,
                        if (unitScroll) units[index] else "",
                        dateShowSize,
                        index,
                        textSize,
                        textColor
                    )
                    scrollToPosition(
                        when (index) {
                            0 -> {
                                (adapter as DateAdapter).setData(
                                    starDate[0].toInt(),
                                    endDate[0].toInt()
                                )
                                positionDate[0].toInt() - starDate[0].toInt()
                            }
                            1 -> {
                                positionDate[1].toInt() - caculateMonth(
                                    adapter as DateAdapter,
                                    positionDate
                                )
                            }
                            else -> {
                                positionDate[2].toInt() - caculateDay(
                                    adapter as DateAdapter,
                                    positionDate
                                )
                            }
                        } //+ dateShowSize / 2
                    )
                    dpvUpdate()
                }
            }
        }

        paint.isAntiAlias = true


    }

    private fun RecyclerView.dpvUpdate() {
        post {
            for (i in 0 until childCount) {
                val cellH = sizeHeight / dateShowSize / 2f
                getChildAt(i)?.let {
                    var scale = (it.top + cellH) / sizeHeight + 0.5f
                    if (scale > 1f) scale -= 2f
                    if (scale < 0f) scale = -scale
                    it.scaleX = scale
                    it.scaleY = scale
                    it.alpha = scale
                }
            }

        }
    }

    private fun caculateDay(adapter: DateAdapter, positionDate: List<String>,dataUpdate:(()->Unit)?=null): Int {
        val year = positionDate[0]
        val month = positionDate[1]
        val days = when (month) {
            "02" -> if (year.toInt() % 4 == 0) 29 else 28
            "04", "06", "09", "11" -> 30
            else -> 31
        }
        return if (starDate[0] == year && starDate[1] == month) {
            //当前等于开始年
            if(adapter.setData(starDate[2].toInt(), days)){
                dataUpdate?.invoke()
            }
            starDate[2].toInt()
        } else if (endDate[0] == year && endDate[1] == month) {
            if (adapter.setData(1, endDate[2].toInt())){
                dataUpdate?.invoke()
            }
            1
        } else {
            if(adapter.setData(1, days)){
                dataUpdate?.invoke()
            }
            1
        }

    }

    /**
     * 计算应该设置月份
     */
    private fun caculateMonth(adapter: DateAdapter, positionDate: List<String>,dataUpdate:(()->Unit)?=null): Int {
        return if (positionDate[0] == starDate[0]) {
            //与开始年一致，月份的开始跟开始年一致
            if(adapter.setData(starDate[1].toInt(), 12)){
                dataUpdate?.invoke()
            }
            starDate[1].toInt()
        } else if (positionDate[0] == endDate[0]) {
            if(adapter.setData(1, endDate[1].toInt())){
                dataUpdate?.invoke()
            }
            1
        } else {
            if(adapter.setData(1, 12)){
                dataUpdate?.invoke()
            }
            1
        }
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        var left = datePaddingStart
        children.forEachIndexed { index, view ->
            val isRecycler = view is RecyclerView
            if (!isRecycler) left += unitMarginStart
            val right = left + if (index == 0) {
                5 * oneRecyclerW
            } else if (isRecycler) {
                oneRecyclerW * 4
            } else {
                view.measuredWidth
            }
            view.layout(
                left,
                if (isRecycler) 0 else (measuredHeight - view.measuredHeight) / 2,
                right,
                if (isRecycler) measuredHeight else (measuredHeight + view.measuredHeight) / 2
            )
            left = right
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        cellHeight = sizeHeight / dateShowSize

        //如果单位不跟随滚动。计算单位的宽高
        oneRecyclerW = (if (unitScroll) {
            (sizeWidth - datePaddingStart - datePaddingEnd) / 13
        } else {
            (sizeWidth - (get(1).measuredWidth + unitMarginStart) * 3 - datePaddingStart - datePaddingEnd) / 13
        }).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        rectf.set(
            0f,
            (sizeHeight - cellHeight) / 2f,
            sizeWidth / 1f,
            (sizeHeight + cellHeight) / 2f
        )
        paint.color = backColor
        paint.style = Paint.Style.FILL
        canvas?.drawRect(rectf, paint)

        paint.color = lineColor
        paint.style = Paint.Style.STROKE


        canvas?.drawLine(rectf.left, rectf.top, rectf.right, rectf.top, paint)
        canvas?.drawLine(rectf.left, rectf.bottom, rectf.right, rectf.bottom, paint)
        super.onDraw(canvas)
    }

    /**
     * type: 0 year 1 month 2 day
     */
    private class DateAdapter(
        val cellHeight: Int,
        val unit: String = "",
        val dateShowSize: Int,
        val type: Int,
        val txtsize: Float,
        val textcolor: Int
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var fromNum = 0
        private var endNum = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(TextView(parent.context).apply {
                layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, cellHeight)
                gravity = Gravity.CENTER// or Gravity.END
                setTextSize(TypedValue.COMPLEX_UNIT_PX, txtsize)
                setTextColor(textcolor)
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as TextView).text = when (position) {
                in (0 until dateShowSize / 2) -> ""
                in itemCount - dateShowSize / 2 until itemCount -> ""
                else -> "${fromNum + position - 2}$unit"
            }

        }

        override fun getItemCount(): Int {
            return endNum - fromNum + dateShowSize
        }


        fun setData(from: Int, end: Int):Boolean {
            if (from == fromNum && end == endNum) {
                return false
            }
            fromNum = from
            endNum = end
            notifyDataSetChanged()
            return true
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isScrolling) return false
        when (ev?.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> return false
        }
        return super.dispatchTouchEvent(ev)
    }
}