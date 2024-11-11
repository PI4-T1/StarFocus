package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.edu.puccampinas.starfocusapp.databinding.FragmentMapBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding // Declare o binding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa o binding corretamente
        binding = FragmentMapBinding.inflate(inflater, container, false)

        // Retorna a root view do binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchStoryStatus()
    }

    private fun fetchStoryStatus() {
        val userId = auth.currentUser?.uid ?: return
        // Recuperando os documentos da coleção "Pessoas" do Firebase
        db.collection("Pessoas").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                // Obter o status das histórias
                val history1 = true // A história 1 está sempre desbloqueada
                val history2 = document.getBoolean("history2") ?: false
                val history3 = document.getBoolean("history3") ?: false
                val history4 = document.getBoolean("history4") ?: false

                // Logando os valores para verificar
                Log.d("MapFragment", "History2: $history2, History3: $history3, History4: $history4")

                // Passa os valores para a função que atualiza a visibilidade
                updateButtonVisibility(history1, history2, history3, history4)
            }
        }
            .addOnFailureListener {
                // Tratamento de falhas
                Log.e("MapFragment", "Erro ao buscar dados do usuário", it)
            }
    }

    private fun updateButtonVisibility(history1: Boolean, history2: Boolean, history3: Boolean, history4: Boolean) {
        // História 1 está sempre desbloqueada
        binding.history1unlock.visibility = View.VISIBLE
        binding.history2unlock.visibility = if (history2) View.VISIBLE else View.GONE
        binding.history2locked.visibility = if (!history2) View.VISIBLE else View.GONE
        binding.history3unlock.visibility = if (history3) View.VISIBLE else View.GONE
        binding.history3locked.visibility = if (!history3) View.VISIBLE else View.GONE
        binding.history4unlock.visibility = if (history4) View.VISIBLE else View.GONE
        binding.history4locked.visibility = if (!history4) View.VISIBLE else View.GONE

        // Redireciona para a história correspondente ao ser desbloqueada
        binding.history1unlock.setOnClickListener {
            if (history1) {
                navigateToStoryActivity(HistoryOne::class.java)
            }
        }

        binding.history2unlock.setOnClickListener {
            if (history2) {
                navigateToStoryActivity(HistoryProgressOneVillain::class.java)
            }
        }

        binding.history3unlock.setOnClickListener {
            if (history3) {
                navigateToStoryActivity(HistoryProgressThreeVillain::class.java)
            }
        }

        binding.history4unlock.setOnClickListener {
            if (history4) {
                navigateToStoryActivity(HistoryProgressFiveVillain::class.java)
            }
        }

        // Lógica de clique nos botões bloqueados
        handleLockedButtonClick(binding.history2locked, "Você precisa desbloquear essa história primeiro!")
        handleLockedButtonClick(binding.history3locked, "Você precisa desbloquear essa história primeiro!")
        handleLockedButtonClick(binding.history4locked, "Você precisa desbloquear essa história primeiro!")
    }

    // Função para tratar clique em botões bloqueados e mostrar mensagem de erro
    private fun handleLockedButtonClick(button: View, message: String) {
        button.setOnClickListener {
            showErrorMessage(message)
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToStoryActivity(storyActivity: Class<*>) {
        activity?.let {
            val intent = Intent(it, storyActivity)
            it.startActivity(intent)
        }
    }
}
