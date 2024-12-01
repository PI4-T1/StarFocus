package br.edu.puccampinas.starfocusapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
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
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

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
    private var isDialogShown = false // Variável para rastrear o estado do diálogo


    /**
     * Metodo chamado quando a view do fragmento é criada. Aqui são configurados os elementos da UI e as ações dos botões.
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

        // Configuração de interface e eventos
        setupUI()
        return binding.root
    }

    // Inicializa a conexão com o servidor, cria os streams de comunicação e configura o ClienteAndroid.
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
                    //celular 192.168.15.58
                    val newSocket = Socket("192.168.15.58", 3000)  // Para emulador (alterar para IP correto se estiver no dispositivo)
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

            clienteAndroid = ClienteAndroid(this, null, parceiro)

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

// Atualiza o progresso do usuário, inicializando o cliente Android e enviando o progresso das tarefas ao servidor.
    private fun updateProgress() {
        lifecycleScope.launch(Dispatchers.Main) {// Inicia uma nova coroutine no contexto da thread principal
            try {
                Log.d("HomeFragment", "Iniciando a atualização do progresso...")

                // Inicializar o cliente Android de forma simplificada
                val initializedSuccessfully = initializeClientAndroid()

                // Verifica se o cliente foi inicializado corretamente
                if (initializedSuccessfully && isClienteAndroidInitialized) {
                    Log.d(
                        "HomeFragment",
                        "clienteAndroid inicializado, atualizando barra de progresso..."
                    )
                    // Obtém o ID do usuário atual autenticado
                    val userId = auth.currentUser?.uid

                    // Formata a data selecionada no formato dd-MM-yyyy
                    val dataSelecionada = String.format(
                        "%02d-%02d-%04d",
                        selectedDay,
                        selectedMonth + 1,  // Adiciona 1 ao mês, pois o índice do mês é 0-based
                        selectedYear
                    )
                    // Verifica se o ID do usuário não é nulo
                    if (userId != null) {
                        // Chama a função para contar as tarefas do usuário no dia selecionado
                        countTasks(userId, dataSelecionada) { totalTarefas, enviadas ->
                            Log.d(
                                "HomeFragment",
                                "Chamando sendProgress com totalTarefas=$totalTarefas e enviadas=$enviadas"
                            )
                            clienteAndroid.sendProgress(totalTarefas, enviadas)  // Envia o progresso das tarefas ao servidor através do clienteAndroid
                            Log.d(
                                "HomeFragment",
                                "Progresso enviado com sucesso: totalTarefas=$totalTarefas, enviadas=$enviadas"
                            )
                        }
                    }
                } else {
                    Log.e("HomeFragment", "clienteAndroid não foi inicializado corretamente.")
                    noServerDialog()
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Falha na inicialização do clienteAndroid", e)
            }
        }
    }

    // Implementação do metodo da interface ProgressListener
    override fun onProgressUpdate(progresso: Int) {
        activity?.runOnUiThread {
            // Atualiza o progresso na barra de progresso
            binding.progressBar.progress = progresso
            Log.d("HomeFragment", "Progresso atualizado: $progresso%")

            // Verifica se o progresso atingiu 100%
            if (progresso == 100) {
                val userId = auth.currentUser?.uid
                val dataSelecionada = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)

                if (userId != null) {
                    verifyRewardStatus(userId, dataSelecionada)
                }
            }
        }
    }

    // Função para configurar a interface do usuário (UI)
    private fun setupUI() {
        // Inicialmente, o botão 'sendProgress' estará desabilitado e com opacidade reduzida
        binding.sendProgress.isEnabled = false
        binding.sendProgress.alpha = 0.7f

        // Configurações de botão de calendário e tarefas
        updateCalendarButtonText()
        binding.ButtonCalendar.setOnClickListener { showMonthYearPickerDialog() }
        addDaysToView(calendar)

        // Ajusta o scroll da barra de dias após o layout ser carregado
        binding.BarDaysScroll.post {
            if (!isScrollAdjusted && currentDayPosition != -1) {
                val targetPosition = (currentDayPosition - 2).coerceAtLeast(1) - 1
                val dayView = binding.linearLayoutDays.getChildAt(targetPosition)
                binding.BarDaysScroll.scrollTo(dayView.left, 0)
                isScrollAdjusted = true
            }
        }
        // Carrega a data selecionada a partir dos argumentos passados para o fragmento
        arguments?.let { selectedDate = it.getString("selected_date") }

        // Define o clique para o campo de entrada de tarefas
        binding.InputTask.setOnClickListener { openAddTaskBottomSheet() }

        // Verifica se o usuário está autenticado
        if (auth.currentUser != null) {
            // Se o usuário estiver autenticado, carrega as tarefas para o dia selecionado
            loadTasksForSelectedDay(String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear))
        }

        // Evento de clique para o botão de envio de progresso
        binding.sendProgress.setOnClickListener {
            val userId = auth.currentUser?.uid
            val dataSelecionada = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            if (userId != null) sendProgress(userId, dataSelecionada) // Envia o progresso se o usuário estiver autenticado
        }
    }

    // Função para abrir o Bottom Sheet de adicionar tarefa
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

        // Infla um TextView para o título personalizado
        val titleTextView = TextView(requireContext()).apply {
            text = "Selecionar Mês e Ano"
            setTextColor(Color.WHITE)  // Define a cor do título para branco
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)  // Tamanho da fonte, ajuste conforme necessário
            setPadding(16, 16, 16, 16)  // Padding do título
        }

        // Define o título do AlertDialog como o TextView personalizado
        builder.setCustomTitle(titleTextView)

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
        builder.setPositiveButton("Salvar") { _, _ ->
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

        // Exibe o dialog
        val dialog = builder.create()

        // Personalizar o fundo do AlertDialog
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_background)

        // Carregar a fonte personalizada
        var typeface = ResourcesCompat.getFont(requireContext(), R.font.poppins)

        // Personalizar a fonte do título
        dialog.findViewById<TextView>(android.R.id.title)?.typeface = typeface

        // Personalizar a fonte da mensagem
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface

        // Personalizar a fonte dos botões
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.typeface = typeface

        // Exibe o AlertDialog
        dialog.show()

        // Personalizar o botão OK após o diálogo ser exibido
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.apply {
            textSize = 16f  // Aumenta o tamanho da fonte do botão
            setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow))  // Cor do texto para a cor personalizada
            typeface = Typeface.create(typeface, Typeface.BOLD)  // Define o texto em negrito
        }
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
     * @author Lais e Luíz
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
                    // Obtém o mapa de tarefas organizadas por data, agora com "lista" e "recompensa"
                    val dataTarefas = document.get("tarefas") as? Map<String, Map<String, Any>>

                    // Verifica se há dados para o dia selecionado
                    if (dataTarefas != null) {
                        // Acessa os dados do dia específico
                        val diaData = dataTarefas[selectedDate]
                        val tarefasDoDia = diaData?.get("lista") as? List<Map<String, Any>> ?: emptyList()
                        val recompensaDoDia = diaData?.get("recompensa") as? Boolean ?: false

                        // Mapeia as tarefas do dia para uma lista de Triple com ID, texto e status
                        val tarefasList = tarefasDoDia.mapNotNull { tarefa ->
                            val id = tarefa["id"] as? String // ID da tarefa
                            val texto = tarefa["texto"] as? String // Texto da tarefa
                            val status = tarefa["status"] as? String ?: "Pendente" // Status da tarefa
                            Triple(id, texto, status) // Retorna um objeto com ID, texto e status da tarefa
                        }

                        // Exibe as tarefas carregadas
                        Log.d("Firestore", "Tarefas para $selectedDate: $tarefasList, Recompensa: $recompensaDoDia")
                        displayTasks(tarefasList, selectedDate) // Chama displayTasks para exibir as tarefas na interface

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

        updateProgress()
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
        // Se a data for passada, desabilita a inserção de novas tarefas e diminui a opacidade do botão
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

                // Cria um FrameLayout para conter o RadioButton e o ícone de deletar sobrepostos
                val tarefaContainer = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Cria um LinearLayout para a tarefa
                val tarefaLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // Garante que ocupe toda a largura disponível
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 0, 16, 0) // Define margens similares ao InputTask
                    }
                }

                // componente de visualização da tarefa
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

                // Cria o ícone de deletar
                val deleteIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.delete_icon)
                    setPadding(20, 0, 20, 0)
                    // Oculta o ícone se a tarefa foi enviada
                    visibility = if (enviada) View.GONE else View.VISIBLE

                    setOnClickListener {
                        deleteTask(userId, selectedDate, tarefaId)
                    }

                    // Define o posicionamento do ícone dentro do FrameLayout (à direita)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        marginEnd = 50
                    }
                }

                // Adiciona o RadioButton e o ícone de deletar ao FrameLayout
                tarefaContainer.addView(tarefaView)
                tarefaContainer.addView(deleteIcon)

                // Finalmente, adiciona o tarefaContainer ao layout principal
                tarefaLayout.addView(tarefaContainer)

                // Configura o layout da tarefa dentro do contêiner de tarefas, ajustando as margens
                val containerLayoutParams = LinearLayout.LayoutParams(
                    binding.InputTask.width,
                    binding.InputTask.height
                ).apply {
                    setMargins(16, 0, 16, 0) // Define as margens esquerda e direita
                }
                tarefaContainer.layoutParams = containerLayoutParams

                // Adiciona o tarefaLayout ao contêiner principal de tarefas
                binding.TaskContainer.addView(tarefaLayout)

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
     * metodo que deleta uma tarefa.
     * @author Alex e Laís
     */
    private fun deleteTask(userId: String, date: String, taskId: String) {
        db.collection("Tarefas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtém as tarefas organizadas por data no formato esperado com "lista" e "recompensa"
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableMap<String, Any>> ?: mutableMapOf()

                    // Acessa o mapa do dia selecionado
                    val diaData = dataTarefas[date]
                    val tarefasDoDia = diaData?.get("lista") as? MutableList<Map<String, Any>>

                    tarefasDoDia?.let {
                        // Remove a tarefa específica pelo ID
                        val tarefaRemovida = it.removeIf { tarefa -> tarefa["id"] == taskId }

                        if (tarefaRemovida) {
                            // Atualiza o mapa para refletir as alterações no Firestore
                            dataTarefas[date] = mutableMapOf(
                                "lista" to tarefasDoDia,
                                "recompensa" to (diaData["recompensa"] as? Boolean ?: false) // Preserva o valor atual de "recompensa"
                            )

                            db.collection("Tarefas").document(userId).update("tarefas", dataTarefas)
                                .addOnSuccessListener {

                                    // Atualiza a interface exibindo a lista de tarefas do dia após a exclusão
                                    displayTasks(
                                        tarefasDoDia.map { tarefa ->
                                            Triple(
                                                tarefa["id"] as? String,
                                                tarefa["texto"] as? String,
                                                tarefa["status"] as? String ?: "Pendente"
                                            )
                                        },
                                        date
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Erro ao deletar tarefa: ${e.message}")
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao acessar o documento do Firestore: ${e.message}")
            }

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
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableMap<String, Any>> ?: mutableMapOf()

            // Acessa o mapa do dia selecionado
            val diaData = dataTarefas[dataSelecionada]
            val tarefasDoDia = diaData?.get("lista") as? MutableList<MutableMap<String, Any>>

            // Verifica se existem tarefas no dia e procura pela tarefa a ser atualizada
            tarefasDoDia?.let {
                for (tarefa in it) {
                    if (tarefa["id"] == tarefaId) {
                        tarefa["status"] = "Concluída" // Atualiza o status da tarefa para "Concluída"
                        break // Interrompe o loop após encontrar e atualizar a tarefa
                    }
                }

                // Atualiza o Firestore com a nova lista de tarefas e preserva o valor de "recompensa"
                dataTarefas[dataSelecionada] = mutableMapOf(
                    "lista" to tarefasDoDia,
                    "recompensa" to (diaData?.get("recompensa") as? Boolean ?: false) // Preserva o valor atual de "recompensa"
                )

                tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                    // Sucesso ao atualizar o status da tarefa
                    Log.d("Firestore", "Tarefa marcada como concluída com sucesso.")
                    // Recarrega as tarefas para o dia selecionado após a atualização
                    loadTasksForSelectedDay(dataSelecionada)
                }.addOnFailureListener { e ->
                    // Erro ao tentar atualizar o status da tarefa
                    Log.w("Firestore", "Erro ao marcar tarefa como concluída", e)
                }
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
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableMap<String, Any>> ?: mutableMapOf()

            // Acessa o mapa do dia selecionado
            val diaData = dataTarefas[dataSelecionada]
            val tarefasDoDia = diaData?.get("lista") as? MutableList<MutableMap<String, Any>>

            // Verifica se existem tarefas no dia e procura pela tarefa a ser atualizada
            tarefasDoDia?.let {
                for (tarefa in it) {
                    if (tarefa["id"] == tarefaId) {
                        tarefa["status"] = "Pendente" // Atualiza o status da tarefa para "Pendente"
                        break // Interrompe o loop após encontrar e atualizar a tarefa
                    }
                }

                // Atualiza o Firestore com a nova lista de tarefas e preserva o valor de "recompensa"
                dataTarefas[dataSelecionada] = mutableMapOf(
                    "lista" to tarefasDoDia,
                    "recompensa" to (diaData?.get("recompensa") as? Boolean ?: false) // Preserva o valor atual de "recompensa"
                )

                tarefasRef.update("tarefas", dataTarefas).addOnSuccessListener {
                    // Sucesso ao atualizar o status da tarefa
                    Log.d("Firestore", "Tarefa marcada como pendente com sucesso.")
                    // Recarrega as tarefas para o dia selecionado após a atualização
                    loadTasksForSelectedDay(dataSelecionada)
                }.addOnFailureListener { e ->
                    // Em caso de erro ao tentar atualizar o status da tarefa
                    Log.w("Firestore", "Erro ao marcar tarefa como pendente", e)
                }
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
            val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableMap<String, Any>> ?: mutableMapOf()
            // Recupera as tarefas do dia específico ou inicializa uma lista vazia caso não existam tarefas para a data
            val tarefasDoDia = dataTarefas[dataSelecionada]?.get("lista") as? MutableList<MutableMap<String, Any>> ?: mutableListOf()

            // Atualiza o status de todas as tarefas que possuem o ID na lista tarefasIds para "Enviada"
            tarefasDoDia.forEach { tarefa ->
                if (tarefasIds.contains(tarefa["id"])) {
                    tarefa["status"] = "Enviada"
                }
            }

            // Atualiza o campo "tarefas" no Firestore com todas as modificações em uma única operação
            dataTarefas[dataSelecionada]?.put("lista", tarefasDoDia)
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
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableMap<String, Any>> ?: mutableMapOf()
                    val tarefasIdsConcluidas = mutableListOf<String>()

                    // Coleta todos os IDs de tarefas concluídas para a data selecionada
                    val tarefasDoDia = dataTarefas[dataSelecionada]?.get("lista") as? List<Map<String, Any>> ?: emptyList()

                    // Itera pelas tarefas do dia selecionado
                    tarefasDoDia.forEach { tarefa ->
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
                val dataTarefas = document.get("tarefas") as? Map<String, Map<String, Any>> ?: emptyMap()

                // Verifica se a data selecionada existe e obtém os dados
                val tarefasDoDia = dataTarefas[dataSelecionada]?.get("lista") as? List<Map<String, Any>> ?: emptyList()

                // Conta o total de tarefas do dia
                val totalTarefas = tarefasDoDia.size

                // Conta quantas têm o status "Enviada"
                val enviadas = tarefasDoDia.count { tarefa -> tarefa["status"] == "Enviada" }

                // Retorna os resultados
                onResult(totalTarefas, enviadas)
            } else {
                // Caso o documento não exista, retorna 0 para todas as contagens
                Log.d("FirestoreError", "Documento não encontrado para o usuário $userId.")
                onResult(0, 0)
            }
        }.addOnFailureListener { exception ->
            // Em caso de erro ao acessar o Firestore
            Log.e("FirestoreError", "Erro ao contar as tarefas", exception)
            onResult(0, 0) // Retorna 0 em caso de falha
        }
    }

    private fun checkAndSetCompletion(userId: String) {
        val pessoasRef = db.collection("Pessoas").document(userId)

        pessoasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Acessa os campos
                val history2 = document.getBoolean("history2") ?: false
                val monster2 = document.getBoolean("monster2") ?: false
                val history3 = document.getBoolean("history3") ?: false
                val monster3 = document.getBoolean("monster3") ?: false
                val history4 = document.getBoolean("history4") ?: false
                val monster4 = document.getBoolean("monster4") ?: false
                val monster5 = document.getBoolean("monster5") ?: false

                // Define os updates com base nas condições
                val updates = when {
                    !history2 && !monster2 -> mapOf("history2" to true, "monster2" to true)
                    !history3 && !monster3 -> mapOf("history3" to true, "monster3" to true)
                    !history4 && !monster4 -> mapOf("history4" to true, "monster4" to true)
                    !monster5 -> mapOf("monster5" to true)
                    else -> {
                        Log.d("PessoasUpdate", "Todas as condições já foram atendidas.")
                        Toast.makeText(requireContext(), "No momento, só possuímos 4 recompensas (MVP)!", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }
                }

                // Aplica os updates, se houver
                pessoasRef.update(updates).addOnSuccessListener {
                    Log.d("PessoasUpdate", "Campos atualizados com sucesso: $updates")
                }.addOnFailureListener { e ->
                    Log.e("PessoasUpdate", "Erro ao atualizar campos", e)
                }
            } else {
                Log.d("PessoasUpdate", "Documento para o usuário não encontrado.")
            }
        }.addOnFailureListener { e ->
            Log.e("PessoasUpdate", "Erro ao acessar documento", e)
        }
    }

    private fun verifyRewardStatus(userId: String, dataSelecionada: String) {
        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Acessa o mapa de tarefas por data
                val dataTarefas = document.get("tarefas") as? Map<*, *>
                val tarefaData = dataTarefas?.get(dataSelecionada) as? Map<*, *>

                // Verifica o campo 'recompensa' para a data selecionada
                val recompensa = tarefaData?.get("recompensa") as? Boolean ?: false

                // Verifica se a barra de progresso está em 100%
                if (binding.progressBar.progress == 100) {
                    if (recompensa) {
                        // Se 'recompensa' é true e progresso é 100%, fixa a barra de progresso e desabilita interações
                        setProgressBarToFullAndDisable()
                    } else {
                        // Se 'recompensa' é false e progresso é 100%, atualiza 'recompensa' para true, chama checkAndSetCompletion e desabilita interações
                        val updates = mapOf("tarefas.$dataSelecionada.recompensa" to true)
                        tarefasRef.update(updates).addOnSuccessListener {
                            checkAndSetCompletion(userId)
                            setProgressBarToFullAndDisable()
                            showDialog()
                        }.addOnFailureListener { e ->
                            Log.e("FirestoreError", "Erro ao atualizar recompensa", e)
                        }
                    }
                }
            } else {
                Log.w("Firestore", "Documento não encontrado para o usuário $userId.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Erro ao acessar o documento de tarefas", exception)
        }
    }

    // Função para fixar a barra de progresso em 100% e desabilitar interações
    private fun setProgressBarToFullAndDisable() {
        binding.progressBar.progress = 100
        binding.progressBar.isEnabled = false // Desabilita interações com a barra de progresso
        binding.sendProgress.isEnabled = false // Desabilita o botão de enviar progresso, se necessário
        binding.InputTask.isEnabled = false
        binding.InputTask.alpha = 0.5f
    }

    private fun showDialog() {
        // Cria uma instância de DialogClothesHistory e exibe o diálogo
        val customDialog = DialogClothesHistory(requireContext())
        customDialog.showDialog()
    }

    private fun noServerDialog() {
        if (!isDialogShown) { // Verifica se o diálogo já foi exibido
            val dialog = DialogNoServerFragment() // Cria o fragmento
            dialog.show(parentFragmentManager, "DialogNoServer") // Exibe o fragmento
            isDialogShown = true // Atualiza o estado para indicar que o diálogo foi exibido
        }
    }

}
