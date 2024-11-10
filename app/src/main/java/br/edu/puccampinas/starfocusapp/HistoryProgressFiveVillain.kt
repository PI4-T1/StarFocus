package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryrogressFiveVillainBinding // Import atualizado para o View Binding

class HistoryProgressFiveVillain : AppCompatActivity() {

    private lateinit var binding: HistoryrogressFiveVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryrogressFiveVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory5.setOnClickListener{
            val intent = Intent(this, HistoryProgressSixWin::class.java)
            startActivity(intent)
        }


    }
}