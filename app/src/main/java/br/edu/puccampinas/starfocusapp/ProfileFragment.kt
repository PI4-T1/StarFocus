package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referência ao botão de logout
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Ao clicar no botão, desloga o usuário
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Redireciona o usuário para a tela de login após deslogar
            val intent = Intent(activity, Login::class.java) // Use "activity" para referir-se ao contexto da Activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish() // Finaliza a atividade atual
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
