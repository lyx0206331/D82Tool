package com.chwishay.d82tool.tools

import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.annotation.ColorInt
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2021/3/11 0011 10:19
 * description:
 */
fun TextView.setScrollable() {
    this.movementMethod = ScrollingMovementMethod.getInstance()
}

suspend fun TextView.appendDataAutoScroll(data: String?, maxLength: Int = 4000) {
    if (data == null) return
    else {
        withContext(Dispatchers.Main) {
            if (text.length > maxLength) {
                text = ""
            }
            append("$data\n")
            val contentHeight = lineCount * lineHeight
            if (contentHeight > height) {
                scrollTo(0, contentHeight - height)
            }
        }
    }
}

fun LineChart.init() {
    this.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {}

        override fun onNothingSelected() {}
    })
    this.description.isEnabled = true
    this.setTouchEnabled(true)
    this.isDragEnabled = true
    this.setScaleEnabled(true)
    this.setDrawGridBackground(false)
    this.setPinchZoom(true)
//    this.setBackgroundColor(Color.WHITE)
    this.data = LineData().apply { setValueTextColor(Color.GRAY) }
    this.legend.apply {
        form = Legend.LegendForm.LINE
        textColor = Color.GRAY
    }
    this.xAxis.apply {
        textColor = Color.GRAY
        setDrawGridLines(false)
        setAvoidFirstLastClipping(false)
        isEnabled = true
    }
//            it.axisLeft.apply {
//                textColor = Color.WHITE
//                axisMaximum = 40f
//                axisMinimum = -40f
//                setDrawGridLines(true)
//            }
    //根据数据自动缩放展示最大最小值,不能设置axisLeft,否则自动缩放无效
    this.isAutoScaleMinMaxEnabled = true
    this.axisRight.isEnabled = false
    this.description.isEnabled = false
}

fun LineChart.drawSet(chartDatas: List<ChartAttr>) = data?.let { d ->

    fun getDataSet(index: Int, @ColorInt color: Int, name: String) =
        d.getDataSetByIndex(index) ?: LineDataSet(null, name).also { lds ->
            lds.axisDependency = YAxis.AxisDependency.LEFT
            lds.color = color
            lds.setCircleColor(Color.GRAY)
            lds.lineWidth = 1f
            lds.circleRadius = 2f
            lds.fillAlpha = 65
            lds.fillColor = color
            lds.highLightColor = Color.rgb(244, 177, 177)
            lds.valueTextColor = Color.GRAY
            lds.valueTextSize = 9f
            lds.setDrawValues(false)
            lds.setDrawCircles(false)
            d.addDataSet(lds)
        }

    chartDatas.forEachIndexed { index, chartAttr ->
//        "index".logE("index:$index, viewIndex:${chartAttr.index}")
        val valueSet = getDataSet(index, chartAttr.color, chartAttr.name)
        d.addEntry(Entry(valueSet.entryCount.toFloat(), chartAttr.value), index)
    }
    d.notifyDataChanged()

    this.notifyDataSetChanged()

    this.setVisibleXRangeMaximum(1000f)

    this.moveViewToX(d.entryCount.toFloat())
}

class ChartAttr(val index: Int = -1, val value: Float = 0f, @ColorInt val color: Int = 0, val name: String = "")