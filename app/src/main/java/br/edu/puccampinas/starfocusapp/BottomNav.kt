package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNav : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_nav)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Carrega o fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }

        // Listener para o item da navegação
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()
                R.id.bottom_closet -> ClosetFragment()
                R.id.map -> MapFragment()
                R.id.profile -> ProfileFragment()
                else -> HomeFragment() // Volta para o fragment home, caso nenhum item corresponda a outro fragment
            }

            // Troca de fragmentos com animação
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.container, fragment)
                .commit()

            true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack() // Volta para o fragmento anterior
        } else {
            super.onBackPressed() // Sai da Activity
        }
    }
}
