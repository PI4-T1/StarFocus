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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_game)

        recyclerView = findViewById(R.id.recyclerView)
        successMessage = findViewById(R.id.successMessage)
        closeButton = findViewById(R.id.closeButton)
        startGameButton = findViewById(R.id.startGameButton)
        errorCounterTextView = findViewById(R.id.errorCounterTextView)

        parametro = intent.getIntExtra("parametro", 0)
        updateErrorCounter() // Atualiza o contador no início

        setupGame()

        startGameButton.setOnClickListener {
            revealAllCards()
            startGameButton.visibility = View.GONE
        }

        closeButton.setOnClickListener {
            when (parametro) {
                1 -> startActivity(Intent(this, HistoryProgressTwoWin::class.java))
                2 -> startActivity(Intent(this, HistoryProgressFourWin::class.java))
                3 -> startActivity(Intent(this, HistoryProgressSixWin::class.java))
                else -> finish()
            }
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
        recyclerView.addItemDecoration(SpaceItemDecoration(16))

        successMessage.visibility = View.GONE
        closeButton.visibility = View.GONE
    }

    private fun revealAllCards() {
        for (card in memoryCards) {
            card.isFaceUp = true
        }
        memoryBoardAdapter.notifyDataSetChanged()

        Handler().postDelayed({
            for (card in memoryCards) {
                card.isFaceUp = false
            }
            memoryBoardAdapter.notifyDataSetChanged()
        }, 5000)
    }

    private fun updateGameWithFlip(position: Int) {
        val card = memoryCards[position]

        if (card.isFaceUp || card.isMatched) {
            return
        }

        if (indexOfSingleSelectedCard == null) {
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            if (!checkForMatch(indexOfSingleSelectedCard!!, position)) {
                errorCount++ // Incrementa o contador de erros em caso de erro
                updateErrorCounter()
                if (errorCount >= 5) {
                    redirectToMapFragment()
                    return
                }
            }
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        memoryBoardAdapter.notifyDataSetChanged()

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

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        return if (memoryCards[position1].identifier == memoryCards[position2].identifier) {
            memoryCards[position1].isMatched = true
            memoryCards[position2].isMatched = true
            true
        } else {
            false
        }
    }

    private fun showSuccessMessage() {
        successMessage.visibility = View.VISIBLE
        closeButton.visibility = View.VISIBLE
    }

    private fun updateErrorCounter() {
        errorCounterTextView.text = "Erros: $errorCount"
    }

    private fun redirectToMapFragment() {
        val dialog = GameOverDialogFragment()
        dialog.show(supportFragmentManager, "GameOverDialogFragment")
    }

}
