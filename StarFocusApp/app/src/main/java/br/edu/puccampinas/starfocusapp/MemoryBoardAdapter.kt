package br.edu.puccampinas.starfocusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter para o RecyclerView que exibe cartas do jogo da memória.
 * @author Luiz
 */
class MemoryBoardAdapter(
    // Lista de objetos MemoryCard que representa as cartas no jogo.
    private val cards: List<MemoryCard>,
    // Função que é chamada quando uma carta é clicada, recebe o índice da carta clicada.
    private val cardClickListener: (Int) -> Unit
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    // Classe interna ViewHolder que é responsável por gerenciar cada item (carta) do RecyclerView.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referência para o ImageView que exibirá a imagem da carta.
        val cardImage: ImageView = itemView.findViewById(R.id.imageView)

        // Metodo que configura a carta (imagem) no ViewHolder com base no estado da carta.
        fun bind(position: Int) {
            // Obtenção da carta na posição especificada.
            val card = cards[position]
            // Se a carta estiver virada para cima, exibe a imagem da carta, caso contrário, exibe a parte de trás.
            cardImage.setImageResource(if (card.isFaceUp) card.identifier else R.drawable.card_back)

            cardImage.setOnClickListener {
                cardClickListener(position) // Chama o listener de clique passando a posição da carta.
            }
        }
    }
    // Metodo responsável por criar o ViewHolder. Esse metodo é chamado quando o RecyclerView precisa de um novo ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla o layout da carta (memory_card) no RecyclerView.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memory_card, parent, false)
        return ViewHolder(view)
    }
    // Metodo responsável por vincular os dados ao ViewHolder. Esse metodo é chamado para cada item no RecyclerView.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Chama o metodo de bind do ViewHolder, passando a posição do item.
        holder.bind(position)
    }

    // Metodo responsável por retornar o número total de itens no RecyclerView.
    override fun getItemCount() = cards.size
}
