package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintDocumentAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Rebanho : AppCompatActivity(), AdapterAnimais.OnItemClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRebanhoBinding
    private val db = FirebaseFirestore.getInstance()
    private var adapter: AdapterAnimais? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rebanho)
        binding = ActivityRebanhoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        initRecyclerView()

        binding?.btVoltar?.setOnClickListener {
            startActivity(Intent(this, InformacoesPropriedade::class.java))
        }
    }

    override fun onItemClick(documentId: String) {
        val navegarPerfilAnimal = Intent(this, PerfilAnimal::class.java)
        navegarPerfilAnimal.putExtra("documentId", documentId)
        startActivity(navegarPerfilAnimal)
    }

    private fun initRecyclerView(){
        binding?.recyclerViewSelecao?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerViewSelecao?.setHasFixedSize(true)
        buscarIdsAnimais()
    }

    private fun buscarIdsAnimais() {
        val user = auth.currentUser
        val email = user?.email.toString()
        lateinit var nomePropriedade: String
        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").get().addOnSuccessListener { querySnapshotAnimais ->
                    val documentIds = querySnapshotAnimais.documents.map { it.id }

                    adapter = AdapterAnimais(documentIds, email, nomePropriedade, storage, this, db)
                    binding.recyclerViewSelecao.adapter = adapter
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, InformacoesPropriedade::class.java))
    }
}