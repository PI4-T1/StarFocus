package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressTwoWinBinding // Import atualizado para o View Binding

class HistoryProgressTwoWin : AppCompatActivity() {

    private lateinit var binding: HistoryprogressTwoWinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding
        binding = HistoryprogressTwoWinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}
