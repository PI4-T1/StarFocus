package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressTwoWinBinding // Import atualizado para o View Binding
/**
 * Classe responsavel por inicializar a historia 2 que mostra o
 * monstrinho vencendo o vilao
 *
 * @author Ana Carolina
 */
class HistoryProgressTwoWin : AppCompatActivity() {

    private lateinit var binding: HistoryprogressTwoWinBinding
    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding
        binding = HistoryprogressTwoWinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Configuração do clique no botão para avançar para a home, quando a história acaba.
        binding.endhistory.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}
