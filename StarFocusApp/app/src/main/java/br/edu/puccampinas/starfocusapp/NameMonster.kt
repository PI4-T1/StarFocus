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
/**
 * Classe que o usuario define o nome do monstrinho
@author
 */
@RequiresApi(Build.VERSION_CODES.O)
class NameMonster : AppCompatActivity() {

    private lateinit var binding: NameMonsterBinding  // Alterado para lateinit
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    /**
     * Data class Monster - Define um objeto para armazenar o nome do monstrinho.
     * @param name Nome do monstrinho.
     */
    data class Monster(
        val name: String
    )

    /**
     * Metodo chamado quando a atividade é criada.
     * Configura a interface e ações de clique para os botões.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // infla o layout
        binding = NameMonsterBinding.inflate(layoutInflater) // Inicialização do binding aqui
        setContentView(binding.root)

        // Chamando o metodo de carregamento do gif
        loadMonsterImage(binding.imageViewGif)

        with(binding) {
            // Quando clicado o botão "Salvar" -  é feito a validação de campo nulo e é
            // registrado o nome passado no campo
            btnSave.setOnClickListener {
                validName() // Valida se o nome foi preenchido
                registerNameMonster() // Registra o nome do monstrinho no Firestore
            }
        }
        setCursor(binding.idNameFriend) // Configura o foco no campo de texto do nome
    }

    /**
     * Função que exibe uma mensagem de Toast na tela.
     * @param message Mensagem que será exibida no Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

     /**
      * Função que configura o comportamento do cursor no EditText.
      * Quando o EditText perde o foco, o cursor vai para o início do campo.
      */
    private fun setCursor(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    /**
     * Função que valida se o campo "Name" está vazio.
     * Exibe um Toast caso o campo não esteja preenchido.
     */
    private fun validName() {
        val name = binding.idNameFriend.text.toString()

        if (name.isBlank()) {
            showToast("Por favor, preencha o campo antes de prosseguir!")
            return
        }
    }

    /**
     * Função que registra o nome do monstrinho no Firebase Firestore.
     * Atualiza o documento do usuário com o nome do monstrinho.
     */
    private fun registerNameMonster() {
        // Define o idioma para português
        FirebaseAuth.getInstance().setLanguageCode("pt")// Define o idioma para português

        val monsterName = binding.idNameFriend.text.toString()// Pega o nome inserido pelo usuário

        // Verificar se o usuário está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val monsterData = mapOf(
                "monsterName" to monsterName, // Atribui o nome do monstrinho
                "userId" to currentUser.uid // Atribui o ID do usuário
            )
            // Adiciona o nome criado na coleção "Pessoas"
            database.collection("Pessoas").document(currentUser.uid).update(monsterData)
                .addOnSuccessListener {
                    // Se a atualização for bem-sucedida, navega para a tela HistoryOne
                    startActivity(
                        Intent(
                            this,
                            HistoryOne::class.java
                        )
                    )
                    finish()
                }
                .addOnFailureListener { exception ->
                    showToast("Erro ao salvar o nome: ${exception.message}")
                }

        } else {
            showToast("Usuário não autenticado.")
        }
    }

    /**
     * Função que carrega uma imagem GIF usando a biblioteca Glide.
     * A imagem é carregada na ImageView fornecida.
     */
    private fun loadMonsterImage(imageView: ImageView) {
        Glide.with(this)
            .load(R.drawable.imageviewgif)
            .into(imageView)
    }
}
