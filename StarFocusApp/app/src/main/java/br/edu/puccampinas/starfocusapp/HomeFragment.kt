package br.edu.puccampinas.starfocusapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.starfocusapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Fragmento que exibe a tela inicial com o calendário,
 * permite o gerenciamento de tarefas diárias e o envio
 * de progresso.
 * @author Lais
 * @version 1.0
 */
class HomeFragment : Fragment(), ProgressListener {

    // Binding da view do fragmento, que permite acessar os elementos de UI da tela
    private lateinit var binding: FragmentHomeBinding
    // Instância de Calendar que é usada para obter a data atual e manipular o calendário
    private var calendar = Calendar.getInstance()
    // A posição do dia atual no layout de dias. Usado para ajustar a rolagem para o dia atual
    private var currentDayPosition = -1
    // Flag para garantir que o ajuste da rolagem seja feito apenas uma vez. Inicialmente, é falso
    private var isScrollAdjusted = false

    // O dia selecionado no calendário, inicialmente é o dia atual
    private var selectedDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    // O mês selecionado no calendário, inicialmente é o mês atual (0 para janeiro, 11 para dezembro)
    private var selectedMonth: Int = calendar.get(Calendar.MONTH)
    // O ano selecionado no calendário, inicialmente é o ano atual
    private var selectedYear: Int = calendar.get(Calendar.YEAR)

    // Instância do Firebase Firestore para acessar o banco de dados do Firebase
    private lateinit var db: FirebaseFirestore
    // Instância do FirebaseAuth para autenticação do usuário no Firebase
    private lateinit var auth: FirebaseAuth

    // A data selecionada como String, utilizada para exibir ou manipular a data de maneira formatada
    private var selectedDate: String? = null

    // Variável que instância a barra de progresso
    private lateinit var clienteAndroid: ClienteAndroid
    private lateinit var parceiro: Parceiro
    private var isClienteAndroidInitialized = false
    private var isInitializingClient = false


