package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistorytwoBinding // Import atualizado para o View Binding

class HistoryTwo : AppCompatActivity() {

    private lateinit var binding: HistorytwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistorytwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nexthistory.setOnClickListener{
            val intent = Intent(this, HistoryThree::class.java)
            startActivity(intent)
        }


    }
}
