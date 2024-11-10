package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryrogressSixWinBinding // Import atualizado para o View Binding

class HistoryProgressSixWin : AppCompatActivity() {

    private lateinit var binding: HistoryrogressSixWinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryrogressSixWinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory6.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}