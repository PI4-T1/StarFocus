package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryprogressFourWinBinding // Import atualizado para o View Binding
/**
 * Classe responsavel por inicializar a historia 4 que mostra o
 * monstrinho vencendo o vilao
 *
 * @author Ana Carolina
 */
class HistoryProgressFourWin : AppCompatActivity() {

    private lateinit var binding: HistoryprogressFourWinBinding

    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryprogressFourWinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do clique no botão para avançar para a home, quando a história acaba.
        binding.endhistory4.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}