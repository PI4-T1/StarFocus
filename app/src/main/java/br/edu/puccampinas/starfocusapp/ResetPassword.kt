package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.ResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {

    // ViewBinding
    private val binding by lazy { ResetPasswordBinding.inflate(layoutInflater) }
    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {

            btnSend.setOnClickListener {
                forgotPassword(idEmail.text.toString())
            }

            signUp.setOnClickListener {
                finish()
            }
        }
    }

    private fun forgotPassword(email: String) {
        if (email.isBlank()) {   // Verifica se o campo de e-mail está em branco
            Toast.makeText(this, "Digite o endereço de email", Toast.LENGTH_LONG).show()
            return
        }
        // Define o idioma para português
        FirebaseAuth.getInstance().setLanguageCode("pt")
        // Envia um e-mail de redefinição de senha para o endereço fornecido
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Se a operação for bem-sucedida, mostra um dialog e volta para a tela de login
                Toast.makeText(this, "Email enviado", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@ResetPassword, Login::class.java))
                finish()

            } else {
                // Se a operação falhar, exibe uma mensagem de erro
                Toast.makeText(this, "Endereço de email inválido!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
