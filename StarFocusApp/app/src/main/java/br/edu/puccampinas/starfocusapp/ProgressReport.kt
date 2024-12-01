package br.edu.puccampinas.starfocusapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.starfocusapp.databinding.ReportProgressBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.text.DateFormatSymbols
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.util.*


/**
 * Classe responsável pela atividade principal que gerencia a navegação por meio de um BottomNavigationView.
 * Contém a lógica de inicialização e transição entre fragmentos, além de gerenciar funcionalidades adicionais,
 * como badges para itens específicos do menu.
 *
 * @author Laís e Ana Carolina
 */
class ProgressReport : AppCompatActivity(), MetricsListener {

    private val binding by lazy { ReportProgressBinding.inflate(layoutInflater) }

    // FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Variável para armazenar o valor de metrics
    private var metrics: String? = null

    private lateinit var clienteAndroid: ClienteAndroid
    private lateinit var parceiro: Parceiro
    private var isClienteAndroidInitialized = false
    private var isInitializingClient = false
    private lateinit var pieChart: PieChart
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Ação do botão de voltar para o perfil, navegando para a tela de perfil
        binding.voltaperfil.setOnClickListener {
            val intent = Intent(this, BottomNav::class.java)
            intent.putExtra("open_profile_fragment", true) // Passa o parâmetro extra
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // Inicia a execução de coroutines para enviar métricas ao servidor após a inicialização
        lifecycleScope.launch {
            // Inicializa o cliente Android antes de tentar enviar as métricas
            val isInitialized = initializeClientAndroid()
            if (isInitialized) {
                // Depois de inicializar, envia as métricas para o servidor
                enviarMetricasParaServidor()
            } else {
                Log.e("ProgressReport", "Falha na inicialização do cliente Android.")
            }
        }

        // Inicializa e configura o gráfico de pizza
        pieChart = binding.pieChartTasks
        setupPieChart()
        loadPieChartData() // Carrega os dados para o gráfico de tarefas
        setupPieChartRecompensas() // Configura o gráfico de recompensas

    }

