package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.LoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.widget.EditText

// Tela de Login
class Login : AppCompatActivity() {
    // ViewBinding
    private val binding by lazy { LoginBinding.inflate(layoutInflater) }
    // FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            btnLogin.setOnClickListener {
                userAuthentication(idEmail.text.toString(), idSenha.text.toString())
            }
            remember.setOnClickListener {
                rememberPassword(idEmail.text.toString())
            }
            signUp.setOnClickListener {
                startActivity(Intent(this@Login, SignUp::class.java))
                finish()
            }
        }
        setCursor(binding.idEmail)
    }

    // voltar o cursor no início do input
    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }
    // enviará para a activity de recuperar senha obrigatoriamente com o email ja escrito no campo
    private fun rememberPassword(email: String) {
        if (email.isBlank())
        {
            showToast("Digite um email para a recuperação de senha!")
            return
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, ResetPassword::class.java).putExtra("email", email))
                finish()
            } else {
                showToast("Endereço de email atual inválido!")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun userAuthentication(email: String, password: String) {
        if (email.isBlank() or password.isBlank()) {
            showToast("Por favor, preencha os campos antes!")
            return
        }
        // autenticação
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // SUCESSO
                startActivity(Intent(this, NameMonster::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                // Trata falhas durante o processo de login
                if (exception.message.toString() == "The email address is badly formatted.") {
                    // Verifica se o formato do email está incorreto
                    showToast("Endereço de email inválido, por favor digite novamente!")
                } else {
                    // Se a falha não for relacionada ao formato do email, exibe mensagem de erro genérica
                    showToast("Email ou senha incorretos, por favor digite novamente!")
                }
            }
    }
}
