package br.edu.puccampinas.starfocusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ClothesCarouselAdapter(private val clothes: List<Pair<Int, Boolean>>) : RecyclerView.Adapter<ClothesCarouselAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageCarouselItem)
        val newSeloImageView: ImageView = view.findViewById(R.id.newSeloRoupa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clothes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (imageRes, isNew) = clothes[position]
        holder.imageView.setImageResource(imageRes)

        // Exibe o selo "new" apenas se a roupa for nova
        holder.newSeloImageView.visibility = if (isNew) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return clothes.size
    }
}
