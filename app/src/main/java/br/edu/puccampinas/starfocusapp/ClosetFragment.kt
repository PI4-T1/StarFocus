package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClosetFragment : Fragment() {

    private lateinit var clothesCarousel: ViewPager2
    private lateinit var rightButton: ImageButton
    private lateinit var leftButton: ImageButton
    private lateinit var selectButton: TextView // Usando TextView para permitir mudança de texto
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var unlockedClothes = mutableListOf<Int>()
    private var currentClothesIndex = 0
    private var equippedClothesIndex = -1 // Índice do personagem atualmente equipado

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_closet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clothesCarousel = view.findViewById(R.id.clothesCarousel)
        rightButton = view.findViewById(R.id.rightbutton)
        leftButton = view.findViewById(R.id.leftbutton)
        selectButton = view.findViewById(R.id.btnselecionar)

        fetchUserData()

        rightButton.setOnClickListener {
            val nextItem = (clothesCarousel.currentItem + 1) % unlockedClothes.size
            clothesCarousel.currentItem = nextItem
            currentClothesIndex = nextItem
            updateButtonLabel()
        }

        leftButton.setOnClickListener {
            val prevItem = if (clothesCarousel.currentItem - 1 < 0) unlockedClothes.size - 1 else clothesCarousel.currentItem - 1
            clothesCarousel.currentItem = prevItem
            currentClothesIndex = prevItem
            updateButtonLabel()
        }

        selectButton.setOnClickListener {
            updateClothesSelection()
        }
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("Pessoas").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                unlockedClothes.clear()
                if (document.getBoolean("monster1") == true) unlockedClothes.add(R.drawable.monsterprincipal)
                if (document.getBoolean("monster2") == true) unlockedClothes.add(R.drawable.monster2)
                if (document.getBoolean("monster3") == true) unlockedClothes.add(R.drawable.monster3)
                if (document.getBoolean("monster4") == true) unlockedClothes.add(R.drawable.monster4)
                if (document.getBoolean("monster5") == true) unlockedClothes.add(R.drawable.monster5)

                clothesCarousel.adapter = ClothesCarouselAdapter(unlockedClothes)

                //Define uma notificação no botão de closet da navBar indicando ao usuário que ele tem roupas desbloqueadas

                updateBadgeForCloset(unlockedClothes.size)

                // Obter a roupa atualmente equipada e definir como o índice inicial
                equippedClothesIndex = (document.getLong("roupa")?.toInt() ?: 1) - 1
                currentClothesIndex = equippedClothesIndex
                clothesCarousel.currentItem = equippedClothesIndex
                updateButtonLabel()
            }
        }
    }

    private fun updateBadgeForCloset(unlockedClothesCount: Int) {
        // Aqui você comunica com a Activity para atualizar o badge
        (activity as? BottomNav)?.updateBadgeForCloset(unlockedClothesCount)
    }

    private fun updateClothesSelection() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("Pessoas").document(userId).update("roupa", currentClothesIndex + 1)
            .addOnSuccessListener {
                equippedClothesIndex = currentClothesIndex
                updateButtonLabel()
            }
            .addOnFailureListener {
                // Exibir um erro caso a atualização falhe
            }
    }

    private fun updateButtonLabel() {
        // Verifica se o personagem atual está equipado e define o texto do botão
        if (currentClothesIndex == equippedClothesIndex) {
            selectButton.text = "Selecionado"
            // Quando estiver equipado, use o selector para definir o fundo
            selectButton.setBackgroundResource(R.drawable.button_background_pressed)
        } else {
            selectButton.text = "Selecionar"
            // Quando não estiver equipado, use o selector para definir o fundo
            selectButton.setBackgroundResource(R.drawable.button_background_normal)
        }
    }

}
