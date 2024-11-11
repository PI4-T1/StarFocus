package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressOneVillainBinding // Import atualizado para o View Binding

class HistoryProgressOneVillain : AppCompatActivity() {

    private lateinit var binding: HistoryprogressOneVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding
        binding = HistoryprogressOneVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nexthistory.setOnClickListener{
            val intent = Intent(this, HistoryProgressTwoWin::class.java)
            startActivity(intent)
        }


    }
}
