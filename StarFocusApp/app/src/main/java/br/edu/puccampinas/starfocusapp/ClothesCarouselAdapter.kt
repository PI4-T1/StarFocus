package br.edu.puccampinas.starfocusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView


// Adapter para o carrossel de roupas no guarda-roupa (Closet)
class ClothesCarouselAdapter(private val clothes: List<Pair<Int, Boolean>>) : RecyclerView.Adapter<ClothesCarouselAdapter.ViewHolder>() {


    // ViewHolder é usado para armazenar as referências das views de cada item da lista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // A ImageView que irá exibir a imagem da roupa
        val imageView: ImageView = view.findViewById(R.id.imageCarouselItem)
        // A ImageView que irá exibir o selo "Novo" se a roupa for nova
        val newSeloImageView: ImageView = view.findViewById(R.id.newSeloRoupa)
    }

    // Cria a view de um item da lista (quando o RecyclerView precisa de um novo ViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla o layout do item (cada roupa) para ser exibido no RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clothes, parent, false)
        return ViewHolder(view)
    }
    // Preenche os dados de cada item no RecyclerView com base na posição
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtém a imagem e o estado "novo" (booleano) da roupa na posição atual
        val (imageRes, isNew) = clothes[position]
        // Define a imagem da roupa na ImageView
        holder.imageView.setImageResource(imageRes)

        // Exibe o selo "new" apenas se a roupa for nova
        holder.newSeloImageView.visibility = if (isNew) View.VISIBLE else View.GONE
    }
    // Retorna o número total de itens no RecyclerView
    override fun getItemCount(): Int {
        return clothes.size
    }
}
