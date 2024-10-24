package br.edu.puccampinas.starfocusapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner

class HomeFragment : Fragment() {
    private lateinit var linearLayoutDays: LinearLayout
    private lateinit var scrollView: HorizontalScrollView
    private var calendar = Calendar.getInstance()

    private var currentDayPosition = -1
    private var isScrollAdjusted = false
    private var selectedDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

    private lateinit var buttonCalendar: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        linearLayoutDays = view.findViewById(R.id.linearLayoutDays)
        scrollView = view.findViewById(R.id.BarDaysScroll)
        buttonCalendar = view.findViewById(R.id.ButtonCalendar)

        updateCalendarButtonText()

        buttonCalendar.setOnClickListener {
            showMonthYearPickerDialog()
        }

        addDaysToView(calendar)

        scrollView.post {
            if (!isScrollAdjusted && currentDayPosition != -1) {
                val targetPosition = (currentDayPosition - 2).coerceAtLeast(1) - 1
                val dayView = linearLayoutDays.getChildAt(targetPosition)
                scrollView.scrollTo(dayView.left, 0)
                isScrollAdjusted = true
            }
        }

        return view
    }

    private fun addDaysToView(calendar: Calendar) {
        linearLayoutDays.removeAllViews()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        currentDayPosition = calendar.get(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            val dayLayout = LinearLayout(requireContext())
            dayLayout.orientation = LinearLayout.VERTICAL

            val dayOfWeekTextView = TextView(requireContext()).apply {
                text = getDayOfWeekSymbol(calendar, day)
                textSize = 12f
                gravity = Gravity.CENTER
            }

            val dayNumberTextView = TextView(requireContext()).apply {
                text = "$day"
                textSize = 22f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#3D3D3D"))
                if (day == selectedDay) {
                    setTextColor(Color.WHITE)
                    background = resources.getDrawable(R.drawable.circle_selected_day, null)
                    setPadding(12, 0, 12, 0)
                }
            }

            dayNumberTextView.setOnClickListener { selectDay(day) }
            dayLayout.setOnClickListener { selectDay(day) }

            dayLayout.addView(dayOfWeekTextView)
            dayLayout.addView(dayNumberTextView)
            dayLayout.setBackgroundColor(Color.TRANSPARENT)
            dayLayout.setPadding(40, 16, 40, 16)
            linearLayoutDays.addView(dayLayout)
        }
    }

    private fun getDayOfWeekSymbol(calendar: Calendar, day: Int): String {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "S"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "Q"
            Calendar.THURSDAY -> "Q"
            Calendar.FRIDAY -> "S"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "D"
            else -> ""
        }
    }

    private fun selectDay(day: Int) {
        selectedDay = day
        addDaysToView(calendar)
    }

    private fun showMonthYearPickerDialog() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecionar Mês e Ano")

        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        builder.setView(dialogView)

        val monthSpinner = dialogView.findViewById<Spinner>(R.id.monthSpinner)
        val months = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter
        monthSpinner.setSelection(currentMonth)

        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 2000
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        builder.setPositiveButton("OK") { _, _ ->
            calendar.set(Calendar.MONTH, monthSpinner.selectedItemPosition)
            calendar.set(Calendar.YEAR, yearPicker.value)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            addDaysToView(calendar)
            updateCalendarButtonText()
        }

        builder.show()
    }

    private fun updateCalendarButtonText() {
        val month = getMonthName(calendar.get(Calendar.MONTH))
        val year = calendar.get(Calendar.YEAR)
        buttonCalendar.text = "$month/$year"
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Fev"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Abr"
            Calendar.MAY -> "Mai"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Ago"
            Calendar.SEPTEMBER -> "Set"
            Calendar.OCTOBER -> "Out"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dez"
            else -> ""
        }
    }
}
