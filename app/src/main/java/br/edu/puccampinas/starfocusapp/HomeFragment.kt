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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.Spinner
import br.edu.puccampinas.starfocusapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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

    private var selectedDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Configura o botão 'sendProgress' para estar indisponível inicialmente
        binding.sendProgress.isEnabled = false
        binding.sendProgress.alpha = 0.7f // opacidade para indicar indisponibilidade

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

        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getString("selected_date")
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

        // Evento de clique para o botão sendProgress
        binding.sendProgress.setOnClickListener {
            val userId = auth.currentUser?.uid
            val dataSelecionada = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            if (userId != null) {
                sendProgress(userId, dataSelecionada)
            }
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
                setTextColor(Color.parseColor("#3D3D3D"))
            }

            val dayNumberTextView = TextView(requireContext()).apply {
                text = "$day"
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 0)
                setTextColor(Color.parseColor("#3D3D3D"))

                if (day == selectedDay) {
                    setTextColor(Color.WHITE)
                    background = resources.getDrawable(R.drawable.circle_selected_day, null)
                    setPadding(0, 0, 0, 0)
                }

                // Verifica se é o dia atual e aplica o fundo amarelo
                if (day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) && calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                    background = resources.getDrawable(R.drawable.circle_today_day, null)
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
            loadTasksForSelectedDay(String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear))
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

    // Função atualizada para carregar tarefas e atualizar o dia selecionado
    public fun loadTasksForSelectedDay(selectedDate: String) {
        val userId = auth.currentUser?.uid ?: return

        // Divide a data selecionada para obter o dia, mês e ano
        val parts = selectedDate.split("-")
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1 // Janeiro é 0, então subtrai 1
        val year = parts[2].toInt()

        // Atualiza as variáveis selecionadas
        selectedDay = day
        selectedMonth = month
        selectedYear = year

        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtém as tarefas organizadas por data no formato esperado
                    val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>>

                    if (dataTarefas != null) {
                        // Seleciona as tarefas do dia específico
                        val tarefasDoDia = dataTarefas[selectedDate]?.mapNotNull { tarefa ->
                            val id = tarefa["id"] as? String
                            val texto = tarefa["texto"] as? String
                            val status = tarefa["status"] as? String ?: "Pendente"
                            Triple(id, texto, status) // Retorna um triple com o ID, texto e status
                        } ?: emptyList()

                        Log.d("Firestore", "Tarefas para $selectedDate: $tarefasDoDia")
                        displayTasks(tarefasDoDia, selectedDate)

                        // Atualiza o calendário com o novo dia selecionado
                        addDaysToView(calendar)
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

    private fun displayTasks(tarefas: List<Triple<String?, String?, String>>, selectedDate: String) {
        binding.TaskContainer.removeAllViews() // Limpa tarefas anteriores

        val userId = auth.currentUser?.uid ?: return
        val today = Calendar.getInstance()
        val selectedDateCalendar = Calendar.getInstance().apply {
            val parts = selectedDate.split("-")
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1) // Janeiro é 0
            set(Calendar.YEAR, parts[2].toInt())
        }

        val isPastDate = selectedDateCalendar.before(today)

        tarefas.forEach { (tarefaId, tarefaTexto, status) ->
            if (tarefaId != null && tarefaTexto != null) {
                val concluido = status == "Concluída"
                val enviada = status == "Enviada"  // Verifica se a tarefa está com status "Enviada"
                val tarefaView = RadioButton(requireContext()).apply {
                    text = tarefaTexto
                    textSize = 20f
                    gravity = Gravity.CENTER_VERTICAL
                    isAllCaps = false

                    setBackgroundResource(R.drawable.rectangleaddtask)
                    setTextColor(Color.parseColor("#3D3D3D"))

                    buttonDrawable = null // Remove o botão padrão do RadioButton
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

                    isEnabled = !isPastDate
                    if (isPastDate) {
                        setTextColor(Color.GRAY) // Altera a cor do texto para indicar que está desabilitado
                    }

                    setOnClickListener {
                        if (!concluido) { // Só marca como concluído se ainda não estiver
                            updateTaskStatusTrue(userId, selectedDate, tarefaTexto)
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            isChecked = true
                        }
                        else -> { // Visual para tarefas pendentes
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0)
                            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }

                    // Configura o clique somente se a tarefa não estiver "Enviada"
                    if (!enviada) {
                        setOnClickListener {
                            if (!concluido) { // Só marca como concluído se ainda não estiver
                                updateTaskStatusConcluida(userId, selectedDate, tarefaId)
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                isChecked = true
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_24, 0, 0, 0)

                                // Exibe o drawable de conclusão com animação
                                binding.feedback1.apply {
                                    alpha = 0f
                                    visibility = View.VISIBLE
                                    animate().alpha(1f).setDuration(300).start()
                                }

                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.feedback1.animate()
                                        .alpha(0f)
                                        .setDuration(300)
                                        .withEndAction {
                                            binding.feedback1.visibility = View.GONE
                                        }
                                        .start()
                                }, 3000)
                            } else {
                                updateTaskStatusPendente(userId, selectedDate, tarefaId)
                                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                isChecked = false
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0)
                            }
                        }
                    }
                }

                val layoutParams = LinearLayout.LayoutParams(
                    binding.InputTask.width,
                    binding.InputTask.height
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
                tarefaView.layoutParams = layoutParams
                binding.TaskContainer.addView(tarefaView)

                if (concluido) {
                    hasConcludedTask = true
                }
            }
        }

        if (hasConcludedTask) {
            binding.sendProgress.isEnabled = true
            binding.sendProgress.alpha = 1f
        } else {
            binding.sendProgress.isEnabled = false
            binding.sendProgress.alpha = 0.7f
        }

        val inputTaskParent = binding.InputTask.parent
        if (inputTaskParent is ViewGroup) {
            inputTaskParent.removeView(binding.InputTask)
        }
        binding.TaskContainer.addView(binding.InputTask)

        val buttonProgressParent = binding.buttonProgress.parent
        if (buttonProgressParent is ViewGroup) {
            buttonProgressParent.removeView(binding.buttonProgress)
        }
        binding.TaskContainer.addView(binding.buttonProgress)
    }

    private fun updateTaskStatusConcluida(userId: String, dataSelecionada: String, tarefaId: String) {
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Encontra a tarefa com o id correspondente e atualiza o campo "status" para "Concluída"
            for (tarefa in tarefasDoDia) {
                if (tarefa["id"] == tarefaId) {
                    tarefa["status"] = "Concluída"
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

    private fun updateTaskStatusPendente(userId: String, dataSelecionada: String, tarefaId: String) {
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Encontra a tarefa com o id correspondente e atualiza o campo "status" para "Pendente"
            for (tarefa in tarefasDoDia) {
                if (tarefa["id"] == tarefaId) {
                    tarefa["status"] = "Pendente"
                    break
                }
            }

            // Atualiza o campo "tarefas" no Firestore com a modificação
            dataTarefas[dataSelecionada] = tarefasDoDia
            tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                Log.d("Firestore", "Tarefa marcada como pendente com sucesso.")
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao marcar tarefa como pendente", e)
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

    // Função modificada para atualizar o status de várias tarefas como "Enviada"
    private fun updateTaskStatusEnviada(userId: String, dataSelecionada: String, tarefasIds: List<String>) {
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Atualiza o status de todas as tarefas concluídas para "Enviada"
            for (tarefa in tarefasDoDia) {
                if (tarefasIds.contains(tarefa["id"])) {
                    tarefa["status"] = "Enviada"
                }
            }

            // Atualiza o campo "tarefas" no Firestore com todas as modificações em uma única operação
            dataTarefas[dataSelecionada] = tarefasDoDia
            tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                Log.d("Firestore", "Tarefas marcadas como enviadas com sucesso.")
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao marcar tarefas como enviadas", e)
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

    // Função sendProgress que envia o progresso de todas as tarefas concluídas
    private fun sendProgress(userId: String, dataSelecionada: String) {
        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>>
                    val tarefasIdsConcluidas = mutableListOf<String>()

                    // Coleta todos os IDs de tarefas concluídas
                    dataTarefas?.get(dataSelecionada)?.forEach { tarefa ->
                        val tarefaId = tarefa["id"] as? String
                        val status = tarefa["status"] as? String

                        if (tarefaId != null && status == "Concluída") {
                            tarefasIdsConcluidas.add(tarefaId)
                        }
                    }

                    // Atualiza o status das tarefas concluídas em lote
                    if (tarefasIdsConcluidas.isNotEmpty()) {
                        updateTaskStatusEnviada(userId, dataSelecionada, tarefasIdsConcluidas)
                    }

                    // Desativa o botão e ajusta a transparência
                    binding.sendProgress.isEnabled = false
                    binding.sendProgress.alpha = 0.7f
                } else {
                    Log.d("Firestore", "Documento não encontrado")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao enviar progresso", e)
            }
    }

    private fun isTaskEditable(taskDate: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        return taskDate >= currentDate // Retorna true se a tarefa for da data atual ou futura
    }

}