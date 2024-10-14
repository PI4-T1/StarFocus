package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import br.edu.puccampinas.starfocusapp.databinding.SignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            arrowBack.setOnClickListener {
                startActivity(Intent(this@SignUp, Login::class.java))
                finish()
            }
            btnCadastrar.setOnClickListener {
                validData()
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
        val user = User(
            email = binding.idEmail.text.toString(),
            nomeCompleto = binding.idNome.text.toString(),
            senha = binding.idSenha.text.toString().replace(" ", ""),
            telefone = binding.idtelefone.text.toString(),
            dataDeNascimento = binding.idDataDeNascimento.text.toString()
        )

        if (user.email.isBlank() || user.nomeCompleto.isBlank() || user.senha.isBlank() ||
            user.telefone.isBlank() || user.dataDeNascimento.isBlank()) {
            showToast("Por favor, preencha todos os campos antes de prosseguir!")
            return
        }

        if (!isPhone(user.telefone)) {
            showToast("Por favor, verifique se o número de telefone está correto!")
            return
        }

        if (!isValidPassword(user.senha)) {
            showToast("A senha deve ter pelo menos 6 caracteres!")
            return
        }

        if (!isValidAge(user.dataDeNascimento)) {
            showToast("O usuário deve ter pelo menos 7 anos e a data não pode ser futura!")
            return
        }

        registerUser(user)
    }

    private fun isPhone(givenPhone: String): Boolean {
        val phone = givenPhone.filter { it.isDigit() }
        return phone.length == 11 && phone.substring(0, 2).toInt() in 11..99
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
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
                    "dataDeNascimento" to user.dataDeNascimento
                )

                // Salvar o usuário no Firestore sem a senha
                database.collection("Pessoas").document(authResult.user?.uid.toString()).set(userData)
                    .addOnSuccessListener {
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

}