    /**
     * Método chamado quando a view do fragmento é criada. Aqui são configurados os elementos da UI e as ações dos botões.
     *
     * @param inflater O LayoutInflater utilizado para inflar a view.
     * @param container O container que vai hospedar a view do fragmento.
     * @param savedInstanceState O estado salvo, caso haja alguma informação a ser restaurada.
     * @author Lais
     * @return A view inflada do fragmento.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("HomeFragment", "onViewCreated chamada")

        // Infla a view do fragmento usando o binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Chama a função suspend dentro de uma coroutine
        lifecycleScope.launch {
            val stringNumeros = obterMetricas()
            // Agora você pode usar stringNumeros como quiser
            Log.d("Metricas", "Valores das métricas: $stringNumeros")
        }

        // Configuração de interface e eventos
        setupUI()
        return binding.root
    }

    private suspend fun initializeClientAndroid(): Boolean {
        if (isInitializingClient) {
            return false
        }

        isInitializingClient = true
        try {
            Log.d("HomeFragment", "Iniciando a conexão com o servidor...")

            // Estabelecer a conexão com o servidor em uma thread de fundo
            val socket = withContext(Dispatchers.IO) {
                try {
                    val newSocket = Socket("10.0.2.2", 3000)  // Para emulador (alterar para IP correto se estiver no dispositivo)
                    Log.d("HomeFragment", "Socket conectado com sucesso.")
                    newSocket
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Falha ao conectar ao servidor", e)
                    null
                }
            }

            // Verificar se o socket foi criado corretamente
            if (socket == null || !socket.isConnected) {
                Log.e("HomeFragment", "Erro ao conectar ao servidor, socket nulo ou não conectado.")
                return false
            }

            Log.d("HomeFragment", "Tentando criar os streams...")

            // Criar DataInputStream e DataOutputStream para comunicação com o servidor
            val inputStream = withContext(Dispatchers.IO) {
                try {
                    val stream = DataInputStream(socket.getInputStream())
                    Log.d("HomeFragment", "DataInputStream criado com sucesso.")
                    stream
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Erro ao criar DataInputStream", e)
                    null
                }
            }

            val outputStream = withContext(Dispatchers.IO) {
                try {
                    val stream = DataOutputStream(socket.getOutputStream())
                    Log.d("HomeFragment", "DataOutputStream criado com sucesso.")
                    stream
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Erro ao criar DataOutputStream", e)
                    null
                }
            }

            // Verificar se os streams foram criados corretamente
            if (inputStream == null || outputStream == null) {
                Log.e("HomeFragment", "Falha ao criar DataInputStream ou DataOutputStream. Abortando a inicialização.")
                return false
            }

            Log.d("HomeFragment", "Streams criados com sucesso, inicializando parceiro...")

            // Inicializar o parceiro e o clienteAndroid com os streams criados
            parceiro = Parceiro(socket, inputStream, outputStream)
            Log.d("HomeFragment", "Parceiro criado com sucesso.")

            clienteAndroid = ClienteAndroid(this, parceiro)
            Log.d("HomeFragment", "clienteAndroid inicializado com sucesso.")

            isClienteAndroidInitialized = true
            return true // Sucesso na inicialização

        } catch (e: Exception) {
            Log.e("HomeFragment", "Erro na inicialização do clienteAndroid", e)
            e.printStackTrace()
            return false
        } finally {
            isInitializingClient = false
        }
    }

    private suspend fun obterMetricas(): String {
        val userId = auth.currentUser?.uid ?: return "000000000000" // Retorna string com zeros se não houver usuário logado
        Log.d("obterMetricas", "Usuário logado: $userId")

        // Obtém o mês e ano atual usando o Calendar
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Mês atual (Janeiro é 0, então soma 1)
        val year = calendar.get(Calendar.YEAR) // Ano atual

        // Formato do mês/ano
        val mesAno = String.format("%02d-%d", month, year)
        Log.d("obterMetricas", "Mês e ano atuais: $mesAno")

        // Variáveis para contar os diferentes status das tarefas
        var pendenteCount = 0
        var concluidaCount = 0
        var enviadaCount = 0
        var totalCount = 0

        // Obtém as tarefas do Firestore para o usuário logado
        val db = FirebaseFirestore.getInstance()

        return suspendCoroutine { continuation ->
            db.collection("Tarefas").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Obtém as tarefas organizadas por data no formato esperado
                        val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>> ?: return@addOnSuccessListener

                        // Filtra e conta as tarefas para o mês/ano atual com base no status
                        dataTarefas.forEach { (data, tarefasList) ->
                            if (data.substring(3, 10) == mesAno) { // Verifica se a data pertence ao mês/ano atual (formato dd-MM-yyyy)
                                tarefasList.forEach { tarefa ->
                                    val status = tarefa["status"] as? String
                                    when (status) {
                                        "Pendente" -> pendenteCount++
                                        "Concluída" -> concluidaCount++
                                        "Enviada" -> enviadaCount++
                                    }
                                    totalCount++
                                }
                            }
                        }

                        // Log dos contadores de status das tarefas
                        Log.d("obterMetricas", "Contadores de tarefas - Pendente: $pendenteCount, Concluída: $concluidaCount, Enviada: $enviadaCount, Total: $totalCount")

                        // Formata os contadores como uma String com três dígitos cada
                        val resultString = String.format(
                            "%03d%03d%03d%03d",
                            pendenteCount,
                            concluidaCount,
                            enviadaCount,
                            totalCount
                        )
                        Log.d("obterMetricas", "String formatada de métricas: $resultString")

                        continuation.resume(resultString)
                    } else {
                        Log.d("Firestore", "Documento não encontrado ou está vazio")
                        continuation.resume("000000000000") // Retorna string com zeros se não encontrar dados
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Erro ao carregar tarefas", e)
                    continuation.resume("000000000000") // Retorna string com zeros em caso de erro
                }
        }
    }

    fun updateProgress() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d("HomeFragment", "Iniciando a atualização do progresso...")

                // Inicializar o cliente Android de forma simplificada
                val initializedSuccessfully = initializeClientAndroid()

                if (initializedSuccessfully && isClienteAndroidInitialized) {
                    Log.d(
                        "HomeFragment",
                        "clienteAndroid inicializado, atualizando barra de progresso..."
                    )

                    val userId = auth.currentUser?.uid
                    val dataSelecionada = String.format(
                        "%02d-%02d-%04d",
                        selectedDay,
                        selectedMonth + 1,
                        selectedYear
                    )
                    if (userId != null) {
                        countTasks(userId, dataSelecionada) { totalTarefas, enviadas ->
                            Log.d(
                                "HomeFragment",
                                "Chamando sendProgress com totalTarefas=$totalTarefas e enviadas=$enviadas"
                            )
                            clienteAndroid.sendProgress(totalTarefas, enviadas)
                            Log.d(
                                "HomeFragment",
                                "Progresso enviado com sucesso: totalTarefas=$totalTarefas, enviadas=$enviadas"
                            )
                        }
                    }
                } else {
                    Log.e("HomeFragment", "clienteAndroid não foi inicializado corretamente.")
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Falha na inicialização do clienteAndroid", e)
            }
        }
    }

    // Implementação do método da interface ProgressListener
    override fun onProgressUpdate(progresso: Int) {
        activity?.runOnUiThread {
            binding.progressBar.progress = progresso
            Log.d("HomeFragment", "Progresso atualizado: $progresso%")
        }
    }

    private fun setupUI() {
        // Inicialmente, o botão 'sendProgress' estará desabilitado e com opacidade reduzida
        binding.sendProgress.isEnabled = false
        binding.sendProgress.alpha = 0.7f

        // Configurações de botão de calendário e tarefas
        updateCalendarButtonText()
        binding.ButtonCalendar.setOnClickListener { showMonthYearPickerDialog() }
        addDaysToView(calendar)

        binding.BarDaysScroll.post {
            if (!isScrollAdjusted && currentDayPosition != -1) {
                val targetPosition = (currentDayPosition - 2).coerceAtLeast(1) - 1
                val dayView = binding.linearLayoutDays.getChildAt(targetPosition)
                binding.BarDaysScroll.scrollTo(dayView.left, 0)
                isScrollAdjusted = true
            }
        }

        arguments?.let { selectedDate = it.getString("selected_date") }
        binding.InputTask.setOnClickListener { openAddTaskBottomSheet() }

        if (auth.currentUser != null) {
            loadTasksForSelectedDay(String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear))
        }

        // Evento de clique para o botão de envio de progresso
        binding.sendProgress.setOnClickListener {
            val userId = auth.currentUser?.uid
            val dataSelecionada = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            if (userId != null) sendProgress(userId, dataSelecionada)
        }
    }

    private fun openAddTaskBottomSheet() {
        val bottomSheetFragment = BottomsSheetAddTaskFragment {
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

    /**
     * Adiciona os dias do mês atual no layout.
     * Para cada dia, é criado um `LinearLayout` com o número do dia e o símbolo do dia da semana.
     * O fundo do dia atual e do dia selecionado é modificado.
     * @author Lais
     * @param calendar O objeto Calendar utilizado para determinar o número de dias no mês e as datas.
     */
    private fun addDaysToView(calendar: Calendar) {

        // Remove todas as views existentes no LinearLayout antes de adicionar novas
        binding.linearLayoutDays.removeAllViews()
        // Obtém o número total de dias no mês
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        // Obtém a posição do dia atual no mês
        currentDayPosition = calendar.get(Calendar.DAY_OF_MONTH)

        // Loop para criar os elementos de cada dia
        for (day in 1..daysInMonth) {
            val dayLayout = LinearLayout(requireContext())
            dayLayout.orientation = LinearLayout.VERTICAL

            // Cria o TextView que exibe o símbolo do dia da semana
            val dayOfWeekTextView = TextView(requireContext()).apply {
                text = getDayOfWeekSymbol(calendar, day)
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#3D3D3D"))
            }

            // Cria o TextView que exibe o número do dia
            val dayNumberTextView = TextView(requireContext()).apply {
                text = "$day"
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 0)
                setTextColor(Color.parseColor("#3D3D3D"))

                // Marca o dia selecionado com um fundo especial
                if (day == selectedDay) {
                    setTextColor(Color.WHITE)
                    background = resources.getDrawable(R.drawable.circle_selected_day, null)
                    setPadding(0, 0, 0, 0)
                }

                // Marca o dia atual com um fundo amarelo
                if (day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) && calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                    background = resources.getDrawable(R.drawable.circle_today_day, null)
                }
            }

