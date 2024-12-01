package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistorythreeBinding // Import atualizado para o View Binding
/**
 * Classe responsavel por inicializar a terceira historia de introdução do aplicativo.
 *
 * @author Ana Carolina
 */
class HistoryThree : AppCompatActivity() {

    private lateinit var binding: HistorythreeBinding
    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistorythreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração do clique no botão para avançar a home, idnicando que a historia acabou.
        binding.nexthistory3.setOnClickListener{
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)
        }


    }
}