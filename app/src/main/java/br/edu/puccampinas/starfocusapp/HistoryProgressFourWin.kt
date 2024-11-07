package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressFourWinBinding // Import atualizado para o View Binding

class HistoryProgressFourWin : AppCompatActivity() {

    private lateinit var binding: HistoryprogressFourWinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryprogressFourWinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory4.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}