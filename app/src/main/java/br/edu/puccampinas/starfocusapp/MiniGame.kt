package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MiniGame : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var memoryBoardAdapter: MemoryBoardAdapter
    private lateinit var memoryCards: List<MemoryCard>
    private var indexOfSingleSelectedCard: Int? = null
    private lateinit var successMessage: TextView
    private lateinit var closeButton: Button
    private lateinit var startGameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_game)

        recyclerView = findViewById(R.id.recyclerView)
        successMessage = findViewById(R.id.successMessage)
        closeButton = findViewById(R.id.closeButton)
        startGameButton = findViewById(R.id.startGameButton)

        setupGame()

        startGameButton.setOnClickListener {
            revealAllCards()
            startGameButton.visibility = View.GONE // Esconde o botão após o início do jogo
        }

        closeButton.setOnClickListener {
            finish() // Fecha a atividade quando o botão é clicado
        }
    }

    private fun setupGame() {
        val images = mutableListOf(
            R.drawable.image1, R.drawable.image2, R.drawable.image3,
            R.drawable.image5, R.drawable.image6, R.drawable.image7,
            R.drawable.image8, R.drawable.image9, R.drawable.image10
        )
        images.addAll(images)
        images.shuffle()

        memoryCards = images.map { MemoryCard(it) }

        memoryBoardAdapter = MemoryBoardAdapter(memoryCards) { position ->
            updateGameWithFlip(position)
        }

        recyclerView.adapter = memoryBoardAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(SpaceItemDecoration(16)) // ajuste conforme necessário

        // Oculta a mensagem de sucesso e o botão no início
        successMessage.visibility = View.GONE
        closeButton.visibility = View.GONE
    }

    private fun revealAllCards() {
        // Vira todas as cartas para cima
        for (card in memoryCards) {
            card.isFaceUp = true
        }
        memoryBoardAdapter.notifyDataSetChanged()

        // Espera 5 segundos e vira todas as cartas para baixo
        Handler().postDelayed({
            for (card in memoryCards) {
                card.isFaceUp = false
            }
            memoryBoardAdapter.notifyDataSetChanged()
        }, 5000)
    }

    private fun updateGameWithFlip(position: Int) {
        val card = memoryCards[position]

        // Ignore se a carta já foi combinada ou está virada para cima
        if (card.isFaceUp || card.isMatched) {
            return
        }

        // 0 ou 2 cartas selecionadas anteriormente
        if (indexOfSingleSelectedCard == null) {
            // Vira a primeira carta
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            // Verifica se as cartas combinam
            checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        memoryBoardAdapter.notifyDataSetChanged()

        // Verifica se todas as cartas foram combinadas
        if (memoryCards.all { it.isMatched }) {
            showSuccessMessage()
        }
    }

    private fun restoreCards() {
        for (card in memoryCards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    private fun checkForMatch(position1: Int, position2: Int) {
        if (memoryCards[position1].identifier == memoryCards[position2].identifier) {
            memoryCards[position1].isMatched = true
            memoryCards[position2].isMatched = true
        }
    }

    private fun showSuccessMessage() {
        successMessage.visibility = View.VISIBLE
        closeButton.visibility = View.VISIBLE
    }
}
