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


/**
 * Classe que representa o fragment do guarda-roupa no aplicativo. Permite que os usuários
 * naveguem por roupas desbloqueadas, selecionem uma roupa para equipar
 * o personagem e acompanhem quais roupas foram recentemente desbloqueadas.
 *
 * @author Luiz e Ana Carolina
 */
class ClosetFragment : Fragment() {

    private lateinit var clothesCarousel: ViewPager2 //Componente que exibe as roupas em formato de carrossel.
    private lateinit var rightButton: ImageButton //Botão para navegar para a próxima roupa.
    private lateinit var leftButton: ImageButton //Botão para navegar para a roupa anterior.
    private lateinit var selectButton: TextView // Usando TextView para permitir mudança de texto
    private val db = FirebaseFirestore.getInstance()//Instancia DO FireStore
    private val auth = FirebaseAuth.getInstance()//Instancia de autenticação do usuario
    private var unlockedClothes = mutableListOf<Pair<Int, Boolean>>() //ista de roupas desbloqueadas pelo usuário
    private var currentClothesIndex = 0 // Índice da roupa atualmente exibida no carrossel.
    private var equippedClothesIndex = -1 // Índice do personagem atualmente equipado


     //Sobescreve o método para inflar o layout

    override fun onCreateView(
        //Sobrescreve o método para inflar o layout do fragment

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_closet, container, false)
    }

/**
 * Sobescreve o método para inflar o layout, incializando os componentes do layout e configura
 * os ouvintes de botao e recebe o carregamento dos dados do usuario.
 *
 */
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

    /**
     * Obtem dos dados do usuario logado, diretamente do Firestore,
     * popula a lista de roupas desbloqueadas, configura o carrosel com a roupa selecionada
     * e atualiza o badget de roupa, que indica que ha novas roupas desbloqueadas.
     *
     * @author Ana Carolina
     */

    private fun fetchUserData() {
        // Obtém o ID do usuário autenticado. Se não estiver autenticado, a função retorna sem fazer nada.
        val userId = auth.currentUser?.uid ?: return
        // Acessa o documento do usuário na coleção "Pessoas" do Firestore
        db.collection("Pessoas").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                // Limpa a lista de roupas desbloqueadas antes de preenchê-la com os novos dados
                unlockedClothes.clear()
                // Adiciona cada roupa à lista com o valor true se for nova
                if (document.getBoolean("monster1") == true) unlockedClothes.add(Pair(R.drawable.monsterprincipal, false))
                if (document.getBoolean("monster2") == true) unlockedClothes.add(Pair(R.drawable.monster2, true))
                if (document.getBoolean("monster3") == true) unlockedClothes.add(Pair(R.drawable.monster3, true))
                if (document.getBoolean("monster4") == true) unlockedClothes.add(Pair(R.drawable.monster4, true))
                if (document.getBoolean("monster5") == true) unlockedClothes.add(Pair(R.drawable.monster5, true))

                // Configura o adaptador do carrossel para exibir as roupas desbloqueadas
                clothesCarousel.adapter = ClothesCarouselAdapter(unlockedClothes)

                // Verifica se algum dos monstros foi desbloqueado
                val monster2 = document.getBoolean("monster2") == true
                val monster3 = document.getBoolean("monster3") == true
                val monster4 = document.getBoolean("monster4") == true
                val monster5 = document.getBoolean("monster5") == true

                // Atualiza o badge do closet na barra de navegação, indicando quantas roupas foram desbloqueadas
                updateBadgeForCloset(unlockedClothes.size)

                // Obter a roupa atualmente equipada e definir como o índice inicial
                equippedClothesIndex = (document.getLong("roupa")?.toInt() ?: 1) - 1
                currentClothesIndex = equippedClothesIndex
                clothesCarousel.currentItem = equippedClothesIndex // Atualiza o item exibido no carrossel com a roupa atualmente equipada
                updateButtonLabel()
            }
        }
    }

    //Atualiza o badge de notificação no botão de closet da barra de navegação.
    private fun updateBadgeForCloset(unlockedClothesCount: Int) {
        // Aqui você comunica com a Activity para atualizar o badge
        (activity as? BottomNav)?.updateBadgeForCloset(unlockedClothesCount)
    }

    //Atualiza a roupa selecionada do usuário no Firestore.
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

    /**
     * Atualiza o rótulo e o estilo do botão de seleção com base na roupa atualmente selecionada.
     * Remove o selo "Novo" da roupa, se selecionado.
     *
     */
    private fun updateButtonLabel() {
        // Verifica se a roupa atualmente selecionada é a mesma que a roupa atualmente equipada
        if (currentClothesIndex == equippedClothesIndex) {
            selectButton.text = "Selecionado"// Se a roupa foi selecionada, altera o texto do botão para "Selecionado"
            selectButton.setBackgroundResource(R.drawable.button_background_pressed)

            // Remove o selo "Novo" quando a roupa for selecionada
            unlockedClothes[currentClothesIndex] = unlockedClothes[currentClothesIndex].copy(second = false)

            // Notifica o adapter para atualizar a visualização da roupa selecionada
            clothesCarousel.adapter?.notifyItemChanged(currentClothesIndex)
        } else {
            selectButton.text = "Selecionar" // Se a roupa não foi selecionada, altera o texto do botão para "Selecionar"
            selectButton.setBackgroundResource(R.drawable.button_background_normal)
        }
    }
}
