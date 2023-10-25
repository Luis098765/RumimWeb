package com.example.teste

import android.graphics.BitmapFactory
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
import java.io.File

class AdapterAnimais (
    val documentIds: List<String>,
    val email: String,
    val nomePropriedade: String,
    val storage: FirebaseStorage,
    val itemClickListener: OnItemClickListener,
    val db: FirebaseFirestore
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

        db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).addSnapshotListener { documento, error ->
            if (documento?.exists() == true) {
                val imageUrl: String = documento.data?.get("Url da imagem do animal").toString()
                if (!imageUrl.isNullOrBlank()) {
                    try {
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                        val localFile = File.createTempFile("localFile", ".png")

                        storageRef.getFile(localFile).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            holder.imageViewAnimal.setImageBitmap(bitmap)
                        }
                    } catch (e: IllegalArgumentException) {

                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(documentId)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNumero: TextView = itemView.findViewById(R.id.textViewNumero)
        val imageViewAnimal: ImageView = itemView.findViewById(R.id.imageViewAnimal)
    }
}