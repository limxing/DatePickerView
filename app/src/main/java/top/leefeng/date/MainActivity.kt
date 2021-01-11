package top.leefeng.date

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import top.leefeng.datepicker.PickerAdapter
import top.leefeng.datepicker.PickerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = listOf("北京市","天津市","上海市","河北省","山东省","河南省","辽宁省","江苏省","安徽省")
        simplePicker.setData(list,3){
            simpleResult.text = list[it]
        }

        datePickerView.listener = {
            dateResult.text = "${it.toList()}"
        }
        datePickerView.setDate("1990-01-01", "2021-01-09", "1999-09-09")
        datePickerView.drawListener = object : PickerView.DrawListener {

            /**
             * width recyclerView 宽
             * height recyclerView 高
             * cellHeight 选中View 高
             */
            override fun drawBelow(canvas: Canvas?, width: Int, height: Int, cellHeight: Int) {
                //按需要画背景
            }

            override fun drawOver(canvas: Canvas?, width: Int, height: Int, cellHeight: Int) {
                //按需要画前景。（有同学可能需要上下覆盖渐变效果，可在此实现,避免在此创建对象）
            }

        }
    }
}