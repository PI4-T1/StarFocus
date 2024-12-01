package br.edu.puccampinas.starfocusapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.Button

 /** Classe que representa o DialogFragment que será exibido se a conexão com o servidor
 * não for bem sucedida.
 * @author Luis e Lais
 */

class DialogNoServerFragment : DialogFragment() {

    // Metodo que infla o layout utilizando o arquivo XML definido para o dialog.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_server_unavailable, container, false)

        val closeButton = view.findViewById<Button>(R.id.btnstory2)
        val closeButton2 = view.findViewById<Button>(R.id.fechardialog2)

        closeButton2.setOnClickListener {
            //Fecha o dialog
            dismiss()
        }

        closeButton.setOnClickListener {
            //Fecha o dialog
            dismiss()
        }


        return view
    }
}



