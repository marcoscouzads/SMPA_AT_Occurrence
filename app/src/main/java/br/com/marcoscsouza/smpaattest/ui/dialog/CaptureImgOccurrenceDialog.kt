package br.com.marcoscsouza.smpaattest.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import br.com.marcoscsouza.smpaattest.databinding.CapureImgOccurrenceBinding

class CaptureImgOccurrenceDialog (private val context: Context) {

    fun mostrar(function: () -> Unit) {
        CapureImgOccurrenceBinding.inflate(LayoutInflater.from(context)).apply {


            btnCaptureOccurrence.setOnClickListener {

            }

            AlertDialog.Builder(context)
                .setView(root)
                .setPositiveButton("Confirmar"){ _,_ ->

                }
                .setNegativeButton("Cancelar"){ _,_ ->

                }.show()
        }
    }

}