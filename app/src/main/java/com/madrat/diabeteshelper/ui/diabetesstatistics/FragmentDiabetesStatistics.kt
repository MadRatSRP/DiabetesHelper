package com.madrat.diabeteshelper.ui.diabetesstatistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentDiabetesStatisticsBinding
import kotlin.collections.ArrayList


class FragmentDiabetesStatistics: Fragment() {
    private var nullableBinding: FragmentDiabetesStatisticsBinding? = null
    private val binding get() = nullableBinding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nullableBinding = FragmentDiabetesStatisticsBinding.inflate(
            inflater,
            container,
            false
        )
        val view = binding.root
        val glucoseList = arrayListOf(
            Example(10.0, "15.12.1996"),
            Example(150.2, "15.12.1996"),
            Example(30.0, "15.12.1996"),
            Example(3.33, "05.06.1991"),
            Example(3.53, "05.06.1991"),
            Example(3.73, "05.06.1991"),
            Example(8.88, "01.01.1988")
        )
        initializeGraphics(view, glucoseList)
        return view
    }
    
    fun initializeGraphics(view: View, glucoseList: ArrayList<Example>) {
        val anyChartView: AnyChartView = view.findViewById(R.id.any_chart_view)
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar))
    
        val cartesian = AnyChart.line()
    
        cartesian.animation(true)
    
        cartesian.padding(10.0, 20.0, 5.0, 20.0)
    
        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)
    
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
    
        val seriesData: MutableList<DataEntry> = ArrayList()
        val collection = glucoseList.groupBy {it.date}.entries
        
        collection.forEach {
            seriesData.add(
                ValueDataEntry(
                    it.key,
                    getAverageFromList(it.value)
                )
            )
        }
    
        val set: Set = Set.instantiate()
        set.data(seriesData)
        val series3Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value' }")
    
        val series3: Line = cartesian.line(series3Mapping)
        series3.name("Уровень глюкозы в крови")
        series3.hovered().markers().enabled(true)
        series3.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series3.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
    
        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)
    
        anyChartView.setChart(cartesian)
    }
    
    fun getAverageFromList(list: List<Example>): Double {
        val listOfDoubles = ArrayList<Double>()
        list.forEach {
            listOfDoubles.add(it.value)
        }
        return listOfDoubles.average()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val glucoseList = arrayOf(2.22, 3.33, 4.44, 5.55, 6.66, 7.77)
        calculateMinAndMaxGlucode(glucoseList)
    }
    
    fun calculateMinAndMaxGlucode(list: Array<Double>) {
        
        val minGlucose: Double? = list.minOrNull()
        val maxGlucose: Double? = list.maxOrNull()
        if (minGlucose != null && maxGlucose != null) {
            binding.glucoseMin.text = minGlucose.toString()
            binding.glucoseMax.text = maxGlucose.toString()
        }
    }
}

class Example(
    val value: Double,
    val date: String
)