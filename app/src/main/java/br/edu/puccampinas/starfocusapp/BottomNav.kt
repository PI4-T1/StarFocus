package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import br.edu.puccampinas.starfocusapp.databinding.BottomNavBinding

class BottomNav : AppCompatActivity() {

    private lateinit var binding: BottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carrega o fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }

        // Listener para o item da navegação
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
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.container, fragment)
                .commit()

            true
        }

        // Listener de clique no FAB para abrir o BottomSheetDialogFragment de adicionar tarefas
        // No BottomNav
        binding.Fab.setOnClickListener {
            val bottomSheetFragment = BottomsSheetAddTaskFragment2 { selectedDate ->
                val formattedDate = selectedDate

                val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                homeFragment?.loadTasksForSelectedDay(formattedDate)
            }
            bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment2")
        }



    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
