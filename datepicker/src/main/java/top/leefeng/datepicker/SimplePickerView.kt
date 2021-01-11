package top.leefeng.datepicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SimplePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PickerView(context, attrs, defStyleAttr) {

    private var backColor: Int
    private var lineColor: Int
    private var lineStrokeWidth: Float
    private var cellHeight: Int = 0
    private var textSize: Float
    private var textColor: Int
    private var showSize: Int
    private var listener: ((Int) -> Unit)? = null
    var drawListener: DrawListener? = null

    init {
        setWillNotDraw(false)
        val it = context.obtainStyledAttributes(attrs, R.styleable.SimplePickerView)
        showSize = it.getInt(R.styleable.SimplePickerView_spvShowSize, 5)
        if (showSize % 2 == 0 || showSize < 3) throw Throwable("dpvDateSize value must be  odd number and must be bigger than 2")
        textColor = it.getColor(R.styleable.SimplePickerView_spvTextColor, Color.BLACK)

        lineStrokeWidth = it.getDimension(
            R.styleable.SimplePickerView_spvLineWidth,
            resources.displayMetrics.density
        )
        lineColor = it.getColor(R.styleable.SimplePickerView_spvLineColor, Color.TRANSPARENT)
        backColor = it.getColor(R.styleable.SimplePickerView_spvBackgroundColor, Color.TRANSPARENT)
        textSize = it.getDimension(
            R.styleable.SimplePickerView_spvTextSize,
            18 * resources.displayMetrics.density
        )
        it.recycle()

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                listener?.invoke((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition())
            }

        })
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        val sizeHeight = MeasureSpec.getSize(heightSpec)
        measureChildren(widthSpec, heightSpec)
        cellHeight = sizeHeight / showSize
    }

    /**
     * 设置数据
     */
    fun setData(list: List<String>, position: Int, scrollBack: (Int) -> Unit) {
        post {
            listener = scrollBack
            adapter = SimplePickerAdapter(list, showSize, textColor, textSize, cellHeight)
            scrollToPosition(position + showSize / 2)
            scrollBack(position)
        }
    }

    private val paint = Paint()
    private val rectF = RectF()
    override fun onDraw(canvas: Canvas?) {
        drawListener?.drawBelow(canvas, measuredWidth, measuredHeight, cellHeight)
        rectF.set(
            0f,
            (measuredHeight - cellHeight) / 2f,
            measuredWidth / 1f,
            (measuredHeight + cellHeight) / 2f
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

        drawListener?.drawOver(canvas, measuredWidth, measuredHeight, cellHeight)
    }


    class SimplePickerAdapter(
        val array: List<String>,
        val showSize: Int,
        val textcolor: Int,
        val textsize: Float,
        val itemHeight: Int
    ) :
        Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(TextView(parent.context).apply {
                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
                gravity = Gravity.CENTER
                setTextColor(textcolor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize)
            }) {}
        }

        override fun getItemCount(): Int {
            return array.size + showSize - 1
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder.itemView as TextView).text = when (position) {
                in (0 until showSize / 2) -> ""
                in itemCount - showSize / 2 until itemCount -> ""
                else -> array[position - showSize / 2]
            }
        }
    }

}