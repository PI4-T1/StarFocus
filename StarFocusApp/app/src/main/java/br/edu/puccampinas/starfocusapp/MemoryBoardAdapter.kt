package br.edu.puccampinas.starfocusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class MemoryBoardAdapter(
    private val cards: List<MemoryCard>,
    private val cardClickListener: (Int) -> Unit
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardImage: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(position: Int) {
            val card = cards[position]
            cardImage.setImageResource(if (card.isFaceUp) card.identifier else R.drawable.card_back)

            cardImage.setOnClickListener {
                cardClickListener(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memory_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = cards.size
}
