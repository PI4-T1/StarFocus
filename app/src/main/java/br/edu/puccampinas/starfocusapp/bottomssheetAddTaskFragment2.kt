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

class BottomsSheetAddTaskFragment2(private val onTaskAdded: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddtask2Binding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetAddtask2Binding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Inicializa o FirebaseAuth

        // Máximo de caracteres permitido
        val maxLength = 50

        // Inicializa o botão como desativado e opaco
        binding.buttonSaveTask.isEnabled = false
        binding.buttonSaveTask.alpha = 0.5f

        // Configura o TextWatcher para atualizar o contador de caracteres
        binding.inputtarefa2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.charCountTextView2.text = "$currentLength/$maxLength"

                if (currentLength == 0 || currentLength > maxLength) {
                    binding.charCountTextView2.setTextColor(resources.getColor(R.color.red, null))
                    binding.inputtarefa2.setTextColor(resources.getColor(R.color.red, null))
                    binding.buttonSaveTask.isEnabled = false
                    binding.buttonSaveTask.alpha = 0.5f  // Reduz a opacidade para indicar que está desativado
                } else {
                    binding.charCountTextView2.setTextColor(resources.getColor(R.color.black, null))
                    binding.inputtarefa2.setTextColor(resources.getColor(R.color.black, null))
                    binding.buttonSaveTask.isEnabled = true
                    binding.buttonSaveTask.alpha = 1.0f  // Restaura a opacidade normal
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Configura o botão para hoje
        binding.buttonToday2.setOnClickListener {
            val currentDate = LocalDate.now()
            selectedDate = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            binding.textDaySelected.text = "A tarefa será adicionada em: $selectedDate" // Atualiza o TextView
            Log.d("BottomSheetAddTask", "Data selecionada: $selectedDate")
        }

// Configura o botão para escolher a data
        binding.buttonchoose2.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                    binding.textDaySelected.text = "A tarefa será adicionada em: $selectedDate" // Atualiza o TextView
                    Log.d("BottomSheetAddTask", "Data selecionada: $selectedDate")
                },
                LocalDate.now().year,
                LocalDate.now().monthValue - 1,
                LocalDate.now().dayOfMonth
            )
            datePickerDialog.show()
        }

        // Configura o listener do botão de salvar tarefa
        binding.buttonSaveTask.setOnClickListener {
            val tarefaTexto = binding.inputtarefa2.text.toString()
            val userId = auth.currentUser?.uid

            if (userId == null) {
                Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarefaTexto.isNotEmpty() && selectedDate.isNotEmpty()) {
                // Referência ao documento do usuário na coleção "Tarefas"
                val tarefasRef = db.collection("Tarefas").document(userId)

                tarefasRef.get().addOnSuccessListener { document ->
                    val dataTarefas = document.get("tarefas") as? MutableMap<String, MutableList<Map<String, Any>>> ?: mutableMapOf()
                    val tarefasDoDia = dataTarefas[selectedDate] ?: mutableListOf()

                    // Adiciona a nova tarefa com o campo "concluído" padrão como false
                    val novaTarefa = mapOf(
                        "texto" to tarefaTexto,
                        "concluido" to false
                    )
                    tarefasDoDia.add(novaTarefa)
                    dataTarefas[selectedDate] = tarefasDoDia

                    // Atualiza o documento com as tarefas modificadas
                    tarefasRef.set(hashMapOf("tarefas" to dataTarefas), SetOptions.merge())
                        .addOnSuccessListener {
                            binding.inputtarefa2.text?.clear()
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
                binding.inputtarefa2.error = "Por favor, insira uma tarefa e selecione uma data."
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
