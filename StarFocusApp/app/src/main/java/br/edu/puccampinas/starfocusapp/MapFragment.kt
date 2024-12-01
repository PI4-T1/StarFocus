package br.edu.puccampinas.starfocusapp

import android.content.Context
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

/**
 * A classe que exibe os botoes de historias bloqueadas e desbloqueadas e m uma mapa.
 * @author Ana Carolina
 */
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
        // Retorna o status da historia
        fetchStoryStatus()
    }

    // Metodo para buscar o status das histórias do usuário no Firebase Firestore. Usa o uid do usuário autenticado.
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
        // Atualiza a visibilidade dos botões de histórias com base nos seus status. Se a história estiver desbloqueada, o botão correspondente será visível.
        // História 1 está sempre desbloqueada
        binding.history1unlock.visibility = View.VISIBLE
        binding.history2unlock.visibility = if (history2) View.VISIBLE else View.GONE
        binding.history2locked.visibility = if (!history2) View.VISIBLE else View.GONE
        binding.history3unlock.visibility = if (history3) View.VISIBLE else View.GONE
        binding.history3locked.visibility = if (!history3) View.VISIBLE else View.GONE
        binding.history4unlock.visibility = if (history4) View.VISIBLE else View.GONE
        binding.history4locked.visibility = if (!history4) View.VISIBLE else View.GONE
        // Se estatus = true, mostra o imageview
        binding.history2novoStatus.visibility = if (history2) View.VISIBLE else View.GONE
        binding.history3novoStatus.visibility = if (history3) View.VISIBLE else View.GONE
        binding.history4novoStatus.visibility = if (history4) View.VISIBLE else View.GONE
        // Redireciona para a história correspondente ao ser desbloqueada

        //Define o comportamento de clique para o botão de desbloqueio das histórias 1,2,3 e 4
        binding.history1unlock.setOnClickListener {
            if (history1) {
                navigateToStoryActivity(HistoryOne::class.java)
            }
        }

        binding.history2unlock.setOnClickListener {
            if (history2) {
                navigateToStoryActivity(HistoryProgressOneVillain::class.java)
                saveButtonVisibilityState()
                binding.history2novoStatus.visibility = View.GONE
            }
        }

        binding.history3unlock.setOnClickListener {
            if (history3) {
                navigateToStoryActivity(HistoryProgressThreeVillain::class.java)
                saveButtonVisibilityState()
                binding.history3novoStatus.visibility = View.GONE
            }
        }

        binding.history4unlock.setOnClickListener {
            if (history4) {
                navigateToStoryActivity(HistoryProgressFiveVillain::class.java)
                saveButtonVisibilityState()
                binding.history4novoStatus.visibility = View.GONE
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
    // Define a função que trata os cliques nos botões bloqueados e exibe uma mensagem de erro.
    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToStoryActivity(storyActivity: Class<*>) {
        activity?.let {
            val intent = Intent(it, storyActivity)
            it.startActivity(intent)
        }
    }
    //Navega para a atividade da história correspondente quando um botão de história desbloqueada é clicado.
    private fun saveButtonVisibilityState() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Salvar a visibilidade dos selos "Novo"
        editor.putBoolean("history2Novo", false)  // History 2 já foi desbloqueada
        editor.putBoolean("history3Novo", false)  // History 3 já foi desbloqueada
        editor.putBoolean("history4Novo", false)  // History 4 já foi desbloqueada
        editor.apply()  // Salva as mudanças
    }

    //Salva o estado de visibilidade dos selos "Novo" em SharedPreferences após o desbloqueio das histórias.
    private fun loadButtonVisibilityState() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Recuperando os valores salvos para os selos "Novo"
        val history2Novo = sharedPreferences.getBoolean("history2Novo", true)  // default é true
        val history3Novo = sharedPreferences.getBoolean("history3Novo", true)  // default é true
        val history4Novo = sharedPreferences.getBoolean("history4Novo", true)  // default é true

        // Atualiza a visibilidade de acordo com o estado
        binding.history2novoStatus.visibility = if (history2Novo) View.VISIBLE else View.GONE
        binding.history3novoStatus.visibility = if (history3Novo) View.VISIBLE else View.GONE
        binding.history4novoStatus.visibility = if (history4Novo) View.VISIBLE else View.GONE
    }



}
