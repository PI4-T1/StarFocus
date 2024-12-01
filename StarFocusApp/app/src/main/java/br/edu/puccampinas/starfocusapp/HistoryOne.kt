package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.starfocusapp.databinding.HistoryoneBinding // Import atualizado para o View Binding

/**
 * Classe responsavel por inicializar a historia de introdução do aplicativo.
 *
 * @author Ana Carolina
 */
class HistoryOne : AppCompatActivity() {

    private lateinit var binding: HistoryoneBinding

    // Infla a activity incializando a viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializando o view binding corretamente
        binding = HistoryoneBinding.inflate(layoutInflater)
        // Define o conteúdo da Activity com o root do binding, que é a view inflada.
        setContentView(binding.root)

        // Configurando o botão para pular para outra atividade
        binding.textViewJump.setOnClickListener {
           //Navega para o menu se o usuário pular a história, o redirecionando para a Home
            val intent = Intent(this, BottomNav::class.java)
            startActivity(intent)

        }
        // Configuração do clique no botão para avançar para a próxima parte da história (HistoryTwo).
        binding.nexthistory.setOnClickListener{
            // Cria um Intent para navegar para a Activity HistoryTwo.
            val intent = Intent(this, HistoryTwo::class.java)
            startActivity(intent)
        }


    }
}
