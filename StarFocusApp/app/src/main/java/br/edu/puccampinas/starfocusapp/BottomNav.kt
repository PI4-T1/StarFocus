package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.edu.puccampinas.starfocusapp.databinding.BottomNavBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNav : AppCompatActivity() {

    private lateinit var binding: BottomNavBinding  // Referência para o binding da activity

    /**
     * Método de criação da atividade.
     * @param savedInstanceState Estado salvo da atividade (não utilizado aqui).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica se a Intent contém o extra para abrir o MapFragment diretamente
        if (intent.getBooleanExtra("open_map_fragment", false)) {
            // Abre diretamente o MapFragment se o extra for true
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapFragment()) // Substitui pelo MapFragment diretamente
                    .commit()
                    binding.bottomNavigation.selectedItemId = R.id.map
            }
        } else if (intent.getBooleanExtra("open_profile_fragment", false)){
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProfileFragment())
                    .commit()
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
            val fragment: Fragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()
                R.id.bottom_closet -> ClosetFragment()
                R.id.map -> MapFragment()
                R.id.profile -> ProfileFragment()
                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left
                )
                .replace(R.id.container, fragment)
                .commit()

            true
        }

        binding.Fab.setOnClickListener {
            val bottomSheetFragment = BottomsSheetAddTaskFragment2 { selectedDate ->
                val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                homeFragment?.loadTasksForSelectedDay(selectedDate)
            }

            bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment2")
        }
    }

    /**
     * Atualiza o badge para o item do Closet quando há roupas desbloqueadas.
     */
    // Método para atualizar o badge no BottomNavigationView
    fun updateBadgeForCloset(unlockedClothesCount: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val closetItem = bottomNav.menu.findItem(R.id.bottom_closet)

        // Exibe o badge somente se houver roupas desbloqueadas
        if (unlockedClothesCount > 0) {
            val badge = bottomNav.getOrCreateBadge(R.id.bottom_closet)
            badge.isVisible = true
            badge.number = unlockedClothesCount

            // Personaliza a cor de fundo e o texto do badge
            badge.backgroundColor = ContextCompat.getColor(this, R.color.teal_dark)

        } else {
            bottomNav.removeBadge(R.id.bottom_closet)
        }
    }


    /**
     * Lógica para o botão de voltar.
     * Verifica se existe algum fragmento na pilha de retrocesso.
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
