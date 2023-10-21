package com.example.teste

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AdapterAnimais (
    val documentIds: List<String>,
    val email: String,
    val nomePropriedade: String,
    val storage: FirebaseStorage,
    val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdapterAnimais.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(documentId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_animais, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = documentIds.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val documentId = documentIds[position]
        holder.textViewNumero.text = documentId

        val imageUrl = "gs://teste-ruminweb.appspot.com/Imagens/$email/Propriedades/$nomePropriedade/Animais/$documentId"
        Glide.with(holder.itemView.context).load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.imageViewAnimal)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(documentId)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNumero: TextView = itemView.findViewById(R.id.textViewNumero)
        val imageViewAnimal: ImageView = itemView.findViewById(R.id.imageViewAnimal)
    }
}