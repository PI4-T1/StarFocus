package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryrogressFiveVillainBinding
/**
 * Classe responsavel por inicializar a historia 5 que contém o vilao
 *
 * @author Ana Carolina
 */
class HistoryProgressFiveVillain : AppCompatActivity() {

    private lateinit var binding: HistoryrogressFiveVillainBinding
    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryrogressFiveVillainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Configuração do clique no botão 'endhistory5', que inicia um novo jogo.
         * @author Luis*/
        binding.endhistory5.setOnClickListener{
            // Configurando o Intent para chamar MiniGame e passando o parâmetro 3
            val intent = Intent(this, MiniGame::class.java)
            intent.putExtra("parametro", 3) // Passa o parâmetro 3 como extra
            startActivity(intent)
        }
    }
}
