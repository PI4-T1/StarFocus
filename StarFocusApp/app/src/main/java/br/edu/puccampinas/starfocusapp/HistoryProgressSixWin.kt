package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryrogressSixWinBinding // Import atualizado para o View Binding
/**
 * Classe responsavel por inicializar a historia 6 que mostra o
 * monstrinho vencendo o vilao
 *
 * @author Ana Carolina
 */
class HistoryProgressSixWin : AppCompatActivity() {

    private lateinit var binding: HistoryrogressSixWinBinding
    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryrogressSixWinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do clique no botão para avançar para a home, quando a história acaba.
        binding.endhistory6.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}