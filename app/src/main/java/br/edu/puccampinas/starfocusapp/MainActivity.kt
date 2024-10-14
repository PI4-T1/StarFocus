package br.edu.puccampinas.starfocusapp



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Splash)

        setContentView(R.layout.main_activity) // Define o layout da atividade

    }
}