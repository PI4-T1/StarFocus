package br.edu.puccampinas.starfocusapp

import android.os.Bundle
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

class BottomsSheetAddTaskFragment(private val onTaskAdded: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddtaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetAddtaskBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Inicializa o FirebaseAuth

        // Obtendo os dados da data completa
        val diaSelecionado = arguments?.getInt("diaSelecionado")
        val mesSelecionado = arguments?.getInt("mesSelecionado")
        val anoSelecionado = arguments?.getInt("anoSelecionado")

        // Formatar a data no formato "dd-MM-yyyy"
        val dataSelecionada = String.format("%02d-%02d-%04d", diaSelecionado, mesSelecionado, anoSelecionado)
        Log.d("BottomSheetAddTask", "Data formatada selecionada: $dataSelecionada")

        binding.buttonSaveTask.setOnClickListener {
            val tarefaTexto = binding.inputtarefa.text.toString()
            val userId = auth.currentUser?.uid

            if (userId == null) {
                Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarefaTexto.isNotEmpty()) {
                // Referência ao documento do usuário na coleção "Tarefas"
                val tarefasRef = db.collection("Tarefas").document(userId)

                tarefasRef.get().addOnSuccessListener { document ->
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<Map<String, Any>>> ?: mutableMapOf()
                    val tarefasDoDia = dataTarefas[dataSelecionada] ?: mutableListOf()

                    // Adiciona a nova tarefa com o campo "concluído" padrão como false
                    val novaTarefa = mapOf(
                        "texto" to tarefaTexto,
                        "concluido" to false
                    )
                    tarefasDoDia.add(novaTarefa)
                    dataTarefas[dataSelecionada] = tarefasDoDia

                    // Atualiza o documento com as tarefas modificadas
                    tarefasRef.set(hashMapOf("tarefas" to dataTarefas), SetOptions.merge())
                        .addOnSuccessListener {
                            binding.inputtarefa.text?.clear()
                            onTaskAdded()
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao adicionar tarefa: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao recuperar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            } else {
                binding.inputtarefa.error = "Por favor, insira uma tarefa."
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}