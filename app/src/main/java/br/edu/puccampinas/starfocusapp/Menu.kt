package br.edu.puccampinas.starfocusapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner

class Menu : AppCompatActivity() {
    private lateinit var linearLayoutDays: LinearLayout
    private lateinit var scrollView: HorizontalScrollView
    private var calendar = Calendar.getInstance()

    // Variáveis auxiliares para controle do scroll
    private var currentDayPosition = -1
    private var isScrollAdjusted = false
    private var selectedDay: Int = calendar.get(Calendar.DAY_OF_MONTH) // Inicializa com o dia atual

    // Variável do botão de mês/ano
    private lateinit var buttonCalendar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        linearLayoutDays = findViewById(R.id.linearLayoutDays)
        scrollView = findViewById(R.id.BarDaysScroll)

        buttonCalendar = findViewById(R.id.ButtonCalendar)

        // Atualiza o texto do botão com o mês e ano atuais
        updateCalendarButtonText()

        // Configura o evento de clique para o botão
        buttonCalendar.setOnClickListener {
            showMonthYearPickerDialog()
        }

        // Carrega todos os dias do mês de uma vez
        addDaysToView(calendar)

        // Ajustar o scroll para a posição correta apenas na primeira vez
        scrollView.post {
            if (!isScrollAdjusted && currentDayPosition != -1) {
                val targetPosition = (currentDayPosition - 2).coerceAtLeast(1) - 1
                val dayView = linearLayoutDays.getChildAt(targetPosition)
                scrollView.scrollTo(dayView.left, 0)
                isScrollAdjusted = true // Marca que o ajuste foi feito
            }
        }
    }

    private fun addDaysToView(calendar: Calendar) {
        linearLayoutDays.removeAllViews() // Limpa os dias atuais
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Obter o dia atual
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Armazena a posição do dia atual
        currentDayPosition = currentDay

        // Adiciona todos os dias do mês ao layout
        for (day in 1..daysInMonth) {
            // Cria um LinearLayout para cada dia
            val dayLayout = LinearLayout(this)
            dayLayout.orientation = LinearLayout.VERTICAL

            // Cria o TextView para o dia da semana
            val dayOfWeekTextView = TextView(this)
            dayOfWeekTextView.text = getDayOfWeekSymbol(calendar, day)
            dayOfWeekTextView.textSize = 12f
            dayOfWeekTextView.gravity = Gravity.CENTER

            // Cria o TextView para o número do dia
            val dayNumberTextView = TextView(this)
            dayNumberTextView.text = "$day"
            dayNumberTextView.textSize = 22f
            dayNumberTextView.gravity = Gravity.CENTER
            dayNumberTextView.setTextColor(Color.parseColor("#3D3D3D")) // Cor padrão para dias não selecionados

            // Define a cor do dia selecionado
            if (day == selectedDay) {
                // Dia selecionado: texto branco
                dayNumberTextView.setTextColor(Color.WHITE)
                dayNumberTextView.background = resources.getDrawable(R.drawable.circle_selected_day, null)
                dayNumberTextView.setPadding(12, 0, 12, 0) // Adiciona padding para o círculo
            } else {
                dayNumberTextView.background = null // Sem fundo se não for selecionado
            }

            // Define um clique no TextView para selecionar o dia
            dayNumberTextView.setOnClickListener {
                selectDay(day) // Seleciona o dia ao clicar
            }

            // Define um clique no layout do dia para selecionar o dia
            dayLayout.setOnClickListener {
                selectDay(day) // Seleciona o dia ao clicar no layout
            }

            // Adiciona os TextViews ao layout do dia
            dayLayout.addView(dayOfWeekTextView)
            dayLayout.addView(dayNumberTextView)

            // Define o layout sem fundo e com padding
            dayLayout.setBackgroundColor(Color.TRANSPARENT)
            dayLayout.setPadding(40, 16, 40, 16)

            linearLayoutDays.addView(dayLayout)
        }
    }

    // Método para obter o símbolo do dia da semana
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

    // Método para selecionar um dia
    private fun selectDay(day: Int) {
        selectedDay = day
        addDaysToView(calendar) // Atualiza a visualização para refletir a nova seleção
    }

    private fun showMonthYearPickerDialog() {
        // Obter o ano e mês atuais
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // Já está correto, pois é o índice do mês

        // Cria um AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar Mês e Ano")

        // Cria o layout para o diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        builder.setView(dialogView)

        // Cria o Spinner para os meses
        val monthSpinner = dialogView.findViewById<Spinner>(R.id.monthSpinner)
        val months = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter
        monthSpinner.setSelection(currentMonth) // Define o mês atual

        // Cria o NumberPicker para o ano
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 2000 // Ano mínimo
        yearPicker.maxValue = 2100 // Ano máximo
        yearPicker.value = currentYear // Define o ano atual

        // Botão de confirmação
        builder.setPositiveButton("OK") { _, _ ->
            // Atualiza o calendário com o mês e ano selecionados
            calendar.set(Calendar.MONTH, monthSpinner.selectedItemPosition) // Mês do Spinner
            calendar.set(Calendar.YEAR, yearPicker.value) // Ano do NumberPicker
            calendar.set(Calendar.DAY_OF_MONTH, 1) // Defina o primeiro dia do mês para evitar problemas

            // Atualiza a visualização após selecionar o mês e o ano
            addDaysToView(calendar)

            // Atualiza o texto do botão com o novo mês e ano
            updateCalendarButtonText()
        }
        // Exibe o AlertDialog
        builder.show()
    }

    // Método para atualizar o texto do botão com o mês e ano atuais
    private fun updateCalendarButtonText() {
        val month = getMonthName(calendar.get(Calendar.MONTH)) // Obtém o nome do mês
        val year = calendar.get(Calendar.YEAR)
        buttonCalendar.text = "$month/$year" // Exibe o mês/ano no formato desejado
    }

    // Método para converter o número do mês em nome abreviado
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