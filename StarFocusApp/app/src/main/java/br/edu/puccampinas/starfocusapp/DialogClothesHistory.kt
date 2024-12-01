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

        // Criar o Dialog usando o AlertDialog.Builder
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView) // Definir o layout customizado
            .setCancelable(true) // Permitir que o diálogo seja fechado ao clicar fora
            .create()

        // Tornar o fundo do Dialog transparente
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Referências dos elementos no layout
        val closeButton: AppCompatButton = dialogView.findViewById(R.id.fechardialog1)

        // Definindo ações do botao se fechar
        closeButton.setOnClickListener {
            dialog.dismiss() // Fechar o diálogo
        }

        // Exibir o dialog
        dialog.show()
    }
}
