package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistorythreeBinding // Import atualizado para o View Binding

class HistoryThree : AppCompatActivity() {

    private lateinit var binding: HistorythreeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistorythreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nexthistory3.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}