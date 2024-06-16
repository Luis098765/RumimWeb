package com.example.teste

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.teste.data.classesDoBanco.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AdapterAnimais (
    val documentIds: List<String>,
    val email: String,
    val nomePropriedade: String,
    val itemClickListener: OnItemClickListener,
    val mUserViewModel: UserViewModel
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

        CoroutineScope(Dispatchers.Main).launch {
            val imageByteArray = mUserViewModel.getAnimalAndImage(documentId)?.first()?.image?.image
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)

            holder.textViewNumero.text = documentId
            holder.imageViewAnimal.setImageBitmap(bitmap)
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