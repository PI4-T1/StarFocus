package br.edu.puccampinas.starfocusapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.edu.puccampinas.starfocusapp.databinding.BottomsheetAddtask2Binding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Fragmento para adicionar uma nova tarefa com uma data selecionada.
 *
 * Esta classe exibe uma tela de BottomSheet onde o usuário pode inserir uma tarefa e escolher a data
 * em que ela será realizada. A tarefa é salva na coleção "Tarefas" do Firestore.
 *
 * @param onTaskAdded Função de callback chamada quando uma tarefa
 * é adicionada com sucesso, passando a data da tarefa.
 * @author Lais
 */
class BottomsSheetAddTaskFragment2(
    private val onTaskAdded: (String) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddtask2Binding? = null  // Variável para manter a referência ao binding
    private val binding get() = _binding!!  // Getter para o binding não-nulo
    private lateinit var db: FirebaseFirestore  // Instância do Firestore para manipulação do banco de dados
    private lateinit var auth: FirebaseAuth  // Instância do FirebaseAuth para autenticação do usuário
    private var selectedDate: String = ""  // Variável para armazenar a data selecionada

    /**
     * Cria a view do BottomSheet e inicializa os componentes necessários.
     *
     * @param inflater LayoutInflater usado para inflar a view.
     * @param container Container que irá segurar a view inflada.
     * @param savedInstanceState Estado salvo do fragmento (não utilizado aqui).
     * @author Lais
     * @return A view inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetAddtask2Binding.inflate(inflater, container, false)  // Infla o layout do fragmento
        db = FirebaseFirestore.getInstance()  // Inicializa o Firestore
        auth = FirebaseAuth.getInstance()  // Inicializa o FirebaseAuth

        // Limite de caracteres para o campo de texto
        val maxLength = 50

        // Inicializa o botão como desativado e opaco
        binding.buttonSaveTask.isEnabled = false
        binding.buttonSaveTask.alpha = 0.5f

        // Configura o TextWatcher para atualizar o contador de caracteres e validar o texto
        binding.inputtarefa2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0  // Obtém o comprimento do texto inserido
                binding.charCountTextView2.text = "$currentLength/$maxLength"  // Atualiza o contador de caracteres

                // Verifica se o texto está vazio ou excede o limite de caracteres
                if (currentLength == 0 || currentLength > maxLength) {
                    binding.charCountTextView2.setTextColor(resources.getColor(R.color.red, null))  // Muda para vermelho
                    binding.inputtarefa2.setTextColor(resources.getColor(R.color.red, null))  // Muda para vermelho
                    binding.buttonSaveTask.isEnabled = false  // Desativa o botão
                    binding.buttonSaveTask.alpha = 0.5f  // Torna o botão opaco
                } else {
                    binding.charCountTextView2.setTextColor(resources.getColor(R.color.black, null))  // Restaura cor preta
                    binding.inputtarefa2.setTextColor(resources.getColor(R.color.black, null))  // Restaura cor preta
                    binding.buttonSaveTask.isEnabled = true  // Habilita o botão
                    binding.buttonSaveTask.alpha = 1.0f  // Restaura opacidade do botão
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Configura o botão para usar a data de hoje
        binding.buttonToday2.setOnClickListener {
            val currentDate = LocalDate.now()  // Obtém a data atual
            selectedDate = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))  // Formata a data
            binding.textDaySelected.text = "A tarefa será adicionada em: $selectedDate"  // Exibe a data selecionada
            Log.d("BottomSheetAddTask", "Data selecionada: $selectedDate")  // Log para depuração
        }

        // Configura o botão para escolher a data
        binding.buttonchoose2.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Formata a data escolhida
                    selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                    binding.textDaySelected.text = "A tarefa será adicionada em: $selectedDate"  // Exibe a data escolhida
                    Log.d("BottomSheetAddTask", "Data selecionada: $selectedDate")  // Log para depuração
                },
                LocalDate.now().year,  // Ano atual
                LocalDate.now().monthValue - 1,  // Mês atual (ajustado para 0-indexed)
                LocalDate.now().dayOfMonth  // Dia atual
            )
            // Define a data mínima como a data atual para desabilitar dias anteriores
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()  // Limita a seleção para o futuro

            datePickerDialog.show()  // Exibe o DatePickerDialog
        }

        // Configura o listener para o botão de salvar tarefa
        binding.buttonSaveTask.setOnClickListener {
            val tarefaTexto = binding.inputtarefa2.text.toString()  // Obtém o texto inserido pelo usuário
            val userId = auth.currentUser?.uid  // Obtém o ID do usuário autenticado

            // Verifica se o usuário está autenticado
            if (userId == null) {
                Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                return@setOnClickListener
            }

            // Verifica se o texto da tarefa e a data foram preenchidos
            if (tarefaTexto.isNotEmpty() && selectedDate.isNotEmpty()) {
                val tarefasRef = db.collection("Tarefas").document(userId)  // Referência ao documento de tarefas do usuário

                tarefasRef.get().addOnSuccessListener { document ->
                    // Obtém as tarefas armazenadas ou cria um novo mapa vazio
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<Map<String, Any>>> ?: mutableMapOf()
                    val tarefasDoDia = dataTarefas[selectedDate] ?: mutableListOf()  // Obtém ou cria uma lista de tarefas para o dia selecionado

                    // Cria um ID único para a nova tarefa
                    val tarefaId = db.collection("Tarefas").document().id

                    // Cria a tarefa a ser adicionada
                    val novaTarefa = mapOf(
                        "id" to tarefaId,
                        "texto" to tarefaTexto,
                        "status" to "Pendente"  // Status inicial da tarefa
                    )

                    tarefasDoDia.add(novaTarefa)  // Adiciona a nova tarefa à lista
                    dataTarefas[selectedDate] = tarefasDoDia  // Atualiza as tarefas para o dia selecionado

                    // Atualiza o Firestore com as tarefas modificadas
                    tarefasRef.set(hashMapOf("tarefas" to dataTarefas), SetOptions.merge())
                        .addOnSuccessListener {
                            binding.inputtarefa2.text?.clear()  // Limpa o campo de texto
                            onTaskAdded(selectedDate)  // Chama a função de callback
                            dismiss()  // Fecha o BottomSheet
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao adicionar tarefa: ${e.message}", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                        }

                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao recuperar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                }

            } else {
                binding.inputtarefa2.error = "Por favor, insira uma tarefa e selecione uma data."  // Exibe erro caso o campo esteja vazio
            }
        }

        return binding.root  // Retorna a view inflada do fragmento
    }

    /**
     * Limpa o binding quando a view for destruída.
     * @author Lais
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpa a referência ao binding para evitar vazamentos de memória
    }
}
