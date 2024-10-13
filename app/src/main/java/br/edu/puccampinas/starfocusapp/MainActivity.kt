package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.edu.puccampinas.starfocusapp.ui.theme.StarFocusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarFocusAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StarFocusAppTheme {
        Greeting("Android")
    }
}

//Código de tela inicial - Mostra  o logo  do app no fundo roxo -  alterar depois
//package br.edu.puccampinas.starfocusapp
//
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import androidx.appcompat.app.AppCompatActivity
//import com.starfocus.app.R
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main_activity) // Define o layout da atividade
//
//        supportActionBar?.hide()
//        window.statusBarColor= Color.parseColor("#6F69AC")
//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent =   Intent(this,Home::class.java)
//            startActivity(intent)
//            finish()
//        },3000)
//    }
//}