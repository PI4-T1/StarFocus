package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryoneBinding // Import atualizado para o View Binding

class HistoryOne : AppCompatActivity() {

    private lateinit var binding: HistoryoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurando o botão para pular para outra atividade
        binding.textViewJump.setOnClickListener {
           //Navega para o menu se o usuário pular a história
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)

        }
        binding.nexthistory.setOnClickListener{
            val intent = Intent(this, HistoryTwo::class.java)
            startActivity(intent)
        }


    }
}
