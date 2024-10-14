package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.LoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.widget.EditText
import android.widget.ToggleButton

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
                startActivity(Intent(this@Login, ResetPassword::class.java))
            }
            signUp.setOnClickListener {
                startActivity(Intent(this@Login, SignUp::class.java))
                finish()
            }
        }
        setCursor(binding.idEmail)
        // Configura o botão de alternar visibilidade da senha
        setupPasswordToggle(
            binding.togglePasswordVisibility,
            binding.idSenha,
            InputType.TYPE_CLASS_TEXT)
    }

    // voltar o cursor no início do input
    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Função que altera a visibilidade da senha ao alternar o toggle
     * @authors: Isabella.
     */
    private fun setupPasswordToggle(
        toggleButton: ToggleButton,
        editText: EditText,
        inputType: Int
    ) {
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se o botão está marcado, mostrar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Se o botão não está marcado, ocultar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Para atualizar a exibição do EditText
            editText.text?.let { editText.setSelection(it.length) }
        }
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
