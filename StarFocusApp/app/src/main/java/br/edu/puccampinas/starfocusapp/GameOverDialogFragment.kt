package br.edu.puccampinas.starfocusapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.Button

class GameOverDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_game, container, false)

        val closeButton = view.findViewById<Button>(R.id.btnstory)
        val retryButton = view.findViewById<Button>(R.id.fechardialog1)

        retryButton.setOnClickListener {
            // Volta para o jogo (apenas fecha o dialog)
            dismiss()
        }

        closeButton.setOnClickListener {
            // Vai para a Activity BottomNav que contém o MapFragment ao clicar no botão de fechar
            val intent = Intent(requireContext(), BottomNav::class.java)
            intent.putExtra("open_map_fragment", true) // Passa o parâmetro extra
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            dismiss() // Fecha o DialogFragment
        }


        return view
    }
}



