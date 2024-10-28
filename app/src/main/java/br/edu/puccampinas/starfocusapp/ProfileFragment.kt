package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: AppCompatTextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referência à TextView onde o nome de usuário será exibido
        usernameTextView = view.findViewById(R.id.username)

        // Referência ao botão de trocar nome
        val btnChangeName = view.findViewById<AppCompatImageButton>(R.id.changenamepage)

        // Ao clicar no botão, navega para a tela de troca de nome
        btnChangeName.setOnClickListener {
            val intent = Intent(activity, ChangeName::class.java)
            startActivity(intent)
        }

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

        return view
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // Chama a função para carregar os dados do usuário
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Pessoas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("monsterName")
                    usernameTextView.text = username ?: "Nome não encontrado"
                } else {
                    Log.d("ProfileFragment", "Nome não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ProfileFragment", "Erro ao recuperar o Nome: ", exception)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
