package top.leefeng.date

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import top.leefeng.datepicker.PickerAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        simplePicker.layoutManager = LinearLayoutManager(this)

//        datePickerView.setDate("1995-01-02","2021-05-05","2021-02-03")
    }
}