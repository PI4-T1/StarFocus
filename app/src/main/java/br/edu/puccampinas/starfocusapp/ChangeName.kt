package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeName : AppCompatActivity() {

    private lateinit var editTextNewName: EditText
    private lateinit var btnUpdateName: Button
    private lateinit var backProfile: AppCompatImageButton
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_name) // Certifique-se de que o layout está correto

        editTextNewName = findViewById(R.id.editTextNewName)
        btnUpdateName = findViewById(R.id.btnUpdateName)
        backProfile = findViewById(R.id.backProfile)

        btnUpdateName.setOnClickListener {
            updateName()
        }

        // Configurando o listener para o botão de voltar
        backProfile.setOnClickListener {
            val profileFragment = ProfileFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.profilefragmentid, profileFragment) // Certifique-se de que 'fragment_container' é o ID do seu layout que contém os fragmentos
                .addToBackStack(null) // Adiciona à pilha de volta, se desejar
                .commit() // Executa a transação
            }
    }

    private fun updateName() {
        val newName = editTextNewName.text.toString().trim()

        if (newName.isNotEmpty()) {
            val userId = auth.currentUser?.uid // Obtém o ID do usuário logado

            if (userId != null) {
                // Atualiza o campo 'username' no Firestore
                db.collection("Pessoas").document(userId)
                    .update("monsterName", newName)
                    .addOnSuccessListener {
                        Log.d("ChangeName", "Nome atualizado com sucesso")
                        // Aqui você pode redirecionar para outra atividade, se desejar
                        finish() // Fecha a atividade atual
                    }
                    .addOnFailureListener { exception ->
                        Log.w("ChangeName", "Erro ao atualizar nome", exception)
                    }
            } else {
                Log.d("ChangeName", "Usuário não encontrado")
            }
        } else {
            editTextNewName.error = "O nome não pode ser vazio" // Adiciona um erro se o campo estiver vazio
        }
    }
}
