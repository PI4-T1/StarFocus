package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
    private var unlockedClothes = mutableListOf<Pair<Int, Boolean>>() // Pair de (imagem, é novo?)
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
                // Adiciona cada roupa à lista com o valor true se for nova
                if (document.getBoolean("monster1") == true) unlockedClothes.add(Pair(R.drawable.monsterprincipal, false))
                if (document.getBoolean("monster2") == true) unlockedClothes.add(Pair(R.drawable.monster2, true))
                if (document.getBoolean("monster3") == true) unlockedClothes.add(Pair(R.drawable.monster3, true))
                if (document.getBoolean("monster4") == true) unlockedClothes.add(Pair(R.drawable.monster4, true))
                if (document.getBoolean("monster5") == true) unlockedClothes.add(Pair(R.drawable.monster5, true))

                clothesCarousel.adapter = ClothesCarouselAdapter(unlockedClothes)

                // Verifica se algum dos monstros foi desbloqueado
                val monster2 = document.getBoolean("monster2") == true
                val monster3 = document.getBoolean("monster3") == true
                val monster4 = document.getBoolean("monster4") == true
                val monster5 = document.getBoolean("monster5") == true

                // Se algum monstro estiver desbloqueado, exibe o diálogo
                if (monster2 || monster3 || monster4 || monster5) {
                    showDialog()  // Chama a função para exibir o diálogo
                }

                // Define uma notificação no botão de closet da navBar indicando ao usuário que ele tem roupas desbloqueadas
                updateBadgeForCloset(unlockedClothes.size)



                // Obter a roupa atualmente equipada e definir como o índice inicial
                equippedClothesIndex = (document.getLong("roupa")?.toInt() ?: 1) - 1
                currentClothesIndex = equippedClothesIndex
                clothesCarousel.currentItem = equippedClothesIndex
                updateButtonLabel()
            }
        }
    }

    private fun showDialog() {
        // Cria uma instância de DialogClothesHistory e exibe o diálogo
        val customDialog = DialogClothesHistory(requireContext())
        customDialog.showDialog()
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
        if (currentClothesIndex == equippedClothesIndex) {
            selectButton.text = "Selecionado"
            selectButton.setBackgroundResource(R.drawable.button_background_pressed)

            // Remove o selo "Novo" quando a roupa for selecionada
            unlockedClothes[currentClothesIndex] = unlockedClothes[currentClothesIndex].copy(second = false)

            // Notifica o adapter para atualizar a visualização da roupa selecionada
            clothesCarousel.adapter?.notifyItemChanged(currentClothesIndex)
        } else {
            selectButton.text = "Selecionar"
            selectButton.setBackgroundResource(R.drawable.button_background_normal)
        }
    }
}
