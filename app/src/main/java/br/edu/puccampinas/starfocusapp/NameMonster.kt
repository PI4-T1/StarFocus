package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import br.edu.puccampinas.starfocusapp.databinding.NameMonsterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

@RequiresApi(Build.VERSION_CODES.O)
class NameMonster : AppCompatActivity() {

    private lateinit var binding: NameMonsterBinding  // Alterado para lateinit
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    data class Monster(
        val name: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NameMonsterBinding.inflate(layoutInflater) // Inicialização do binding aqui
        setContentView(binding.root)

        // Chamando o método de carregamento do gif
        loadMonsterImage(binding.imageViewGif)

        with(binding) {
            // Quando clicado o botão "Salvar" -  é feito a validação de campo nulo e é
            // registrado o nome passado no campo
            btnSave.setOnClickListener {
                validName()
                registerNameMonster()
            }
        }
        setCursor(binding.idNameFriend)
    }

    // Função que mostra o Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Foco no cursor
    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    // Função que valida se o campo "Name" é vazio
    private fun validName() {
        val name = binding.idNameFriend.text.toString()

        if (name.isBlank()) {
            showToast("Por favor, preencha o campo antes de prosseguir!")
            return
        }
    }

    // Função que salva o nome do monstro no FireBase
    private fun registerNameMonster() {
        // Define o idioma para português
        FirebaseAuth.getInstance().setLanguageCode("pt")

        val monsterName = binding.idNameFriend.text.toString()

        // Verificar se o usuário está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val monsterData = mapOf(
                "monsterName" to monsterName,
                "userId" to currentUser.uid
            )
            // Adiciona o nome criado na coleção "Pessoas"
            database.collection("Pessoas").document(currentUser.uid).update(monsterData)
                .addOnSuccessListener {
                    showToast("Nome salvo com sucesso!")
                    // Navegar para outra tela ou finalizar o fluxo de cadastro
                    startActivity(
                        Intent(
                            this,
                            Profile::class.java
                        )
                    ) // Troque "Home::class.java" pela activity desejada
                    finish()
                }
                .addOnFailureListener { exception ->
                    showToast("Erro ao salvar o nome: ${exception.message}")
                }

        } else {
            showToast("Usuário não autenticado.")
        }
    }

    // Função que carrega uma imagem de monstro usando Glide
    private fun loadMonsterImage(imageView: ImageView) {
        Glide.with(this)
            .load(R.drawable.imageviewgif)
            .into(imageView)
    }
}
