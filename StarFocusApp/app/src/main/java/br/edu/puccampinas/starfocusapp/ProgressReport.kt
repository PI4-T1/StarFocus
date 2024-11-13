package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.puccampinas.starfocusapp.databinding.ReportProgressBinding
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

class ProgressReport : AppCompatActivity(), MetricsListener {

    private val binding by lazy { ReportProgressBinding.inflate(layoutInflater) }

    // FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var clienteAndroid: ClienteAndroid
    private lateinit var parceiro: Parceiro
    private var isClienteAndroidInitialized = false
    private var isInitializingClient = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

    }

    private suspend fun initializeClientAndroid(): Boolean {
        if (isInitializingClient) {
            return false
        }

        isInitializingClient = true
        try {
            Log.d("ProgressReport", "Iniciando a conexão com o servidor...")

            // Estabelecer a conexão com o servidor
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
                    val stream = DataInputStream(socket.getInputStream())
                    Log.d("ProgressReport", "DataInputStream criado com sucesso.")
                    stream
                } catch (e: Exception) {
                    Log.e("ProgressReport", "Erro ao criar DataInputStream", e)
                    null
                }
            }

            val outputStream = withContext(Dispatchers.IO) {
                try {
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

    // Implementação do método da interface MetricsListener
    override fun onMetricsUpdate(metrics: String) {
        runOnUiThread {
            binding.stringmetrics.text = metrics
            Log.d("HomeFragment", "String de métrica: $metrics")
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


}

