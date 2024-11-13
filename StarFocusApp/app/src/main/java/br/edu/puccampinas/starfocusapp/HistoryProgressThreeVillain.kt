package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressThreeVillainBinding

class HistoryProgressThreeVillain : AppCompatActivity() {

    private lateinit var binding: HistoryprogressThreeVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryprogressThreeVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory3.setOnClickListener{
            // Configurando o Intent para chamar MiniGame e passando o parâmetro 2
            val intent = Intent(this, MiniGame::class.java)
            intent.putExtra("parametro", 2) // Passa o parâmetro 2 como extra
            startActivity(intent)
        }
    }
}
