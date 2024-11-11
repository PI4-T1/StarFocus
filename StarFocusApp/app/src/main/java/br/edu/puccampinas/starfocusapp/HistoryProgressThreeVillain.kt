package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressThreeVillainBinding // Import atualizado para o View Binding

class HistoryProgressThreeVillain : AppCompatActivity() {

    private lateinit var binding: HistoryprogressThreeVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryprogressThreeVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory3.setOnClickListener{
            val intent = Intent(this, HistoryProgressFourWin::class.java)
            startActivity(intent)
        }


    }
}