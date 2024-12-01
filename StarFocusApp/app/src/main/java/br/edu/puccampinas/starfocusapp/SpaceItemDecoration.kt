package br.edu.puccampinas.starfocusapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A classe SpaceItemDecoration é uma implementação personalizada do RecyclerView.ItemDecoration,
 * usada para adicionar espaçamento entre os itens de uma lista no RecyclerView.
 *
 * @param spacing Define o valor de espaçamento que será aplicado entre os itens do RecyclerView.
 @author Luiz
 */

class SpaceItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    /**
     * Este metodo é chamado para definir os offsets de cada item no RecyclerView.
     * Ele define a quantidade de espaço que será aplicada ao redor de cada item da lista.
     *
     * @param outRect Um objeto Rect onde os offsets serão definidos.
     * @param view A view do item que está sendo desenhado.
     * @param parent O RecyclerView que contém os itens.
     * @param state O estado atual do RecyclerView.
     */

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        outRect.top = spacing
    }
}

