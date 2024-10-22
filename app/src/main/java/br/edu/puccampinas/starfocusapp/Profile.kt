package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Referência ao botão de logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Ao clicar no botão, desloga o usuário
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Redireciona o usuário para a tela de login após deslogar
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finaliza a atividade atual
        }
    }
}
