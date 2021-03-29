package top.leefeng.datepicker

import android.util.Log

/**
 *
 * Created by lilifeng on 3/29/21
 *
 * Copyright www.putaoabc.com
 *
 */
val String.p: Any
    get() {
        if (BuildConfig.DEBUG)
            Log.i("DataPicker", this)
        return this
    }