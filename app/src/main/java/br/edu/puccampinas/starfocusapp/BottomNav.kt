package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.edu.puccampinas.starfocusapp.databinding.BottomNavBinding

/**
 * Atividade principal que gerencia a navegação entre os fragments no BottomNavigationView.
 * A classe BottomNav é responsável por configurar a navegação entre os diferentes fragments
 * através de um BottomNavigationView. Também lida com a exibição do BottomSheetDialogFragment para adicionar tarefas.
 * @author Lais
 */
class BottomNav : AppCompatActivity() {

    private lateinit var binding: BottomNavBinding  // Referência para o binding da activity

    /**
     * Método de criação da atividade.
     * @param savedInstanceState Estado salvo da atividade (não utilizado aqui).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomNavBinding.inflate(layoutInflater)  // Infla o layout da atividade
        setContentView(binding.root)  // Define o conteúdo da activity

        // Carrega o fragmento inicial (HomeFragment) se não houver estado salvo
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())  // Substitui o fragmento do contêiner pelo HomeFragment
                .commit()  // Realiza a transação
        }

        // Configura o listener para o BottomNavigationView, responsável por trocar os fragments
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()  // Carrega o fragmento Home
                R.id.bottom_closet -> ClosetFragment()  // Carrega o fragmento Closet
                R.id.map -> MapFragment()  // Carrega o fragmento Map
                R.id.profile -> ProfileFragment()  // Carrega o fragmento Profile
                else -> HomeFragment()  // Caso padrão, carrega o HomeFragment
            }

            // Realiza a transação de troca de fragmento com animações
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(  // Define animações para a transição
                    R.anim.slide_in_right,  // Animação de entrada do fragmento
                    R.anim.slide_out_left  // Animação de saída do fragmento atual
                )
                .replace(R.id.container, fragment)  // Substitui o fragmento exibido
                .commit()  // Realiza a transação

            true  // Indica que o item foi tratado
        }

        // Configura o listener para o clique no FAB (Floating Action Button)
        // O clique exibe um BottomSheetDialogFragment para adicionar tarefas
        binding.Fab.setOnClickListener {
            // Cria o fragmento BottomsSheetAddTaskFragment2 e define a ação a ser executada ao adicionar uma tarefa
            val bottomSheetFragment = BottomsSheetAddTaskFragment2 { selectedDate ->

                // Recupera o fragmento HomeFragment atual e carrega as tarefas para a data selecionada
                val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                homeFragment?.loadTasksForSelectedDay(selectedDate)
            }

            // Exibe o BottomSheetFragment
            bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment2")
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
