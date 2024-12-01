package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressOneVillainBinding

/**
 * Classe responsavel por inicializar a historia 1 que contém o vilao
 *
 * @author Ana Carolina
 */

class HistoryProgressOneVillain : AppCompatActivity() {

    private lateinit var binding: HistoryprogressOneVillainBinding
    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding
        binding = HistoryprogressOneVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Configuração do clique no botão, que inicia um novo jogo.
         * @author Luis
         */
        binding.nexthistory.setOnClickListener{
            // Configurando o Intent para chamar MiniGame e passando o parâmetro
            val intent = Intent(this, MiniGame::class.java)
            intent.putExtra("parametro", 1)
            startActivity(intent)
        }
    }
}
