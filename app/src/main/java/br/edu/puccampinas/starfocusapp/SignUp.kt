package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.annotation.RequiresApi
import br.edu.puccampinas.starfocusapp.databinding.SignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.content.ContextCompat
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class SignUp : AppCompatActivity() {
    private val binding by lazy { SignUpBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    data class User(
        val email: String,
        val nomeCompleto: String,
        val senha: String,
        val telefone: String,
        val dataDeNascimento: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            btnCadastrar.setOnClickListener {
                validData()
            }
            textViewLoginPrompt.setOnClickListener {
                startActivity(Intent(this@SignUp, Login::class.java))
                finish() // Opcional: finaliza a atividade atual
            }

        }
        setCursor(binding.idEmail)
        setCursor(binding.idNome)
        setupDateMask(binding.idDataDeNascimento)
        setupPhoneMask(binding.idtelefone)
    }

    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    private fun validData() {
        var isValid = true // Variável de controle para saber se os dados estão corretos

        val user = User(
            email = binding.idEmail.text.toString(),
            nomeCompleto = binding.idNome.text.toString(),
            senha = binding.idSenha.text.toString().replace(" ", ""),
            telefone = binding.idtelefone.text.toString(),
            dataDeNascimento = binding.idDataDeNascimento.text.toString()
        )

        // Validação do e-mail
        if (user.email.isBlank() || !isValidEmail(user.email)) {
            updateInputState(binding.idEmail, binding.textErrorEmail, "E-mail inválido!", true)
            isValid = false
        } else {
            updateInputState(binding.idEmail, binding.textErrorEmail, "", false)
        }

        // Validação do nome completo
        if (user.nomeCompleto.isBlank()) {
            updateInputState(binding.idNome, binding.textErrorNome, "Campo obrigatório!", true)
            isValid = false
        } else {
            updateInputState(binding.idNome, binding.textErrorNome, "", false)
        }

        // Validação do telefone
        if (!isPhone(user.telefone)) {
            updateInputState(binding.idtelefone, binding.textErrorTelefone, "Número de telefone inválido!", true)
            isValid = false
        } else {
            updateInputState(binding.idtelefone, binding.textErrorTelefone, "", false)
        }

        // Validação da senha
        if (user.senha.isBlank() || !isValidPassword(user.senha)) {
            updateInputState(binding.idSenha, binding.textErrorSenha, "A senha deve ter pelo menos 6 caracteres!", true)
            isValid = false
        } else {
            updateInputState(binding.idSenha, binding.textErrorSenha, "", false)
        }

        // Validação da data de nascimento
        if (!isValidAge(user.dataDeNascimento)) {
            updateInputState(binding.idDataDeNascimento, binding.textErrorDataNascimento, "Data de nascimento inválida! Idade mínima: 7", true)
            isValid = false
        } else {
            updateInputState(binding.idDataDeNascimento, binding.textErrorDataNascimento, "", false)
        }
        // Se todos os campos forem válidos, prosseguir com o registro
        if (isValid) {
            registerUser(user)
        }
    }

    private fun isPhone(givenPhone: String): Boolean {
        val phone = givenPhone.filter { it.isDigit() }
        return phone.length == 11 && phone.substring(0, 2).toInt() in 11..99
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidAge(dataDeNascimento: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return try {
            val birthDate = LocalDate.parse(dataDeNascimento, formatter)
            val currentDate = LocalDate.now()

            // Verificar se a data de nascimento é no futuro
            if (birthDate.isAfter(currentDate)) {
                return false // Data no futuro é inválida
            }

            // Calcular a idade do usuário
            val age = ChronoUnit.YEARS.between(birthDate, currentDate)

            // O usuário deve ter pelo menos 7 anos
            age >= 7
        } catch (e: Exception) {
            false // Retorna falso se a data estiver em formato inválido
        }
    }

    private fun setupDateMask(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            var oldText = ""

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val newText = s.toString().replace(Regex("[^\\d]"), "")
                if (newText == oldText) return

                isUpdating = true

                val formattedText = when {
                    newText.length > 8 -> newText.substring(0, 2) + "/" + newText.substring(2, 4) + "/" + newText.substring(4, 8)
                    newText.length > 4 -> newText.substring(0, 2) + "/" + newText.substring(2, 4) + "/" + newText.substring(4)
                    newText.length > 2 -> newText.substring(0, 2) + "/" + newText.substring(2)
                    else -> newText
                }

                editText.setText(formattedText)
                editText.setSelection(formattedText.length.coerceAtMost(formattedText.length))

                isUpdating = false
            }
        })
    }

    private fun setupPhoneMask(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            var isUpdating = false // Variável de controle para evitar o loop

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    return
                }

                isUpdating = true // Impedir que o evento seja chamado novamente

                val currentText = s.toString().replace(Regex("[^\\d]"), "")
                val formattedText = when {
                    currentText.length > 10 -> {
                        currentText.substring(0, 2) + " " + currentText.substring(2, 7) + "-" + currentText.substring(7, 11)
                    }
                    currentText.length > 2 -> {
                        currentText.substring(0, 2) + " " + currentText.substring(2)
                    }
                    else -> {
                        currentText
                    }
                }

                // Atualizar o texto sem disparar o listener novamente
                editText.setText(formattedText)
                editText.setSelection(formattedText.length)

                isUpdating = false // Permitir que o evento seja chamado novamente
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun registerUser(user: User) {
        // Define o idioma para português
        FirebaseAuth.getInstance().setLanguageCode("pt")
        auth.createUserWithEmailAndPassword(user.email, user.senha).addOnSuccessListener { authResult ->
            authResult.user?.sendEmailVerification()?.addOnCompleteListener {

                // Criar um novo objeto sem a senha para salvar no Firestore
                val userData = mapOf(
                    "email" to user.email,
                    "nomeCompleto" to user.nomeCompleto,
                    "telefone" to user.telefone,
                    "dataDeNascimento" to user.dataDeNascimento,
                    "monster1" to true,
                    "monster2" to true,
                    "monster3" to true,
                    "monster4" to true,
                    "monster5" to true,
                    "history1" to true,
                    "history2" to true,
                    "history3" to true,
                    "history4" to true
                )

                // Salvar o usuário no Firestore sem a senha
                database.collection("Pessoas").document(authResult.user?.uid.toString()).set(userData)
                    .addOnSuccessListener {
                        showToast("Enviamos um email de verificação para você!")
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        showToast("Erro ao salvar os dados: ${exception.message}")
                    }
            }
        }.addOnFailureListener { exception ->
            when (exception.message) {
                "The email address is badly formatted." -> {
                    showToast("Endereço de email inválido!")
                }
                "The email address is already in use by another account." -> {
                    showToast("Endereço de email já registrado!")
                }
                else -> {
                    showToast("Erro ao criar usuário: ${exception.message}")
                }
            }
        }
    }

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

}
