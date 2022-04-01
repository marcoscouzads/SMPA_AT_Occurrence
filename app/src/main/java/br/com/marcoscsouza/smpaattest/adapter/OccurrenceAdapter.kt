package br.com.marcoscsouza.smpaattest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.marcoscsouza.smpaattest.databinding.OccurrenceItemBinding
import br.com.marcoscsouza.smpaattest.db.Occurrence

class OccurrenceAdapter(val occurrences: List<Occurrence>
): RecyclerView.Adapter<OccurrenceAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: OccurrenceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        private lateinit var occurrence: Occurrence

//        val titulo = binding.occurrenceItemTitulo
//
//        val texto = binding.occurrenceItemTexto
//
//        val data = binding.occurrenceItemData
//
//        val foto = binding.occurrenceItemFoto




        fun bind(occurrence: Occurrence) {
            this.occurrence = occurrence
            val titulo = binding.occurrenceItemTitulo
            titulo.text = occurrence.titulo
            val texto = binding.occurrenceItemTexto
            texto.text = occurrence.texto
            val data = binding.occurrenceItemData
            data.text = occurrence.data
            val foto = binding.occurrenceItemFoto
            foto.setImageBitmap(occurrence.foto)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = OccurrenceItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val occurrence = occurrences[position]
        holder.bind(occurrence)
//        holder.titulo.text = occurrence.titulo
//        holder.texto.text = occurrence.texto
//        holder.data.text = occurrence.data
//        holder.foto.setImageBitmap(occurrence.foto)
    }

    override fun getItemCount(): Int = occurrences.size

//    fun update(occurrences: List<Occurrence>) {
//        this.occurrences.clear()
//        this.occurrences.addAll(occurrences)
//        notifyDataSetChanged()
//    }

}