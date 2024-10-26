package com.example.teste.data.classesAuxiliares

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teste.R
import com.example.teste.data.classesDoBanco.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterRegistros(
    val registersIds: List<String>?,
    val registerDates: List<String>?,
    val email: String,
    val itemClickListener: OnItemClickListener,
    val mUserViewModel: UserViewModel
): RecyclerView.Adapter<AdapterRegistros.RegistrosViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(registerId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistrosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false)
        return RegistrosViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RegistrosViewHolder, position: Int) {
        if (!registersIds.isNullOrEmpty() && !registerDates.isNullOrEmpty()) {
            val registerId = registersIds[position]
            val registerDate = registerDates[position]

            CoroutineScope(Dispatchers.Main).launch {
                holder.textViewNomeRegistro.text = if (registerId.contains("Pesagem") && !registerId.contains("desmame")) {
                    "Pesagem"
                } else {
                    registerId
                }
                holder.textViewDates.text = registerDate

                if (registerId.contains("vacina") || registerId.contains("Vacina")) {
                    holder.imagemRegistro.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.registro_vacina_foreground)
                    )
                }
                else if (registerId.contains("pesagem") || registerId.contains("Pesagem")) {
                    holder.imagemRegistro.setImageDrawable(
                        ContextCompat.getDrawable(holder.itemView.context, R.mipmap.registro_peso)
                    )
                }
            }

            holder.itemView.setOnClickListener {
                itemClickListener.onItemClick(registerId)
            }
        }
    }

    override fun getItemCount() = registersIds?.size ?: 0

    class RegistrosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNomeRegistro: TextView = itemView.findViewById(R.id.textViewNomeDoRegistro)
        val textViewDates: TextView = itemView.findViewById(R.id.textViewDataRegistro)
        val imagemRegistro: ImageView = itemView.findViewById(R.id.imageViewRegistro)
    }
}