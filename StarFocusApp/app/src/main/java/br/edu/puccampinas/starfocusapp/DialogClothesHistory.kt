package br.edu.puccampinas.starfocusapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton


class DialogClothesHistory(private val context: Context) {

    fun showDialog() {
        // Inflar o layout XML
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_unlocked_clothesandhistory, null)

        // Criar o Dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView) // Definir o layout customizado
            .setCancelable(true) // Permitir que o diálogo seja fechado ao clicar fora
            .create()

        // Referências dos elementos no layout
        val closeButton: AppCompatButton = dialogView.findViewById(R.id.fechardialog1)
        val clothesButton: AppCompatButton = dialogView.findViewById(R.id.btnclothes)
        val storyButton: AppCompatButton = dialogView.findViewById(R.id.btnstory)
        val dialogImage: ImageView = dialogView.findViewById(R.id.dialogimage)
        val dialogTitle: TextView = dialogView.findViewById(R.id.dialogTitle)
        val dialogTextBody: TextView = dialogView.findViewById(R.id.dialogtextbody)

        // Definindo ações dos botões
        closeButton.setOnClickListener {
            dialog.dismiss() // Fechar o diálogo
        }

        clothesButton.setOnClickListener {

             val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
             fragmentTransaction.replace(R.id.btnclothes, ClosetFragment())
             fragmentTransaction.addToBackStack(null)
             fragmentTransaction.commit()

            dialog.dismiss() // Fechar o diálogo após abrir a tela de roupas
        }

        storyButton.setOnClickListener {

            val fragmentTransaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.btnstory, MapFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            dialog.dismiss() // Fechar o diálogo após abrir a tela de roupas
        }

        // Exibir o diálogo
        dialog.show()
    }
}
