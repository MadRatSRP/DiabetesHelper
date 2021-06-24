package com.madrat.diabeteshelper.ui.diabetesstatistics

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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
import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.streams.toList


class FragmentDiabetesStatistics: Fragment() {
    private var nullableBinding: FragmentDiabetesStatisticsBinding? = null
    private val binding get() = nullableBinding!!
    val args: FragmentDiabetesStatisticsArgs by navArgs()
    
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
        initializeGraphics(view, args.diabetesNotes.toCollection(ArrayList()))
        return view
    }
    
    fun initializeGraphics(view: View, glucoseList: ArrayList<DiabetesNote>) {
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
        val collection = glucoseList.groupBy {it.noteDate}.entries
        
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
    
    fun getAverageFromList(list: List<DiabetesNote>): Double {
        val listOfDoubles = ArrayList<Double>()
        list.forEach {
            listOfDoubles.add(it.glucoseLevel)
        }
        return listOfDoubles.average()
    }
    
    fun getMinFromList(list: List<DiabetesNote>): Double? {
        val listOfDoubles = ArrayList<Double>()
        list.forEach {
            listOfDoubles.add(it.glucoseLevel)
        }
        return listOfDoubles.minOrNull()
    }
    
    fun getMaxFromList(list: List<DiabetesNote>): Double? {
        val listOfDoubles = ArrayList<Double>()
        list.forEach {
            listOfDoubles.add(it.glucoseLevel)
        }
        return listOfDoubles.maxOrNull()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        binding.buttonChangeDate.setOnClickListener {
            val diabetesNotes = args.diabetesNotes.toCollection(ArrayList())
            val listOfDates = ArrayList<String>()
            val groupedNotes = diabetesNotes.groupBy {it.noteDate}.entries
            groupedNotes.forEach {
                listOfDates.add(it.key)
            }
            
            changeDateClicklistener(listOfDates)
        }
    }
    
    fun changeDateClicklistener(listOfDates: ArrayList<String>) {
        val now: Calendar = Calendar.getInstance()
        val dpd: DatePickerDialog = DatePickerDialog.newInstance(
            { view, year, monthOfYear, dayOfMonth ->
                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru","RU"))
                /*val date = "You picked the following date: " + dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year
                binding.setupDate.text = date*/
                val calendar = java.util.Calendar.getInstance()
                /*calendar.time = simpleDateFormat.parse(it)
                calendars.add(calendar)*/
                calendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = simpleDateFormat.format(
                    calendar.time
                )
                println("ВЫБРАННАЯ ДАТА:" + selectedDate)
                calculateGlucoseMinAndMax(selectedDate)
            },
            now.get(Calendar.YEAR),  // Initial year selection
            now.get(Calendar.MONTH),  // Initial month selection
            now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        )
        dpd.selectableDays = formCalendarArrayFromDates(
            listOfDates
        )
        dpd.show(childFragmentManager, "Datepickerdialog")
    }
    
    fun formCalendarArrayFromDates(listOfDates: ArrayList<String>): Array<java.util.Calendar> {
        val calendars = ArrayList<java.util.Calendar>()
        val simpleDateFormat = SimpleDateFormat("dd.MM.yy", Locale("ru","RU"))
    
        listOfDates.forEach {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = simpleDateFormat.parse(it)
            calendars.add(calendar)
        }
        
        return calendars.toTypedArray()
    }
    
    fun calculateGlucoseMinAndMax(noteDate: String) {
        val diabetesNotes = args.diabetesNotes.toCollection(ArrayList())
        val groupedNotes = diabetesNotes.groupBy {it.noteDate}.entries
        val filteredSet = groupedNotes.stream().filter {
            it.key == noteDate
        }
        
        val listOfNotes = filteredSet.toList()[0].value
        
        val minGlucose: Double? = getMinFromList(
            listOfNotes
        )
        val maxGlucose: Double? = getMaxFromList(
            listOfNotes
        )
        
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