    /**
     * Configura o gráfico de pizza para mostrar os status das tarefas
     */
    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f // Tamanho do buraco no centro (ajuste conforme necessár
        pieChart.description.isEnabled = false  // Desativa a descrição
        pieChart.setDrawCenterText(false) // Desativa o texto no centro
        pieChart.setDrawEntryLabels(false)
        pieChart.setEntryLabelColor(Color.WHITE)
        // Desativa a legenda
        pieChart.legend.isEnabled = false
        // Desativa a exibição dos valores no gráfico
        pieChart.setDrawSliceText(false)
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.off_white))


    }
    /**
     * Carrega os dados para o gráfico de pizza, como as porcentagens de tarefas
     */
    private fun loadPieChartData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            contarTarefasNoMesAtual(userId) { totalTarefasNoMes ->
                binding.qtdtarefascriadas.text = totalTarefasNoMes.toString()
            }
        }

        // Separação das porcentagens da string 'metrics'
        val porcentagemPendentes = metrics?.substring(0, 3)?.toIntOrNull() ?: 0
        val porcentagemConcluidas = metrics?.substring(3, 6)?.toIntOrNull() ?: 0
        val porcentagemEnviadas = metrics?.substring(6, 9)?.toIntOrNull() ?: 0

        binding.percentageTextConcluidas.text = "${(porcentagemConcluidas)}%"
        binding.percentageTextenviadas.text = "${(porcentagemEnviadas)}%"
        binding.percentageTextpendentes.text = "${(porcentagemPendentes)}%"

        // Adiciona as entradas de dados para o gráfico
        val entries = listOf(
            PieEntry(porcentagemConcluidas.toFloat(), "Concluídas"),
            PieEntry(porcentagemEnviadas.toFloat(), "Enviadas"),
            PieEntry(porcentagemPendentes.toFloat(), "Pendentes")
        )

        // Cria o PieDataSet com as entradas
        val dataSet = PieDataSet(entries, "Status das Tarefas")

        // Define as cores do gráfico
        dataSet.colors = listOf(
            Color.parseColor("#6A4AC4"), // Concluídas
            Color.parseColor("#9FA8DA"), // Enviadas
            Color.parseColor("#C5CAE9")  // Pendentes
        )

        // Desativa os valores nas fatias
        dataSet.setDrawValues(false)

        // Cria o PieData com o PieDataSet
        val data = PieData(dataSet)

        // Atualiza os dados no gráfico
        pieChart.data = data

        // Atualiza o gráfico
        pieChart.invalidate()
    }

    /**
     * Configura o gráfico de pizza de recompensas
     */
    private fun setupPieChartRecompensas() {
        // Obtém a referência do gráfico de pizza usando o ID do componente no layout
        val pieChartRecompensas = findViewById<PieChart>(R.id.pieChartRecompensas)

        // Desativa a descrição e a legenda
        pieChartRecompensas.description.isEnabled = false
        pieChartRecompensas.legend.isEnabled = false
        pieChartRecompensas.holeRadius = 40f
        pieChartRecompensas.setHoleColor(ContextCompat.getColor(this, R.color.off_white))

        // Obtém o ID do usuário atual autenticado no Firebase
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Chama a função que calcula o total de dias com tarefas e dias com recompensas
            contarDiasComTarefasERecompensa(userId) { diasComTarefas, diasComRecompensa ->

                // Converte os dias com tarefas e dias com recompensas para valores float
                val totalRecompensas = diasComTarefas.toFloat() // Total de dias com tarefas
                val recompensatotal = diasComTarefas.toFloat() // Ou outro cálculo conforme necessário
                val recompensaobtida = diasComRecompensa.toFloat() // Dias com recompensa

                // Atualiza o texto sobre o status de recompensas no layout
                binding.textsobrerecompensas.text = "${recompensaobtida.toInt()} " +
                        "recompensas obtidas de ${recompensatotal.toInt()} disponíveis"

                // Calcula o progresso de recompensas obtidas como uma fração do total
                val progressoObtido = if (totalRecompensas > 0) recompensaobtida / totalRecompensas else 0f

                // Calcula o progresso restante, subtraindo o progresso obtido de 1 (100%)
                val progressoRestante = 1f - progressoObtido

                //  // Cria uma lista de entradas (PieEntry) representando os dados do gráfico
                val entries = listOf(
                    PieEntry(progressoObtido, "Obtidas"),
                    PieEntry(progressoRestante, "Restantes")
                )

                // Cria o DataSet para o gráfico
                val dataSet = PieDataSet(entries, "Recompensas")
                // Desativa os textos nas fatias
                dataSet.setDrawValues(false)

                // Desativa as labels nas fatias (texto nas fatias)
                pieChartRecompensas.setDrawSliceText(false)

                // Define as cores para cada categoria usando ContextCompat
                dataSet.colors = listOf(
                    ContextCompat.getColor(this, R.color.recompensatotal), // Cor para "Total"
                    ContextCompat.getColor(this, R.color.recompensaobtida) // Cor para "Obtidas"
                )

                // Cria o PieData e atribui ao gráfico
                val data = PieData(dataSet)
                pieChartRecompensas.data = data

                // Calcula o percentual de recompensas obtidas e define como texto central no gráfico
                val totalPercent = if (totalRecompensas > 0) {
                    (recompensaobtida / totalRecompensas) * 100
                } else {
                    0f
                }
                // Exibe o percentual no centro do gráfico
                pieChartRecompensas.centerText = "${"%.1f".format(totalPercent)}%"
                pieChartRecompensas.setCenterTextSize(12f) // Define o tamanho da fonte do texto central
                dataSet.valueTextColor = ContextCompat.getColor(this, R.color.dark_grey) // Cor do texto
                val typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
                pieChartRecompensas.setCenterTextTypeface(typeface)

                // Atualiza o gráfico
                pieChartRecompensas.invalidate()
            }
        }
    }

    // Função suspensa para inicializar o cliente Android. Retorna verdadeiro se a inicialização for bem-sucedida.
    private suspend fun initializeClientAndroid(): Boolean {
        // Verifica se o processo de inicialização já está em andamento para evitar inicializações simultâneas
        if (isInitializingClient) {
            return false
        }

        // Marca que a inicialização está em andamento
        isInitializingClient = true
        try {
            Log.d("ProgressReport", "Iniciando a conexão com o servidor...")

            // // Estabelece a conexão com o servidor de forma assíncrona usando o Dispatchers.IO para operações de I/O
            val socket = withContext(Dispatchers.IO) {
                try {
                    val newSocket = Socket("192.168.15.58", 3000) // Para emulador, altere para o IP do servidor em um dispositivo real
                    Log.d("ProgressReport", "Socket conectado com sucesso.")
                    newSocket
                } catch (e: Exception) {
                    Log.e("ProgressReport", "Falha ao conectar ao servidor", e)
                    null
                }
            }

            // Verifica se o socket foi criado corretamente
            if (socket == null || !socket.isConnected) {
                Log.e("ProgressReport", "Erro ao conectar ao servidor, socket nulo ou não conectado.")
                return false
            }

            Log.d("ProgressReport", "Tentando criar os streams...")

            // Criar DataInputStream e DataOutputStream para comunicação com o servidor
            val inputStream = withContext(Dispatchers.IO) {
                try {
                    // Tenta criar um DataInputStream a partir do socket
                    val stream = DataInputStream(socket.getInputStream())
                    Log.d("ProgressReport", "DataInputStream criado com sucesso.")
                    stream
                } catch (e: Exception) {
                    Log.e("ProgressReport", "Erro ao criar DataInputStream", e)
                    null
                }
            }

            // Cria o DataOutputStream para enviar dados através do socket de forma assíncrona
            val outputStream = withContext(Dispatchers.IO) {
                try {
                    // Tenta criar um DataOutputStream a partir do socket
                    val stream = DataOutputStream(socket.getOutputStream())
                    Log.d("ProgressReport", "DataOutputStream criado com sucesso.")
                    stream
                } catch (e: Exception) {
                    Log.e("ProgressReport", "Erro ao criar DataOutputStream", e)
                    null
                }
            }

            // Verifica se os streams foram criados corretamente
            if (inputStream == null || outputStream == null) {
                Log.e("ProgressReport", "Falha ao criar DataInputStream ou DataOutputStream.")
                return false
            }

            Log.d("ProgressReport", "Streams criados com sucesso, inicializando parceiro...")

            // Inicializa o parceiro sem o clienteAndroid
            parceiro = Parceiro(socket, inputStream, outputStream)
            Log.d("ProgressReport", "Parceiro criado com sucesso.")

            clienteAndroid = ClienteAndroid(null, this, parceiro)

            Log.d("HomeFragment", "clienteAndroid inicializado com sucesso.")

            isClienteAndroidInitialized = true
            return true // Sucesso na inicialização
        } catch (e: Exception) {
            Log.e("ProgressReport", "Erro na inicialização do clienteAndroid", e)
            return false
        } finally {
            isInitializingClient = false
        }
    }

    // Implementação do metodo da interface MetricsListener
    override fun onMetricsUpdate(metrics: String) {
        runOnUiThread {
            // Armazena o valor recebido na variável global
            this.metrics = metrics

            Log.d("ProgressReport", "String de métrica: $metrics")
            loadPieChartData()
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
                        val dataTarefas = document.get("tarefas") as? Map<String, MutableMap<String, Any>> ?: return@addOnSuccessListener

                        // Filtra e conta as tarefas para o mês/ano atual com base no status
                        dataTarefas.forEach { (data, diaData) ->
                            val tarefasList = diaData["lista"] as? List<Map<String, Any>> ?: return@forEach
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

    private suspend fun enviarMetricasParaServidor() {
        val stringNumeros = obterMetricas()  // Obtém as métricas como string
        Log.d("Metricas", "Valores das métricas: $stringNumeros")

        // Verifica se o clienteAndroid está inicializado antes de enviar os dados
        if (isClienteAndroidInitialized) {
            // Envia as métricas para o servidor
            clienteAndroid.sendMetrics(stringNumeros)
        } else {
            Log.e("ProgressReport", "ClienteAndroid não está inicializado")
        }
    }

    // Função que conta os dias com tarefas e os dias com recompensa, recebendo o ID do usuário e um callback para retornar os resultados
    private fun contarDiasComTarefasERecompensa(userId: String, onResult: (diasComTarefas: Int, diasComRecompensa: Int) -> Unit) {
        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        // Realiza uma leitura assíncrona do documento de tarefas do usuário
        tarefasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Acessa o mapa de tarefas
                val tarefas = document.get("tarefas") as? Map<String, Map<String, Any>> ?: emptyMap()

                // Inicializa contadores para os dias com tarefas e com recompensa
                var diasComTarefas = 0
                var diasComRecompensa = 0

                // Obtem o mês e o ano atuais
                val hoje = Calendar.getInstance()
                val anoAtual = hoje.get(Calendar.YEAR)
                val mesAtual = hoje.get(Calendar.MONTH) + 1 // Janeiro é 0

                // Percorre todos os dias no mapa de tarefas
                for ((dataSelecionada, tarefaData) in tarefas) {
                    // Extrai o dia, mês e ano da data selecionada
                    val partesData = dataSelecionada.split("-")
                    if (partesData.size == 3) {
                        val dia = partesData[0].toIntOrNull() // Não será usado aqui, mas pode ser útil
                        val mes = partesData[1].toIntOrNull()
                        val ano = partesData[2].toIntOrNull()

                        // Verifica se a data pertence ao mês e ano atual
                        if (ano == anoAtual && mes == mesAtual) {
                            // Verifica se há ao menos uma tarefa no dia
                            val tarefasDoDia = tarefaData["lista"] as? List<Map<String, Any>> ?: emptyList()
                            if (tarefasDoDia.isNotEmpty()) {
                                diasComTarefas++ // Se houver tarefas, incrementa o contador de dias com tarefas

                                // Verifica se o campo 'recompensa' é true
                                val recompensa = tarefaData["recompensa"] as? Boolean ?: false
                                if (recompensa) {
                                    diasComRecompensa++// Se a recompensa for verdadeira, incrementa o contador de dias com recompensa
                                }
                            }
                        }
                    }
                }

                // Retorna o resultado para o callback
                onResult(diasComTarefas, diasComRecompensa)

            } else {
                Log.w("Firestore", "Documento não encontrado para o usuário $userId.")
                onResult(0, 0) // Retorna 0 se o documento não for encontrado
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Erro ao acessar o documento de tarefas", exception)
            onResult(0, 0) // Retorna 0 em caso de falha
        }
    }

    // Função que conta o número total de tarefas no mês atual, recebendo o ID do usuário e um callback para retornar o resultado
    private fun contarTarefasNoMesAtual(userId: String, onResult: (totalTarefasNoMes: Int) -> Unit) {
        // Obtém o mês e o ano atuais
        val calendar = Calendar.getInstance()
        val anoAtual = calendar.get(Calendar.YEAR)
        val mesAtual = calendar.get(Calendar.MONTH) + 1 // Janeiro é 0, então somamos 1 para ficar no intervalo correto

        // Obtem o nome do mês atual
        val nomeDoMes = DateFormatSymbols().months[mesAtual - 1]

        //Altera o text do mês atual
        binding.mes.text = "Em $nomeDoMes"

        // Referência ao documento de tarefas do usuário no Firestore
        val tarefasRef = db.collection("Tarefas").document(userId)

        tarefasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Acessa o mapa de tarefas
                val tarefas = document.get("tarefas") as? Map<String, Map<String, Any>> ?: emptyMap()

                var totalTarefasNoMes = 0

                // Percorre todas as datas no mapa de tarefas
                for ((dataSelecionada, tarefaData) in tarefas) {
                    // Converte a data no formato dd-MM-yyyy para acessar mês e ano
                    val partesData = dataSelecionada.split("-")
                    val dia = partesData.getOrNull(0)?.toIntOrNull() ?: 0
                    val mes = partesData.getOrNull(1)?.toIntOrNull() ?: 0
                    val ano = partesData.getOrNull(2)?.toIntOrNull() ?: 0

                    // Verifica se a tarefa pertence ao mês e ano atual
                    if (mes == mesAtual && ano == anoAtual) {
                        // Conta quantas tarefas existem no dia
                        val listaDeTarefas = tarefaData["lista"] as? List<Map<String, Any>> ?: emptyList()
                        totalTarefasNoMes += listaDeTarefas.size
                    }
                }

                // Retorna o resultado para o callback
                onResult(totalTarefasNoMes)

            } else {
                Log.w("Firestore", "Documento não encontrado para o usuário $userId.")
                onResult(0) // Retorna 0 se o documento não for encontrado
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Erro ao acessar o documento de tarefas", exception)
            onResult(0) // Retorna 0 em caso de falha
        }
    }

}