package top.leefeng.datepicker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
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
 * Mail:    lilifeng@tongxue-inc.com
 * Blog:    https://leefeng.top
 * Develop Date：2020-01-10
 */
class DatePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var lineStrokeWidth: Float
    private var positionDate: List<String>
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
    private val result = IntArray(3)
    var listener: ((IntArray) -> Unit)? = null

    var drawListener: PickerView.DrawListener? = null

    private val scroolListener = object : RecyclerView.OnScrollListener() {
        private var lastTag: Any = 0
        private var lastValue = ""
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                val value = getCurrentText(recyclerView)
                if (recyclerView.tag == lastTag && lastValue == value) return
                lastTag = recyclerView.tag
                lastValue = value
                val dayR = findViewWithTag<RecyclerView>(2)
                val monR = findViewWithTag<RecyclerView>(1)
                when (recyclerView.tag) {
                    0 -> {
                        caculateMonth(monR.adapter as DateAdapter, value)
                        monR.post {
                            caculateDay(
                                dayR.adapter as DateAdapter,
                                listOf(value, getCurrentText(monR))
                            )
                        }
                    }
                    1 -> {
                        val yearR = findViewWithTag<RecyclerView>(0)
                        monR.post {
                            caculateDay(
                                dayR.adapter as DateAdapter,
                                listOf(getCurrentText(yearR), value)
                            )
                        }
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            result[recyclerView.tag as Int] = getCurrentText(recyclerView).toInt()
            listener?.invoke(result)
        }
    }

    private fun getCurrentText(recyclerView: RecyclerView): String {
        var text =
            (recyclerView.findViewHolderForAdapterPosition(
                (recyclerView.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition() + dateShowSize / 2
            )?.itemView as? TextView)?.text.toString()
        if (unitScroll) text = text.dropLast(1)
        return if (text.length == 1) "0$text" else text


    }


    private val paint = Paint()
    private val rectF = RectF()

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
        if (dateShowSize % 2 == 0 || dateShowSize < 3) throw Throwable("dpvDateSize value must be  odd number and must be bigger than 2")
        unitMarginStart = it.getDimension(
            R.styleable.DatePickerView_dpvUnitMarginStart,
            resources.displayMetrics.density * 2
        ).toInt()

        lineStrokeWidth = it.getDimension(
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
        positionDate = datePosition.split("-")
        endDate = dateEnd.split("-")
        positionDate.forEachIndexed { index, s ->
            result[index] = s.toInt()
        }
        repeat(3) {
            addView(PickerView(context).apply {
                tag = it
                addOnScrollListener(scroolListener)
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
                        textSize,
                        textColor
                    )
                    scrollToPosition(this)
                }
            }
            listener?.invoke(result)
        }

        paint.isAntiAlias = true


    }

    private fun scrollToPosition(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter as DateAdapter
        recyclerView.scrollToPosition(
            when (recyclerView.tag) {
                0 -> {
                    adapter.setData(
                        starDate[0].toInt(),
                        endDate[0].toInt()
                    )
                    positionDate[0].toInt() - starDate[0].toInt()
                }
                1 -> {
                    positionDate[1].toInt() - caculateMonth(
                        adapter,
                        positionDate[0]
                    )
                }
                else -> {
                    positionDate[2].toInt() - caculateDay(
                        adapter,
                        positionDate
                    )
                }
            }
        )
        recyclerView.post {
            result[recyclerView.tag as Int] = getCurrentText(recyclerView).toInt()
        }

    }

    /**
     * 计算日
     */
    private fun caculateDay(adapter: DateAdapter, positionDate: List<String>): Int {
        val year = positionDate[0]
        val month = positionDate[1]
        val days = when (month) {
            "02" -> if (year.toInt() % 4 == 0) 29 else 28
            "04", "06", "09", "11" -> 30
            else -> 31
        }
        return if (starDate[0] == year) {
            adapter.setData(starDate[2].toInt(), days)
            starDate[2].toInt()
        } else if (endDate[0] == year) {
            adapter.setData(1, endDate[2].toInt())
            1
        } else {
            adapter.setData(1, days)
            1
        }

    }

    /**
     * 计算月份
     */
    private fun caculateMonth(adapter: DateAdapter, year: String): Int {
        return if (year == starDate[0]) {
            adapter.setData(starDate[1].toInt(), 12)
            starDate[1].toInt()
        } else if (year == endDate[0]) {
            adapter.setData(1, endDate[1].toInt())
            1
        } else {
            adapter.setData(1, 12)
            1
        }
    }

    /**
     * set end date
     *  format yyyy-MM-dd
     *  dateStart   开始日期
     *  dateEnd     结束日期
     *  datePosition定位日期
     */
    fun setDate(dateStart: String, dateEnd: String, datePosition: String) {
        post {
            val startArray = dateStart.split("-")
            if (startArray.size != 3) throw Throwable("dateStart format mast be yyyy-MM-dd")
            val endArray = dateEnd.split("-")
            if (endArray.size != 3) throw Throwable("dateEnd format mast be yyyy-MM-dd")
            val posArray = datePosition.split("-")
            if (posArray.size != 3) throw Throwable("datePosition format mast be yyyy-MM-dd")

            val startTime = sdf.parse(dateStart)!!.time
            val endTime = sdf.parse(dateEnd)!!.time
            val posTime = sdf.parse(datePosition)!!.time
            if (endTime < startTime) throw Throwable("dateEnd mast bu bigger than dateStart")
            if (posTime < startTime || posTime > endTime) throw Throwable("datePosition must between dateStart and dateEnd")

            endDate = endArray
            starDate = startArray
            positionDate = posArray
            children.filter { it is RecyclerView }.forEach {
                scrollToPosition(it as RecyclerView)
            }
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

//    val colorFilter = LightingColorFilter(0xffffff, 0x0000f0)
//    var lg: LinearGradient? = null
//    val color = Color.parseColor("#112233")
    override fun onDraw(canvas: Canvas?) {
        drawListener?.drawBelow(canvas, measuredWidth, measuredHeight, cellHeight)
        rectF.set(
            0f,
            (sizeHeight - cellHeight) / 2f,
            sizeWidth / 1f,
            (sizeHeight + cellHeight) / 2f
        )
        paint.reset()
        paint.isAntiAlias = true
        paint.strokeWidth = lineStrokeWidth
        paint.color = backColor
        paint.style = Paint.Style.FILL
        canvas?.drawRect(rectF, paint)

        paint.color = lineColor
        paint.style = Paint.Style.STROKE
        canvas?.drawLine(rectF.left, rectF.top, rectF.right, rectF.top, paint)
        canvas?.drawLine(rectF.left, rectF.bottom, rectF.right, rectF.bottom, paint)
        super.onDraw(canvas)

//        paint.shader = if (lg == null) {
//            lg = LinearGradient(0f, 0f, 0f, rectF.bottom, color, color, Shader.TileMode.CLAMP)
//            lg
//        } else lg
////        paint.colorFilter = colorFilter
//        paint.style = Paint.Style.FILL
//
//        canvas?.drawRect(0f, 0f, rectF.width(), rectF.top, paint)
        drawListener?.drawOver(canvas, measuredWidth, measuredHeight, cellHeight)
    }

    /**
     * type: 0 year 1 month 2 day
     */
    private class DateAdapter(
        val cellHeight: Int,
        val unit: String = "",
        val dateShowSize: Int,
        val txtsize: Float,
        val textcolor: Int
    ) :
        RecyclerView.Adapter<ViewHolder>() {

        private var fromNum = 0
        private var endNum = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : RecyclerView.ViewHolder(TextView(parent.context).apply {
                layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, cellHeight)
                gravity = Gravity.CENTER// or Gravity.END
                setTextSize(TypedValue.COMPLEX_UNIT_PX, txtsize)
                setTextColor(textcolor)
            }) {}
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder.itemView as TextView).text = when (position) {
                in (0 until dateShowSize / 2) -> ""
                in itemCount - dateShowSize / 2 until itemCount -> ""
                else -> "${fromNum + position - dateShowSize / 2}$unit"
            }

        }

        override fun getItemCount(): Int {
            return endNum - fromNum + dateShowSize
        }


        fun setData(from: Int, end: Int): Boolean {

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