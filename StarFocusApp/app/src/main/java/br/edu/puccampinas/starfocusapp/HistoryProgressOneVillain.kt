package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressOneVillainBinding

class HistoryProgressOneVillain : AppCompatActivity() {

    private lateinit var binding: HistoryprogressOneVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding
        binding = HistoryprogressOneVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nexthistory.setOnClickListener{
            // Configurando o Intent para chamar MiniGame e passando o parâmetro
            val intent = Intent(this, MiniGame::class.java)
            intent.putExtra("parametro", 1)
            startActivity(intent)
        }
    }
}
