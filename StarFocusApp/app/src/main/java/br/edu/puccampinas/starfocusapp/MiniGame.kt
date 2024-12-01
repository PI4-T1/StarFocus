package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
/**
 * Classe que define o layout e a funcionalidade do jogo
@author Luiz
 */
class MiniGame : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var memoryBoardAdapter: MemoryBoardAdapter
    private lateinit var memoryCards: List<MemoryCard>
    private var indexOfSingleSelectedCard: Int? = null
    private lateinit var successMessage: TextView
    private lateinit var closeButton: Button
    private lateinit var startGameButton: Button
    private lateinit var errorCounterTextView: TextView // Exibe os erros

    private var parametro: Int = 0
    private var errorCount = 0 // Contador de erros

    // Função onCreate, chamada ao iniciar a atividade
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_game)

// Inicializa as variáveis de UI com os elementos da tela
        recyclerView = findViewById(R.id.recyclerView)
        successMessage = findViewById(R.id.successMessage)
        closeButton = findViewById(R.id.closeButton)
        startGameButton = findViewById(R.id.startGameButton)
        errorCounterTextView = findViewById(R.id.errorCounterTextView)

        // Obtém o valor do parâmetro da intent
        parametro = intent.getIntExtra("parametro", 0)
        updateErrorCounter() // Atualiza o contador no início

        // Configura o jogo
        setupGame()

        // Configura o botão de início do jogo
        startGameButton.setOnClickListener {
            revealAllCards()// Revela todas as cartas
            startGameButton.visibility = View.GONE // Esconde o botão "Iniciar"
        }

        // Configura o botão de fechar, com diferentes ações baseadas no valor de "parametro"
        closeButton.setOnClickListener {
            when (parametro) {
                1 -> startActivity(Intent(this, HistoryProgressTwoWin::class.java))
                2 -> startActivity(Intent(this, HistoryProgressFourWin::class.java))
                3 -> startActivity(Intent(this, HistoryProgressSixWin::class.java))
                else -> finish()
            }
        }
    }

    // Função que configura o jogo (cartas e adaptador)
    private fun setupGame() {
        // Lista de imagens que serão usadas nas cartas
        val images = mutableListOf(
            R.drawable.image1, R.drawable.image2, R.drawable.image3,
            R.drawable.image5, R.drawable.image6, R.drawable.image7,
            R.drawable.image8, R.drawable.image9, R.drawable.image10
        )
        // Duplica a lista de imagens para criar pares
        images.addAll(images)
        images.shuffle() // Embaralha as imagens

        // Cria uma lista de cartas (MemoryCard) com as imagens
        memoryCards = images.map { MemoryCard(it) }

        // Cria o adaptador do RecyclerView e configura a lógica de seleção de cartas
        memoryBoardAdapter = MemoryBoardAdapter(memoryCards) { position ->
            updateGameWithFlip(position) // Atualiza o jogo quando uma carta é virada
        }

        // Configura o RecyclerView
        recyclerView.adapter = memoryBoardAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 3)// Grid de 3 colunas
        recyclerView.addItemDecoration(SpaceItemDecoration(16))// Adiciona espaçamento entre itens

        // Esconde a mensagem de sucesso e o botão de fechar inicialmente
        successMessage.visibility = View.GONE
        closeButton.visibility = View.GONE
    }

    // Função que revela todas as cartas por 5 segundos
    private fun revealAllCards() {
        for (card in memoryCards) {
            card.isFaceUp = true // Revela a carta
        }
        memoryBoardAdapter.notifyDataSetChanged() // Atualiza a UI

        //  Após 5 segundos, vira as cartas novamente
        Handler().postDelayed({
            for (card in memoryCards) {
                card.isFaceUp = false // Vira as cartas de volta
            }
            memoryBoardAdapter.notifyDataSetChanged()
        }, 5000) // Atualiza a UI
    }

    // Função que é chamada ao virar uma carta
    private fun updateGameWithFlip(position: Int) {
        val card = memoryCards[position]

        // Se a carta já estiver virada ou combinada, não faz nada
        if (card.isFaceUp || card.isMatched) {
            return
        }
        // Se nenhuma carta foi selecionada, seleciona a carta
        if (indexOfSingleSelectedCard == null) {
            restoreCards()// Restaura as cartas viradas
            indexOfSingleSelectedCard = position
        } else {
            // Se duas cartas foram selecionadas, verifica se são uma combinação
            if (!checkForMatch(indexOfSingleSelectedCard!!, position)) {
                errorCount++ // Incrementa o contador de erros em caso de erro
                updateErrorCounter() // Atualiza o contador de erros na UI
                if (errorCount >= 12) {
                    redirectToMapFragment()// Se o número de erros for 12, redireciona para o mapa
                    return
                }
            }
            indexOfSingleSelectedCard = null// Reseta a seleção
        }

        // Vira a carta atual
        card.isFaceUp = !card.isFaceUp
        memoryBoardAdapter.notifyDataSetChanged()

        // Se todas as cartas forem combinadas, mostra a mensagem de sucesso
        if (memoryCards.all { it.isMatched }) {
            showSuccessMessage()
        }
    }

    // Função que restaura as cartas que não foram combinadas (virando-as de volta)
    private fun restoreCards() {
        for (card in memoryCards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    // Função que verifica se duas cartas são uma combinação
    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        return if (memoryCards[position1].identifier == memoryCards[position2].identifier) {
            memoryCards[position1].isMatched = true
            memoryCards[position2].isMatched = true
            true
        } else {
            false
        }
    }

    // Função que mostra a mensagem de sucesso e o botão de fechar
    private fun showSuccessMessage() {
        successMessage.visibility = View.VISIBLE
        closeButton.visibility = View.VISIBLE
    }

    // Função que atualiza o contador de erros na UI
    private fun updateErrorCounter() {
        errorCounterTextView.text = "Erros: $errorCount/12"
    }

    // Função que redireciona para o fragmento de jogo finalizado
    private fun redirectToMapFragment() {
        val dialog = GameOverDialogFragment() // Cria o fragmento de fim de jogo
        dialog.show(supportFragmentManager, "GameOverDialogFragment") // Exibe o fragmento
    }

}
