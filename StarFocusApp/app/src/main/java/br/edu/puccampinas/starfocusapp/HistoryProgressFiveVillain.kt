package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryrogressFiveVillainBinding

class HistoryProgressFiveVillain : AppCompatActivity() {

    private lateinit var binding: HistoryrogressFiveVillainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryrogressFiveVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.endhistory5.setOnClickListener{
            // Configurando o Intent para chamar MiniGame e passando o parâmetro 3
            val intent = Intent(this, MiniGame::class.java)
            intent.putExtra("parametro", 3) // Passa o parâmetro 3 como extra
            startActivity(intent)
        }
    }
}