            // Ao clicar em um dia, chama a função de seleção do dia
            dayNumberTextView.setOnClickListener { selectDay(day) }
            dayLayout.setOnClickListener { selectDay(day) }

            // Adiciona os TextViews ao layout do dia
            dayLayout.addView(dayOfWeekTextView)
            dayLayout.addView(dayNumberTextView)

            // Define a aparência do layout de cada dia
            dayLayout.setBackgroundColor(Color.TRANSPARENT)
            dayLayout.setPadding(40, 16, 40, 16)

            // Adiciona o layout do dia ao LinearLayout principal
            binding.linearLayoutDays.addView(dayLayout)
        }
    }

    /**
     * Retorna o símbolo do dia da semana (primeira letra) para um dado dia do mês.
     *
     * @param calendar O objeto Calendar utilizado para determinar o dia da semana.
     * @param day O número do dia para o qual o símbolo deve ser retornado.
     * @author Lais
     * @return O símbolo do dia da semana, sendo uma letra representativa.
     */
    private fun getDayOfWeekSymbol(calendar: Calendar, day: Int): String {

        // Atualiza o calendário para o dia específico
        calendar.set(Calendar.DAY_OF_MONTH, day)

        // Retorna o símbolo do dia da semana
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "S" // Segunda-feira
            Calendar.TUESDAY -> "T" // Terça-feira
            Calendar.WEDNESDAY -> "Q" // Quarta-feira
            Calendar.THURSDAY -> "Q" // Quinta-feira
            Calendar.FRIDAY -> "S" // Sexta-feira
            Calendar.SATURDAY -> "S" // Sábado
            Calendar.SUNDAY -> "D" // Domingo
            else -> "" // Caso inesperado
        }
    }

    /**
     * Atualiza a data selecionada e recarrega a visualização do calendário.
     * Ao selecionar um novo dia, o calendário é atualizado para refletir o dia escolhido
     * e as tarefas para o dia selecionado são carregadas.
     *
     * @param day O número do dia que foi selecionado.
     * @author Lais
     */
    private fun selectDay(day: Int) {

        // Atualiza o dia, mês e ano selecionado
        selectedDay = day
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedYear = calendar.get(Calendar.YEAR)

        // Recarrega os dias no layout
        addDaysToView(calendar)  // Atualiza a exibição do calendário com o novo dia selecionado

        // Formata a data selecionada e carrega as tarefas para o dia selecionado
        val selectedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
        loadTasksForSelectedDay(selectedDate)
    }

    /**
     * Exibe um diálogo para que o usuário selecione um mês e ano específicos.
     * O diálogo inclui spinners para escolher o mês e um `NumberPicker` para escolher o ano.
     * Quando o usuário confirmar a escolha, o calendário será atualizado com as opções selecionadas.
     * @author Lais
     */
    private fun showMonthYearPickerDialog() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Cria o builder para o AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecionar Mês e Ano")

        // Infla o layout do diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        builder.setView(dialogView)

        // Configura o spinner de meses
        val monthSpinner = dialogView.findViewById<Spinner>(R.id.monthSpinner)
        val months = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter
        monthSpinner.setSelection(currentMonth)

        // Configura o NumberPicker para o ano
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        yearPicker.minValue = 2024
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        // Configura a ação do botão OK do diálogo
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

        // Exibe o diálogo
        builder.show()
    }

    /**
     * Atualiza o texto exibido no botão de calendário, mostrando o mês e o ano selecionados.
     * @author Lais
     */
    private fun updateCalendarButtonText() {

        // Obtém o nome do mês e o ano
        val month = getMonthName(selectedMonth)
        val year = selectedYear

        // Atualiza o texto do botão para o mês e ano selecionados
        binding.ButtonCalendar.text = "$month/$year"
    }

    /**
     * Retorna o nome do mês com base no valor numérico do mês.
     *
     * @param month O número do mês (0 para Janeiro, 11 para Dezembro).
     * @author Lais
     * @return O nome do mês correspondente.
     */
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

    /**
     * Carrega as tarefas para o dia selecionado e atualiza a interface com as informações.
     * O layout é recriado com os dias do mês e o scroll é reposicionado para o dia específico.
     *
     * @param selectedDate A data selecionada no formato "dd-MM-yyyy".
     * @author Lais
     */
    fun loadTasksForSelectedDay(selectedDate: String) {

        // Obtém o ID do usuário logado, retornando imediatamente se não estiver logado
        val userId = auth.currentUser?.uid ?: return

        // Divide a data selecionada (exemplo: "05-11-2024") para extrair o dia, mês e ano
        val parts = selectedDate.split("-")
        val day = parts[0].toInt() // Converte o dia para inteiro
        val month = parts[1].toInt() - 1 // Converte o mês para inteiro e subtrai 1, porque Janeiro é 0
        val year = parts[2].toInt() // Converte o ano para inteiro

        // Atualiza as variáveis de dia, mês e ano no estado do calendário
        selectedDay = day
        selectedMonth = month
        selectedYear = year

        // Configura o calendário para a data selecionada
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

        // Atualiza o layout com os dias do mês correspondente
        addDaysToView(calendar)

        // Reposiciona o scroll para o dia específico
        binding.BarDaysScroll.post {
            // Calcula a posição do scroll para centralizar o dia no meio da tela
            val targetPosition = (selectedDay - 1).coerceAtLeast(0) // Impede que a posição seja negativa
            val dayView = binding.linearLayoutDays.getChildAt(targetPosition) // Obtém o "View" do dia

            // Se o "View" do dia existir, ajusta o scroll para centralizar o dia
            if (dayView != null) {
                val scrollCenter = (binding.BarDaysScroll.width / 2) - (dayView.width / 2) // Calcula a posição para centralização
                binding.BarDaysScroll.scrollTo(dayView.left - scrollCenter, 0) // Posiciona o scroll para centralizar o dia
            }
        }

        // Obtém as tarefas do Firestore para o usuário logado
        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtém as tarefas organizadas por data no formato esperado
                    val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>>

                    // Se houver tarefas no dia selecionado, processa e exibe
                    if (dataTarefas != null) {
                        // Seleciona as tarefas do dia específico
                        val tarefasDoDia = dataTarefas[selectedDate]?.mapNotNull { tarefa ->
                            val id = tarefa["id"] as? String // ID da tarefa
                            val texto = tarefa["texto"] as? String // Texto da tarefa
                            val status = tarefa["status"] as? String ?: "Pendente" // Status da tarefa (default é "Pendente")
                            Triple(id, texto, status) // Retorna um objeto com ID, texto e status da tarefa
                        } ?: emptyList()

                        // Exibe as tarefas carregadas
                        Log.d("Firestore", "Tarefas para $selectedDate: $tarefasDoDia")
                        displayTasks(tarefasDoDia, selectedDate) // Chama displayTasks para exibir as tarefas na interface

                        // Atualiza o calendário e os botões da interface
                        updateCalendarButtonText()
                        addDaysToView(calendar) // Recria o layout dos dias
                    } else {
                        Log.d("Firestore", "Campo 'tarefas' está vazio ou não encontrado")
                    }
                } else {
                    Log.d("Firestore", "Documento não encontrado")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao carregar tarefas", e) // Loga qualquer erro ao tentar carregar tarefas
            }
    }

    /**
     * Exibe as tarefas para o usuário na interface, permitindo a interação com elas.
     *
     * @param tarefas Lista de tarefas a serem exibidas, onde cada tarefa é representada por um Triple
     * contendo o ID da tarefa, o texto da tarefa e seu status.
     * @param selectedDate Data selecionada pelo usuário para visualizar as tarefas.
     * @author Lais
     */
    private fun displayTasks(tarefas: List<Triple<String?, String?, String>>, selectedDate: String) {

        // Limpa todas as tarefas anteriores exibidas na interface
        binding.TaskContainer.removeAllViews() // Limpa tarefas anteriores

        val userId = auth.currentUser?.uid ?: return // Obtém o ID do usuário atual, se disponível
        var hasConcludedTask = false // Flag para verificar se há alguma tarefa concluída

        val today = Calendar.getInstance() // Data de hoje
        val selectedDateCalendar = Calendar.getInstance().apply {
            // Converte a data selecionada em um objeto Calendar
            val parts = selectedDate.split("-")
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1) // Janeiro é 0
            set(Calendar.YEAR, parts[2].toInt())
        }

        // Verifica se a data selecionada já passou
        val isPastDate = selectedDateCalendar.before(today)
        if (isPastDate) {
            binding.InputTask.isEnabled = false // Desabilita a inserção de novas tarefas para datas passadas
            binding.InputTask.alpha = 0.5f // Diminui a opacidade do botão
        } else {
            binding.InputTask.isEnabled = true // Habilita a inserção de novas tarefas para datas futuras
            binding.InputTask.alpha = 1f
        }

        // Itera sobre cada tarefa para configurar a exibição
        tarefas.forEach { (tarefaId, tarefaTexto, status) ->
            if (tarefaId != null && tarefaTexto != null) {
                val concluido = status == "Concluída"
                val enviada = status == "Enviada"  // Verifica se a tarefa está com status "Enviada"

                // Cria o componente de visualização da tarefa
                val tarefaView = RadioButton(requireContext()).apply {
                    text = tarefaTexto
                    textSize = 20f
                    gravity = Gravity.CENTER_VERTICAL
                    isAllCaps = false

                    setBackgroundResource(R.drawable.rectangleaddtask) // Define o fundo
                    setTextColor(Color.parseColor("#3D3D3D")) // Define a cor do texto

                    buttonDrawable = null // Remove o botão padrão do RadioButton
                    compoundDrawablePadding = 37 // Ajusta o espaço entre o texto e o botão
                    setPadding(70, 10, 60, 10) // Ajusta o padding

                    isEnabled = !isPastDate // Desabilita a interação em datas passadas
                    if (isPastDate) {
                        setTextColor(Color.GRAY) // Altera a cor do texto para indicar inatividade
                    }

                    // Configura o visual de acordo com o status da tarefa
                    when {
                        enviada -> { // Configura o visual de tarefas enviadas
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_send_24, 0, 0, 0) // Ícone de "Enviada"
                            setTextColor(Color.GRAY) // Cor cinza para indicar inatividade
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Texto riscado
                            isEnabled = false // Desabilita a interação
                        }
                        concluido -> { // Visual para tarefas concluídas, mas não enviadas
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_24, 0, 0, 0) // Ícone de concluído
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            isChecked = true
                        }
                        else -> { // Visual para tarefas pendentes
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0)
                            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }

                    // Permite a interação apenas se a tarefa não estiver "Enviada"
                    if (!enviada) {
                        // Define o comportamento do clique na tarefa
                        setOnClickListener {
                            // Marca a tarefa como concluída se ainda não estiver
                            if (!concluido) {
                                updateTaskStatusConcluida(userId, selectedDate, tarefaId) //Atualiza o status no banco

                                // Aplica o efeito de riscado no texto da tarefa (indicando que foi concluída)
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                // Marca o checkbox como "selecionado"
                                isChecked = true
                                // Exibe o ícone de "check" (tarefa concluída) ao lado do texto
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_24, 0, 0, 0)

                                // Exibe o drawable de conclusão
                                binding.feedback1.apply {
                                    alpha = 0f // Inicializa o feedback invisível
                                    visibility = View.VISIBLE // Torna o feedback visível
                                    animate().alpha(1f).setDuration(300).start() // Anima a opacidade para 1 (visível)
                                }

                                // Após 3 segundos, faz o feedback desaparecer com animação
                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.feedback1.animate()
                                        .alpha(0f) // Anima a opacidade para 0 (invisível)
                                        .setDuration(300)
                                        .withEndAction {
                                            binding.feedback1.visibility = View.GONE // Esconde o feedback após a animação
                                        }
                                        .start()
                                }, 3000)
                            } else {
                                // Se a tarefa já estava concluída, marca como "Pendente" (desfaz a conclusão)
                                updateTaskStatusPendente(userId, selectedDate, tarefaId)
                                // Remove o efeito de riscado no texto (indica que a tarefa não está mais concluída)
                                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                // Desmarca o checkbox (a tarefa não está mais concluída)
                                isChecked = false
                                // Exibe o ícone de "check-circle-outline" (tarefa não concluída) ao lado do texto
                                setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_circle_outline_24, 0, 0, 0)
                            }
                        }
                    }
                }

                // Configura o layout da tarefa dentro do contêiner de tarefas, ajustando as margens
                val layoutParams = LinearLayout.LayoutParams(
                    binding.InputTask.width,
                    binding.InputTask.height
                ).apply {
                    setMargins(16, 0, 16, 0) // Define as margens esquerda e direita
                }
                // Aplica o layout à view da tarefa
                tarefaView.layoutParams = layoutParams
                // Adiciona a tarefa ao contêiner de tarefas na tela
                binding.TaskContainer.addView(tarefaView)

                // Se a tarefa já estava concluída, marca que há uma tarefa concluída na lista
                if (concluido) {
                    hasConcludedTask = true
                }
            }
        }

        // Ativa ou desativa o botão de progresso dependendo da presença de tarefas concluídas
        if (hasConcludedTask) {
            binding.sendProgress.isEnabled = true
            binding.sendProgress.alpha = 1f
        } else {
            binding.sendProgress.isEnabled = false
            binding.sendProgress.alpha = 0.7f
        }

        // Reposiciona os botões de adicionar tarefa
        val inputTaskParent = binding.InputTask.parent
        if (inputTaskParent is ViewGroup) {
            inputTaskParent.removeView(binding.InputTask)
        }
        binding.TaskContainer.addView(binding.InputTask)

        // Reposiciona os botões de enviar progresso
        val buttonProgressParent = binding.buttonProgress.parent
        if (buttonProgressParent is ViewGroup) {
            buttonProgressParent.removeView(binding.buttonProgress)
        }
        binding.TaskContainer.addView(binding.buttonProgress)

        updateProgress()
    }

    /**
     * Atualiza o status de uma tarefa para "Concluída" no banco de dados.
     *
     * @param userId ID do usuário.
     * @param dataSelecionada Data da tarefa a ser atualizada.
     * @param tarefaId ID da tarefa a ser marcada como concluída.
     * @author Lais
     */
    private fun updateTaskStatusConcluida(userId: String, dataSelecionada: String, tarefaId: String) {

        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            // Obtém as tarefas do documento, ou inicializa um mapa vazio caso não exista
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            // Recupera as tarefas do dia específico ou inicializa uma lista vazia caso não existam tarefas para a data
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Percorre as tarefas do dia para encontrar a tarefa pelo ID e atualizar o seu status para "Concluída"
            for (tarefa in tarefasDoDia) {
                if (tarefa["id"] == tarefaId) {
                    tarefa["status"] = "Concluída" // Atualiza o status da tarefa para "Concluída"
                    break // Interrompe o loop após encontrar e atualizar a tarefa
                }
            }

            // Atualiza as tarefas no Firestore com a modificação do status da tarefa
            dataTarefas[dataSelecionada] = tarefasDoDia
            tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                // Sucesso ao atualizar o status da tarefa
                Log.d("Firestore", "Tarefa marcada como concluída com sucesso.")
                // Recarrega as tarefas para o dia selecionado após a atualização
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                // Erro ao tentar atualizar o status da tarefa
                Log.w("Firestore", "Erro ao marcar tarefa como concluída", e)
            }
        }.addOnFailureListener { e ->
            // Erro ao tentar obter o documento de tarefas do Firestore
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

    /**
     * Atualiza o status de uma tarefa para "Pendente" no Firestore.
     * Este método localiza a tarefa pelo ID fornecido e altera o seu status de "Concluída" ou qualquer outro valor
     * para "Pendente". Em seguida, ele atualiza o Firestore com a nova informação.
     *
     * @param userId O ID do usuário cujas tarefas serão atualizadas.
     * @param dataSelecionada A data (em formato "dd-MM-yyyy") das tarefas que serão atualizadas.
     * @param tarefaId O ID da tarefa a ser atualizada para "Pendente".
     */
    private fun updateTaskStatusPendente(userId: String, dataSelecionada: String, tarefaId: String) {

        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        // Recupera o documento de tarefas do usuário
        tarefasRef.get().addOnSuccessListener { document ->
            // Obtém as tarefas do documento ou inicializa um mapa vazio caso não existam tarefas
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            // Recupera as tarefas do dia específico ou inicializa uma lista vazia caso não existam tarefas para a data
            val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

            // Percorre as tarefas do dia para encontrar a tarefa pelo ID e atualiza o seu status para "Pendente"
            for (tarefa in tarefasDoDia) {
                if (tarefa["id"] == tarefaId) {
                    tarefa["status"] = "Pendente" // Atualiza o status da tarefa para "Pendente"
                    break // Interrompe o loop após encontrar e atualizar a tarefa
                }
            }

            // Atualiza o campo "tarefas" no Firestore com a modificação
            dataTarefas[dataSelecionada] = tarefasDoDia
            tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                // Sucesso ao atualizar o status da tarefa
                Log.d("Firestore", "Tarefa marcada como pendente com sucesso.")
                // Recarrega as tarefas para o dia selecionado após a atualização
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                // Em caso de erro ao tentar atualizar o status da tarefa
                Log.w("Firestore", "Erro ao marcar tarefa como pendente", e)
            }
        }.addOnFailureListener { e ->
            // Em caso de falha ao tentar obter as tarefas do Firestore
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

    /**
     * Atualiza o status de várias tarefas para "Enviada" no Firestore.
     * Este método localiza as tarefas pelos seus IDs fornecidos e altera o seu status para "Enviada". Em seguida,
     * ele atualiza o Firestore com as novas informações de todas as tarefas.
     *
     * @param userId O ID do usuário cujas tarefas serão atualizadas.
     * @param dataSelecionada A data (em formato "dd-MM-yyyy") das tarefas que serão atualizadas.
     * @param tarefasIds A lista de IDs das tarefas a serem atualizadas para "Enviada".
     * @author Lais
     */
    private fun updateTaskStatusEnviada(userId: String, dataSelecionada: String, tarefasIds: List<String>) {

        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            // Obtém as tarefas do documento ou inicializa um mapa vazio caso não existam tarefas
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<MutableMap<String, Any>>> ?: mutableMapOf()
            // Recupera as tarefas do dia específico ou inicializa uma lista vazia caso não existam tarefas para a data
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
                // Sucesso ao atualizar o status das tarefas
                Log.d("Firestore", "Tarefas marcadas como enviadas com sucesso.")
                // Recarrega as tarefas para o dia selecionado após a atualização
                loadTasksForSelectedDay(dataSelecionada)
            }.addOnFailureListener { e ->
                // Em caso de erro ao tentar atualizar o status das tarefas
                Log.w("Firestore", "Erro ao marcar tarefas como enviadas", e)
            }
        }.addOnFailureListener { e ->
            // Em caso de falha ao tentar obter as tarefas do Firestore
            Log.w("Firestore", "Erro ao obter tarefas", e)
        }
    }

    /**
     * Envia o progresso de todas as tarefas concluídas para o Firestore.
     * Este método coleta todas as tarefas concluídas para a data selecionada e as marca como "Enviada".
     * Após a atualização, ele desativa o botão de envio de progresso.
     *
     * @param userId O ID do usuário cujas tarefas serão enviadas.
     * @param dataSelecionada A data (em formato "dd-MM-yyyy") das tarefas a serem enviadas.
     * @author Lais
     */
    private fun sendProgress(userId: String, dataSelecionada: String) {
        Log.d("HomeFragment", "Função sendProgress chamada. userId: $userId, dataSelecionada: $dataSelecionada")

        // Referência ao documento de tarefas do usuário no Firestore
        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                // Verifica se o documento existe
                if (document != null && document.exists()) {
                    // Recupera as tarefas do documento ou inicializa um mapa vazio
                    val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>>
                    val tarefasIdsConcluidas = mutableListOf<String>()

                    // Coleta todos os IDs de tarefas concluídas para a data selecionada
                    dataTarefas?.get(dataSelecionada)?.forEach { tarefa ->
                        val tarefaId = tarefa["id"] as? String
                        val status = tarefa["status"] as? String

                        // Adiciona os IDs das tarefas concluídas à lista
                        if (tarefaId != null && status == "Concluída") {
                            tarefasIdsConcluidas.add(tarefaId)
                        }
                    }

                    // Atualiza o status das tarefas concluídas para "Enviada"
                    if (tarefasIdsConcluidas.isNotEmpty()) {
                        updateTaskStatusEnviada(userId, dataSelecionada, tarefasIdsConcluidas)
                        updateProgress()
                    }

                    // Desativa o botão e ajusta a transparência após o envio
                    binding.sendProgress.isEnabled = false
                    binding.sendProgress.alpha = 0.7f
                } else {
                    // Caso o documento de tarefas não seja encontrado
                    Log.d("Firestore", "Documento não encontrado")
                }
            }
            .addOnFailureListener { e ->
                // Em caso de falha ao tentar obter as tarefas do Firestore
                Log.e("Firestore", "Erro ao enviar progresso", e)
            }
    }

    /**
     * Conta o total de tarefas de um usuário para uma data específica
     * e quantas delas possuem o status "Enviada".
     * @param userId O ID do usuário autenticado para o qual as tarefas serão contadas.
     * @param dataSelecionada A data específica para a qual as tarefas serão contadas, no formato "dd-MM-yyyy".
     * @param onResult Callback que retorna o resultado da contagem. Recebe dois parâmetros:
     *   - totalTarefas: O total de tarefas cadastradas para o dia selecionado.
     *   - enviadas: O número de tarefas com o status "Enviada" para o dia selecionado.
     * @author Lais
     **/
    private fun countTasks(userId: String, dataSelecionada: String, onResult: (totalTarefas: Int, enviadas: Int) -> Unit) {
        Log.d("HomeFragment", "Contando tarefas para userId: $userId, data: $dataSelecionada")
        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Obtém o mapa de tarefas por data
                val dataTarefas = document.get("tarefas") as? Map<String, List<Map<String, Any>>> ?: emptyMap()
                // Recupera as tarefas do dia selecionado
                val tarefasDoDia = dataTarefas[dataSelecionada] ?: emptyList()

                // Conta o total de tarefas do dia
                val totalTarefas = tarefasDoDia.size
                // Conta quantas têm o status "Enviada"
                val enviadas = tarefasDoDia.count { tarefa -> tarefa["status"] == "Enviada" }

                // Retorna os resultados
                onResult(totalTarefas, enviadas)
            } else {
                // Caso o documento não exista, retorna 0 para ambas as contagens
                onResult(0, 0)
            }
        }.addOnFailureListener { exception ->
            // Em caso de erro, você pode exibir uma mensagem ou logar o erro
            Log.e("FirestoreError", "Erro ao contar as tarefas", exception)
            onResult(0, 0) // Retorna 0 em caso de falha
        }
    }

}