package com.example.teste.data.classesAuxiliares

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teste.R
import com.example.teste.data.classesDoBanco.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterRegistros(
    val registersIds: List<String>,
    val email: String,
    val numeroDoAnimal: String,
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
        val registerId = registersIds[position]

        CoroutineScope(Dispatchers.Main).launch {
            holder.textViewNome.text = registerId
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(registerId)
        }
    }

    override fun getItemCount() = registersIds.size

    class RegistrosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNome: TextView = itemView.findViewById(R.id.textViewNomeDoRegistro)
    }
}