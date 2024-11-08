package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.viewpager2.widget.ViewPager2

class ClosetFragment : Fragment() {

    private lateinit var clothesCarousel: ViewPager2
    private lateinit var rightButton: ImageButton
    private lateinit var leftButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar o layout do fragment
        return inflater.inflate(R.layout.fragment_closet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clothesCarousel = view.findViewById(R.id.clothesCarousel)
        rightButton = view.findViewById(R.id.rightbutton)
        leftButton = view.findViewById(R.id.leftbutton)

        // Configurar o adaptador do ViewPager2 com as roupas
        val images = listOf(R.drawable.monsterprincipal, R.drawable.monster2, R.drawable.monster3,R.drawable.monster4
            ,R.drawable.monster5, ) // Substitua com suas imagens
        clothesCarousel.adapter = ClothesCarouselAdapter(images)

        //navegação pelas setas
        rightButton.setOnClickListener {
            val nextItem = (clothesCarousel.currentItem + 1) % images.size
            clothesCarousel.currentItem = nextItem
        }

        leftButton.setOnClickListener {
            val prevItem = if (clothesCarousel.currentItem - 1 < 0) images.size - 1 else clothesCarousel.currentItem - 1
            clothesCarousel.currentItem = prevItem
        }
    }
}
