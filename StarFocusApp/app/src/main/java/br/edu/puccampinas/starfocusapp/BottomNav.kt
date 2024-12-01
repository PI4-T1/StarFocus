package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.edu.puccampinas.starfocusapp.databinding.BottomNavBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Classe responsável pela atividade principal que gerencia a navegação por meio de um BottomNavigationView.
 * Contém a lógica de inicialização e transição entre fragmentos, além de gerenciar funcionalidades adicionais,
 * como badges para itens específicos do menu.
 *
 * @author Ana Carolina
 */
class BottomNav : AppCompatActivity() {

    // Referência para o binding da interface de usuário associada a esta atividade
    private lateinit var binding: BottomNavBinding  // Referência para o binding da activity

    /**
     * Inicializa a atividade e configura a navegação entre os fragmentos.
     * Abre fragmentos específicos, dependendo dos parâmetros fornecidos na Intent.
     *
     * @param savedInstanceState Estado salvo da atividade (opcional).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout da atividade usando View Binding
        binding = BottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica se a Intent contém o extra para abrir o MapFragment diretamente
        if (intent.getBooleanExtra("open_map_fragment", false)) {
            // Abre diretamente o MapFragment se o extra for true
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapFragment()) // Substitui pelo MapFragment diretamente
                    .commit()
                    // Define o item do menu como selecionado
                    binding.bottomNavigation.selectedItemId = R.id.map
            }
        } else if (intent.getBooleanExtra("open_profile_fragment", false)){
            // Caso "open_profile_fragment" seja true, carrega o ProfileFragment
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProfileFragment())
                    .commit()
                // Define o item do menu como selecionado
                binding.bottomNavigation.selectedItemId = R.id.profile
            }
        } else {
            // Caso contrário, carrega o fragmento padrão (HomeFragment, por exemplo)
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HomeFragment()) // Substitui pelo HomeFragment inicialmente
                    .commit()
            }
        }

        // Configura a navegação do BottomNavigationView
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            // Define qual fragmento será exibido com base no item selecionado
            val fragment: Fragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()
                R.id.bottom_closet -> ClosetFragment()
                R.id.map -> MapFragment()
                R.id.profile -> ProfileFragment()
                else -> HomeFragment()
            }

            // Substitui o fragmento exibido no container com animações de transição
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left
                )
                .replace(R.id.container, fragment)
                .commit()

            true // Retorna true para indicar que o item foi tratado
        }

        // Configura o clique do FloatingActionButton (FAB) para exibir o BottomSheet
        binding.Fab.setOnClickListener {
            // Instancia o BottomSheet para adicionar uma nova tarefa
            val bottomSheetFragment = BottomsSheetAddTaskFragment2 { selectedDate ->
                // Obtém o fragmento atual (HomeFragment) e carrega as tarefas do dia selecionado
                val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                homeFragment?.loadTasksForSelectedDay(selectedDate)
            }

            // Exibe o BottomSheet como um diálogo
            bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment2")
        }
    }

    /**
     * Atualiza o badge associado ao item Closet no BottomNavigationView.
     * Exibe a quantidade de roupas desbloqueadas, se houver.
     *
     * @param unlockedClothesCount Número de roupas desbloqueadas. Exibe o badge apenas se o valor for maior que zero.
     */
    fun updateBadgeForCloset(unlockedClothesCount: Int) {
        // Obtém o componente BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // Obtém o item do menu correspondente ao Closet
        val closetItem = bottomNav.menu.findItem(R.id.bottom_closet)

        // Exibe o badge somente se houver roupas desbloqueadas
        if (unlockedClothesCount > 0) {
            // Cria e configura o badge para o item Closet
            val badge = bottomNav.getOrCreateBadge(R.id.bottom_closet)
            badge.isVisible = true // Torna o badge visível
            badge.number = unlockedClothesCount // Define o número exibido no badge

            // Personaliza a cor de fundo e o texto do badge
            badge.backgroundColor = ContextCompat.getColor(this, R.color.teal_dark)

        } else {
            // Remove o badge se não houver roupas desbloqueadas
            bottomNav.removeBadge(R.id.bottom_closet)
        }
    }


    /**
     * Lógica para o botão de voltar.
     * Sobrescreve o comportamento do botão de voltar.
     * Remove o fragmento atual da pilha, se houver, ou utiliza o comportamento padrão.
     *
     * @author Lais
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Verifica se há fragmentos na pilha de retrocesso
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Se houver, remove o fragmento atual da pilha
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()  // Caso contrário, executa o comportamento padrão do botão de voltar
        }
    }
}
