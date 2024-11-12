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

class ProgressReport : AppCompatActivity() {

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

        // Chama a função suspend dentro de uma coroutine
        lifecycleScope.launch {
            val stringNumeros = obterMetricas()
            // Agora você pode usar stringNumeros como quiser
            Log.d("Metricas", "Valores das métricas: $stringNumeros")
        }

    }

//    private suspend fun initializeClientAndroid(): Boolean {
//        if (isInitializingClient) {
//            return false
//        }
//
//        isInitializingClient = true
//        try {
//            Log.d("HomeFragment", "Iniciando a conexão com o servidor...")
//
//            // Estabelecer a conexão com o servidor em uma thread de fundo
//            val socket = withContext(Dispatchers.IO) {
//                try {
//                    val newSocket = Socket("10.0.2.2", 3000)  // Para emulador (alterar para IP correto se estiver no dispositivo)
//                    Log.d("HomeFragment", "Socket conectado com sucesso.")
//                    newSocket
//                } catch (e: Exception) {
//                    Log.e("HomeFragment", "Falha ao conectar ao servidor", e)
//                    null
//                }
//            }
//
//            // Verificar se o socket foi criado corretamente
//            if (socket == null || !socket.isConnected) {
//                Log.e("HomeFragment", "Erro ao conectar ao servidor, socket nulo ou não conectado.")
//                return false
//            }
//
//            Log.d("HomeFragment", "Tentando criar os streams...")
//
//            // Criar DataInputStream e DataOutputStream para comunicação com o servidor
//            val inputStream = withContext(Dispatchers.IO) {
//                try {
//                    val stream = DataInputStream(socket.getInputStream())
//                    Log.d("HomeFragment", "DataInputStream criado com sucesso.")
//                    stream
//                } catch (e: Exception) {
//                    Log.e("HomeFragment", "Erro ao criar DataInputStream", e)
//                    null
//                }
//            }
//
//            val outputStream = withContext(Dispatchers.IO) {
//                try {
//                    val stream = DataOutputStream(socket.getOutputStream())
//                    Log.d("HomeFragment", "DataOutputStream criado com sucesso.")
//                    stream
//                } catch (e: Exception) {
//                    Log.e("HomeFragment", "Erro ao criar DataOutputStream", e)
//                    null
//                }
//            }
//
//            // Verificar se os streams foram criados corretamente
//            if (inputStream == null || outputStream == null) {
//                Log.e("HomeFragment", "Falha ao criar DataInputStream ou DataOutputStream. Abortando a inicialização.")
//                return false
//            }
//
//            Log.d("HomeFragment", "Streams criados com sucesso, inicializando parceiro...")
//
//            // Inicializar o parceiro e o clienteAndroid com os streams criados
//            parceiro = Parceiro(socket, inputStream, outputStream)
//            Log.d("HomeFragment", "Parceiro criado com sucesso.")
//
//            clienteAndroid = ClienteAndroid(this, parceiro)
//            Log.d("HomeFragment", "clienteAndroid inicializado com sucesso.")
//
//            isClienteAndroidInitialized = true
//            return true // Sucesso na inicialização
//
//        } catch (e: Exception) {
//            Log.e("HomeFragment", "Erro na inicialização do clienteAndroid", e)
//            e.printStackTrace()
//            return false
//        } finally {
//            isInitializingClient = false
//        }
//    }

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
                                        "pendente" -> pendenteCount++
                                        "concluida" -> concluidaCount++
                                        "enviada" -> enviadaCount++
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

}

