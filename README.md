# Android 日期选择器 滚轮样式的选择器
DatePickerView

<img src="./screen_record_date.GIF" width="419" height="387"/>

PickerView  inside。

<img src="./screen_record_simple.GIF" width="419" height="387"/>


## Usage
#### Gradle

Project level build.gradle
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
App level build.gradle
```
dependencies {
    
}
```

### XML
```
<top.leefeng.datepicker.DatePickerView
    android:id="@+id/datePickerView"
    android:layout_width="match_parent"
    android:layout_height="300dp"
   />

```
### Attribute
```
app:dpvBackgroundColor="#ffffff"        选中（中间条目）的背景色，默认透明
app:dpvDateEnd="2021-05-05"             截止日期，默认今天
app:dpvDatePaddingEnd="75dp"            右边距，默认0
app:dpvDatePaddingStart="75dp"          左边距，默认0
app:dpvDatePosition="2021-02-03"        定位日期，默认截止日期
app:dpvDateSize="7"                     当前课件范围内显示日期的个数，默认5          
app:dpvDateStar="1995-01-02"            开始日期，默认 1970-01-01
app:dpvDateTextSize="24dp"              日期文字大小，默认 20dp
app:dpvLineColor="#33000000"            选中（中间条目）上下边界线颜色，默认透明
app:dpvLineWidth="1dp"                  选中（中间条目）上下边界线宽度，默认1dp
app:dpvUnitMarginStart="10dp"           如果年月日单位固定不滚动，设置单位与数字之间的距离，默认0
app:dpvUnitScroll="true"                是否设置单位（年月日）与数字一起滚动，默认false
app:dpvUnitTextColor="@color/black"     设置单位（年月日）文本颜色，默认 黑色
app:dpvUnitTextSize="20dp"              设置单位（年月日）文本大小，默认 18dp
```


### Code

kotlin
```kotlin

//监听回调闭包
datePickerView.listener = {
     //[year,month,day]
}

//起止日期及当前定位日期
datePickerView.setDate("1990-01-01","2021-01-09","1999-09-09")

//需要进一步加工
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
```
SimplePickerView
```kotlin
 val list = listOf("北京市","天津市","上海市","河北省","山东省","河南省","辽宁省","江苏省","安徽省")
simplePicker.setData(list,3){
    //position
}
//进一步加工
simplePickerView.drawListener  //... 同上
```
java
```java
//什么？都2021年了你还在使用JAVA。Kotlin与JAVA 兼容，请自行Google。
```

### 实现原理

> Android发展的这么多年，很多Android开发者包括我面对做一个与IOS滚轮选择器UI时，各种搜索，
不难发现很多前辈为此付出了不少努力。有反编译别人apk的，有继承View自定义的，简单点的使用ListView
或Recyclerview在其上画一个渐变蒙层。结果就是要么有BUG的，要么与UI不符，还有的滑动时错位的。
> 我也是一个比较懒的人。不想全部自定义View，毕竟列表这种事情,系统SDK控件Recyclerview已经做的不是很好了吗！
> 因此，我就在RecyclerView上下手找到突破口。

首先我们分析一下：




#### License
DatePickerView and PickerView is available under the MIT license.