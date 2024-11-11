package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.edu.puccampinas.starfocusapp.databinding.BottomsheetAddtaskBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Fragmento para adicionar uma nova tarefa.
 *
 * Esta classe exibe uma tela de BottomSheet que permite ao usuário inserir uma tarefa para um dia específico.
 * As tarefas são salvas no Firestore sob a coleção "Tarefas" do usuário.
 *
 * @param onTaskAdded Função de callback chamada quando uma tarefa é adicionada com sucesso.
 * @author Lais
 */
class BottomsSheetAddTaskFragment(private val onTaskAdded: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddtaskBinding? = null  // Variável para manter a referência ao binding
    private val binding get() = _binding!!  // Getter do binding não-nulo
    private lateinit var db: FirebaseFirestore  // Instância do Firestore para manipulação do banco de dados
    private lateinit var auth: FirebaseAuth  // Instância do FirebaseAuth para obter o usuário autenticado

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
    ): View? {
        _binding = BottomsheetAddtaskBinding.inflate(inflater, container, false)  // Inflar o layout
        db = FirebaseFirestore.getInstance()  // Inicializa a instância do Firestore
        auth = FirebaseAuth.getInstance()  // Inicializa a instância do FirebaseAuth

        // Limite de caracteres para o campo de tarefa
        val maxLength = 50

        // Inicializa o botão como desativado e opaco
        binding.buttonSaveTask.isEnabled = false
        binding.buttonSaveTask.alpha = 0.5f

        // Configura um TextWatcher para atualizar o contador de caracteres e validar o texto da tarefa
        binding.inputtarefa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0  // Obtém o comprimento atual do texto
                binding.charCountTextView.text = "$currentLength/$maxLength"  // Atualiza o contador de caracteres

                // Verifica se o texto está vazio ou excede o limite de caracteres
                if (currentLength == 0 || currentLength > maxLength) {
                    binding.charCountTextView.setTextColor(resources.getColor(R.color.red, null))  // Muda para cor vermelha
                    binding.inputtarefa.setTextColor(resources.getColor(R.color.red, null))  // Muda para cor vermelha
                    binding.buttonSaveTask.isEnabled = false  // Desativa o botão
                    binding.buttonSaveTask.alpha = 0.5f  // Torna o botão opaco
                } else {
                    // Se o texto está dentro do limite, ativa o botão e restaura as cores
                    binding.charCountTextView.setTextColor(resources.getColor(R.color.black, null))
                    binding.inputtarefa.setTextColor(resources.getColor(R.color.black, null))
                    binding.buttonSaveTask.isEnabled = true  // Ativa o botão
                    binding.buttonSaveTask.alpha = 1.0f  // Restaura a opacidade
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Obtém os dados da data selecionada (dia, mês, ano)
        val diaSelecionado = arguments?.getInt("diaSelecionado")
        val mesSelecionado = arguments?.getInt("mesSelecionado")
        val anoSelecionado = arguments?.getInt("anoSelecionado")

        // Formata a data no formato "dd-MM-yyyy"
        val dataSelecionada = String.format("%02d-%02d-%04d", diaSelecionado, mesSelecionado, anoSelecionado)
        Log.d("BottomSheetAddTask", "Data formatada selecionada: $dataSelecionada")  // Log para depuração

        // Ação ao clicar no botão de salvar tarefa
        binding.buttonSaveTask.setOnClickListener {
            val tarefaTexto = binding.inputtarefa.text.toString()  // Obtém o texto inserido pelo usuário
            val userId = auth.currentUser?.uid  // Obtém o ID do usuário autenticado

            if (userId == null) {
                Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                return@setOnClickListener
            }

            if (tarefaTexto.isNotEmpty()) {
                // Referência ao documento de tarefas do usuário no Firestore
                val tarefasRef = db.collection("Tarefas").document(userId)

                tarefasRef.get().addOnSuccessListener { document ->
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<Map<String, Any>>> ?: mutableMapOf()
                    val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()  // Obtém ou cria a lista de tarefas para o dia

                    // Cria um ID único para a nova tarefa
                    val tarefaId = db.collection("Tarefas").document().id

                    // Cria um mapa para a nova tarefa
                    val novaTarefa = mapOf(
                        "id" to tarefaId,
                        "texto" to tarefaTexto,
                        "status" to "Pendente"  // Status inicial da tarefa
                    )
                    tarefasDoDia.add(novaTarefa)  // Adiciona a tarefa na lista de tarefas do dia
                    dataTarefas[dataSelecionada] = tarefasDoDia  // Atualiza o mapa de tarefas para o dia selecionado

                    // Atualiza o Firestore com as tarefas modificadas
                    tarefasRef.set(hashMapOf("tarefas" to dataTarefas), SetOptions.merge())
                        .addOnSuccessListener {
                            binding.inputtarefa.text?.clear()  // Limpa o campo de texto
                            onTaskAdded()  // Chama o callback para indicar que a tarefa foi adicionada
                            dismiss()  // Fecha o BottomSheet
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao adicionar tarefa: ${e.message}", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                        }

                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao recuperar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()  // Mensagem de erro
                }

            } else {
                binding.inputtarefa.error = "Por favor, insira uma tarefa."  // Mensagem de erro se o campo estiver vazio
            }
        }

        return binding.root  // Retorna a view do fragmento
    }

    /**
     * Limpa o binding quando a view for destruída.
     * @author Lais
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpa a referência ao binding
    }
}
