package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistorytwoBinding // Import atualizado para o View Binding
/**
 * Classe responsavel por inicializar a segunda historia de introdução do aplicativo.
 *
 * @author Ana Carolina
 */
class HistoryTwo : AppCompatActivity() {

    private lateinit var binding: HistorytwoBinding

    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistorytwoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Configuração do clique no botão para avançar para a próxima parte da história (HistoryThree).
        binding.nexthistory.setOnClickListener{
            val intent = Intent(this, HistoryThree::class.java)
            startActivity(intent)
        }


    }
}
