package br.edu.puccampinas.starfocusapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.app.AlertDialog
import android.graphics.Paint
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.Spinner
import br.edu.puccampinas.starfocusapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    // Variáveis scroll mensal
    private lateinit var binding: FragmentHomeBinding
    private var calendar = Calendar.getInstance()
    private var currentDayPosition = -1
    private var isScrollAdjusted = false

    private var selectedDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    private var selectedMonth: Int = calendar.get(Calendar.MONTH)
    private var selectedYear: Int = calendar.get(Calendar.YEAR)

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Atualiza o botão de calendário com o mês e ano atuais
        updateCalendarButtonText()

        // Exibe o seletor de mês e ano
        binding.ButtonCalendar.setOnClickListener {
            showMonthYearPickerDialog()
        }

        // Adiciona os dias ao layout
        addDaysToView(calendar)

        // Ajusta a rolagem para o dia atual
        binding.BarDaysScroll.post {
            if (!isScrollAdjusted && currentDayPosition != -1) {
                val targetPosition = (currentDayPosition - 2).coerceAtLeast(1) - 1
                val dayView = binding.linearLayoutDays.getChildAt(targetPosition)
                binding.BarDaysScroll.scrollTo(dayView.left, 0)
                isScrollAdjusted = true
            }
        }

        // Ação de adicionar nova tarefa
        binding.InputTask.setOnClickListener {
            val bottomSheetFragment = BottomsSheetAddTaskFragment {
                // Chama a função que recarrega as tarefas
                loadTasksForSelectedDay(String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear))
            }.apply {
                arguments = Bundle().apply {
                    putInt("diaSelecionado", selectedDay)
                    putInt("mesSelecionado", selectedMonth + 1)
                    putInt("anoSelecionado", selectedYear)
                }
            }
            bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)
        }

        // Carregar as tarefas para o dia selecionado ao iniciar o fragmento
        if (auth.currentUser != null) {
            loadTasksForSelectedDay(String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear))
        }

        return binding.root
    }

    private fun addDaysToView(calendar: Calendar) {
        binding.linearLayoutDays.removeAllViews()
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
            binding.linearLayoutDays.addView(dayLayout)
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
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedYear = calendar.get(Calendar.YEAR)
        addDaysToView(calendar)  // Atualiza a exibição do calendário com o novo dia selecionado
        val selectedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
        loadTasksForSelectedDay(selectedDate) // Carrega as tarefas do novo dia selecionado
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
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        builder.setPositiveButton("OK") { _, _ ->
            // Atualiza o calendário com o mês e ano selecionados
            selectedMonth = monthSpinner.selectedItemPosition
            selectedYear = yearPicker.value
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            // Atualiza a exibição dos dias e o botão de calendário
            addDaysToView(calendar)
            updateCalendarButtonText()
        }

        builder.show()
    }

    private fun updateCalendarButtonText() {
        val month = getMonthName(selectedMonth)
        val year = selectedYear
        binding.ButtonCalendar.text = "$month/$year"
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

    private fun loadTasksForSelectedDay(selectedDate: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtém as tarefas organizadas por data no formato esperado
                    val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>>

                    if (dataTarefas != null) {
                        // Seleciona as tarefas do dia específico
                        val tarefasDoDia = dataTarefas[selectedDate]?.mapNotNull { tarefa ->
                            val texto = tarefa["texto"] as? String
                            val concluido = tarefa["concluido"] as? Boolean ?: false
                            Pair(texto, concluido) // Retorna um par com o texto e o status de conclusão
                        } ?: emptyList()

                        Log.d("Firestore", "Tarefas para $selectedDate: $tarefasDoDia")
                        displayTasks(tarefasDoDia, selectedDate)
                    } else {
                        Log.d("Firestore", "Campo 'tarefas' está vazio ou não encontrado")
                    }
                } else {
                    Log.d("Firestore", "Documento não encontrado")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao carregar tarefas", e)
            }
    }

    private fun displayTasks(tarefas: List<Pair<String?, Boolean>>, selectedDate: String) {
        binding.TaskContainer.removeAllViews() // Limpa tarefas anteriores

        val userId = auth.currentUser?.uid ?: return

        tarefas.forEach { (tarefaTexto, concluido) ->
            if (tarefaTexto != null) {
                val tarefaView = RadioButton(requireContext()).apply {
                    text = tarefaTexto
                    textSize = 20f
                    gravity = Gravity.CENTER_VERTICAL
                    isAllCaps = false

                    setBackgroundResource(R.drawable.rectangleaddtask)
                    setTextColor(Color.parseColor("#3D3D3D"))

                    buttonDrawable = null // Remove o botão padrão do RadioButton
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0) // Adiciona o drawable à esquerda do texto
                    compoundDrawablePadding = 37 // Ajusta o espaço entre o texto e o botão
                    setPadding(70, 10, 60, 10) // Ajusta o padding para o texto e o botão à direita

                    isChecked = concluido // Define o estado do RadioButton conforme o campo 'concluido' da tarefa

                    paintFlags = if (concluido) {
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_24, 0, 0, 0) // Drawable de tarefa concluída
                        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0) // Drawable de tarefa não concluída
                        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }

                    setOnClickListener {
                        if (!concluido) { // Só marca como concluído se ainda não estiver
                            updateTaskStatus(userId, selectedDate, tarefaTexto)
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            isChecked = true
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_24, 0, 0, 0) // Muda o drawable para indicar conclusão
                        }
                    }
                }

                val layoutParams = LinearLayout.LayoutParams(
                    binding.InputTask.width, //mesmo tamanho do botao
                    binding.InputTask.height //mesmo tamanho do botao
                ).apply {
                    setMargins(16, 0, 16, 0) // Margens horizontais para espaçamento
                }
                tarefaView.layoutParams = layoutParams
                binding.TaskContainer.addView(tarefaView)
            }
        }

        // Remove o botão 'InputTask' do seu pai, se ele já tiver um
        val inputTaskParent = binding.InputTask.parent
        if (inputTaskParent is ViewGroup) {
            inputTaskParent.removeView(binding.InputTask)
        }

        // Adiciona o botão 'InputTask'
        binding.TaskContainer.addView(binding.InputTask)

        // Remove o botão 'buttonProgress' do seu pai, se ele já tiver um
        val buttonProgressParent = binding.buttonProgress.parent
        if (buttonProgressParent is ViewGroup) {
            buttonProgressParent.removeView(binding.buttonProgress)
        }

        // Adiciona o botão 'buttonProgress' logo após o 'InputTask'
        binding.TaskContainer.addView(binding.buttonProgress)
    }

    private fun updateTaskStatus(userId: String, dataSelecionada: String, tarefaTexto: String) {
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Encontra a tarefa com o texto correspondente e atualiza o campo "concluido" para true
            for (tarefa in tarefasDoDia) {
                if (tarefa["texto"] == tarefaTexto) {
                    tarefa["concluido"] = true
                    break
                }
            }

            // Atualiza o campo "tarefas" no Firestore com a modificação
            dataTarefas[dataSelecionada] = tarefasDoDia
            tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                Log.d("Firestore", "Tarefa marcada como concluída com sucesso.")
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao marcar tarefa como concluída", e)
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

}