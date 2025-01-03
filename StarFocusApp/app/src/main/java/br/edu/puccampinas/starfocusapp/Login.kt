package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.LoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.widget.EditText
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatImageView
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A classe trata da autenticação do usuário utilizando Firebase e navegação entre telas.
 * @author Alex
 */
class Login : AppCompatActivity() {
    // ViewBinding
    private val binding by lazy { LoginBinding.inflate(layoutInflater) }
    // FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Metodo chamado quando a Activity é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)// Configura a view da Activity

        // Configura o clique do botão de login
        with(binding) {
            btnLogin.setOnClickListener {
                // Realiza a autenticação com os dados inseridos
                userAuthentication(idEmail.text.toString(), idSenha.text.toString())
            }
            remember.setOnClickListener {
                // Caso o usuário clique em "Esqueci a senha", navega para a tela de reset de senha
                startActivity(Intent(this@Login, ResetPassword::class.java))
            }
            signUp.setOnClickListener {
                // Caso o usuário clique em "Cadastrar", navega para a tela de cadastro
                startActivity(Intent(this@Login, SignUp::class.java))
                finish()
            }
        }
        // Configura o cursor do campo de email para iniciar no começo do texto
        setCursor(binding.idEmail)

        // Define inicialmente o campo de senha como oculto
        binding.idSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Configura o botão de alternar visibilidade da senha
        setupPasswordToggle(
            binding.togglePasswordVisibility,
            binding.idSenha,
            InputType.TYPE_CLASS_TEXT,
            binding.monster,
            binding.hiddenPassword
        )

    }
    // Metodo para garantir que o cursor vá para o início do input quando perde o foco
    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    // Metodo para exibir mensagens de toast na tela
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Função que altera a visibilidade da senha ao alternar o toggle
     * @authors: Ana Carolina.
     */
    private fun setupPasswordToggle(
        toggleButton: ToggleButton,
        editText: EditText,
        inputType: Int,
        monsterImageView: AppCompatImageView,
        hiddenPasswordImageView: AppCompatImageView
        ) {
        // Inicializa o botão como "não mostrar senha"
        toggleButton.isChecked = false

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se o botão está marcado, mostrar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                // Ocultar a imagem do monstro
                monsterImageView.visibility = View.GONE
                // Mostrar a imagem de senha oculta
                hiddenPasswordImageView.visibility = View.VISIBLE
            } else {
                // Se o botão não está marcado, ocultar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
                // Mostrar a imagem do monstro
                monsterImageView.visibility = View.VISIBLE
                // Ocultar a imagem de senha oculta
                hiddenPasswordImageView.visibility = View.GONE
            }
            // Para atualizar a exibição do EditText
            editText.text?.let { editText.setSelection(it.length) }
        }
    }

    // Função que realiza a autenticação do usuário com email e senha
    private fun userAuthentication(email: String, password: String) {
        var isValid = true
        // Validações antes de tentar o login
        if (email.isBlank()) {
            updateInputState(binding.idEmail, binding.textErrorEmail, "Campo obrigatório!", true)
            isValid = false
        } else {
            updateInputState(binding.idEmail, binding.textErrorEmail, "", false)
        }
        if (password.isBlank()) {
            updateInputState(binding.idSenha, binding.textErrorSenha, "Campo obrigatório!", true)
            isValid = false
        } else {
            updateInputState(binding.idSenha, binding.textErrorSenha, "", false)
        }
        if (!isValid) {
            return // Interrompe o login caso algum campo seja inválido
        }

        // Realiza a autenticação com o Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null && user.isEmailVerified) {
                    // Se E-mail verificado, continuar com o login
                    val userId = user.uid
                    checkMonsterNameAndNavigate(userId)
                } else {
                    // E-mail não verificado
                    showToast("Verifique seu e-mail antes de fazer login.")
                }
            }
            .addOnFailureListener { exception ->
                //  Caso a autenticação falhe, exibe uma mensagem
                if (exception.message.toString() == "The email address is badly formatted.") {
                    updateInputState(binding.idEmail, binding.textErrorEmail, "Endereço de email inválido!", true)
                } else {
                    showToast("Email ou senha incorretos, por favor digite novamente!")
                }
            }
    }

    /** Função para atualizar o estado do input (campo de texto e mensagem de erro)
        @author Laís
     */
    private fun updateInputState(
        editText: EditText,
        textView: TextView,
        text: String,
        status: Boolean
    ) {
        val errorIcon: Drawable?
        if (status) {
            editText.setBackgroundResource(R.drawable.shape_input_invalid)
            textView.text = text
            errorIcon = ContextCompat.getDrawable(this, R.drawable.icon_error)
        } else {
            editText.setBackgroundResource(R.drawable.shape_inputs)
            textView.text = ""
            errorIcon = null
        }
        // Obtém o ícone original do início
        val originalStartIcon = editText.compoundDrawablesRelative[0]
        // Adiciona o ícone de erro no final, mantendo o ícone original no início
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            originalStartIcon,
            null,
            errorIcon,
            null
        )
    }
    // Função que verifica o nome do monstro do usuário no Firestore e navega conforme o caso
    private fun checkMonsterNameAndNavigate(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Pessoas").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists() && document.getString("monsterName") != null) {
                // Se o campo monsterName existir, vai para a tela Home
                startActivity(Intent(this, BottomNav::class.java))
            } else {
                // Se o campo monsterName não existir, vai para a tela NameMonster
                startActivity(Intent(this, NameMonster::class.java))
            }
            finish()
        }.addOnFailureListener { exception ->
            // Tratar possíveis erros na consulta
            showToast("Erro ao verificar o nome do monstro: ${exception.message}")
        }
    }
}
