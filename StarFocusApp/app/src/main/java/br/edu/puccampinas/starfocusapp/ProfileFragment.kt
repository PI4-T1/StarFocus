package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
/**
 *  Define a classe Profile
 * @author Ana Carolina
 */
class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: AppCompatTextView
    private val db = FirebaseFirestore.getInstance()

    // Metodo chamado quando o Fragment é criado
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referência à TextView onde o nome de usuário será exibido
        usernameTextView = view.findViewById(R.id.username)

        // Referência ao botão "Saiba Mais"
        val btnSaibaMais = view.findViewById<Button>(R.id.btnsaibamais)

        // Referência ao botão de logout
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Ao clicar no botão, desloga o usuário
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Redireciona o usuário para a tela de login após deslogar
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish() // Finaliza a atividade atual
        }

        // Ao clicar no botão "Saiba Mais", redireciona para a nova Activity
        btnSaibaMais.setOnClickListener {
            val intent = Intent(activity, ProgressReport::class.java)
            startActivity(intent)
        }

        return view
    }

    // Metodo chamado quando o Fragment entra em foco (resumido)
    override fun onResume() {
        super.onResume()
        loadUserData() // Chama a função para carregar os dados do usuário
    }

    // Função que carrega os dados do usuário a partir do Firestore
    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Função que carrega os dados do usuário a partir do Firestore
        db.collection("Pessoas").document(userId).get()
            .addOnSuccessListener { document ->
                // Verifica se o documento existe e obtém o nome de usuário
                if (document != null && document.exists()) {
                    val username = document.getString("monsterName")
                    // Exibe o nome de usuário na TextView ou um valor padrão caso o nome não seja encontrado
                    usernameTextView.text = username ?: "Nome não encontrado"
                } else {
                    // Registra uma mensagem de log caso o documento não seja encontrado
                    Log.d("ProfileFragment", "Nome não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileFragment", "Erro ao recuperar o Nome: ", exception)
            }
    }

    // Função para criar uma nova instância do fragmento
    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
