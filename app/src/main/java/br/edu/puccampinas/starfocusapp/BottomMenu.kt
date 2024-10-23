package br.edu.puccampinas.starfocusapp

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import br.edu.puccampinas.starfocusapp.databinding.BottommenuBinding


class BottomMenu : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: BottommenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottommenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.background=null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){

                R.id.bottom_closet -> openFragment(ClosetFragment())
                R.id.map -> openFragment(MapFragment())
                R.id.profile -> openFragment(ProfileFragment())
            }
            true
        }
        fragmentManager = supportFragmentManager


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.bottom_closet -> openFragment(ClosetFragment())
            R.id.map -> openFragment(MapFragment())
            R.id.profile -> openFragment(ProfileFragment())

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("This method overrides a deprecated member", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        // Fecha o drawer se ele estiver aberto
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed() // Mantenha o comportamento padrão
        }
    }

    private fun openFragment(fragment: Fragment){
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container,fragment)
        fragmentTransaction.commit()
    }
}