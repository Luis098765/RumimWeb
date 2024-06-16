package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintDocumentAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Rebanho : AppCompatActivity(), AdapterAnimais.OnItemClickListener {
    private lateinit var binding: ActivityRebanhoBinding
    private val db = FirebaseFirestore.getInstance()
    private var adapter: AdapterAnimais? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    lateinit var email: String
    lateinit var nomePropriedade: String
    lateinit var mUserViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rebanho)
        binding = ActivityRebanhoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        email = intent.getStringExtra("email").toString()
        nomePropriedade = intent.getStringExtra("nomePropriedade").toString()

        CoroutineScope(Dispatchers.IO).launch {
            initRecyclerView()
        }

        binding?.btVoltar?.setOnClickListener {
            startActivity(Intent(this, InformacoesPropriedade::class.java))
        }
    }

    override fun onItemClick(documentId: String) {
        val navegarPerfilAnimal = Intent(this, PerfilAnimal::class.java)
        navegarPerfilAnimal.putExtra("documentId", documentId)
        navegarPerfilAnimal.putExtra("email", email)
        startActivity(navegarPerfilAnimal)
    }

    private suspend fun initRecyclerView(){
        binding?.recyclerViewSelecao?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerViewSelecao?.setHasFixedSize(true)

        val documentIds = mUserViewModel.getUserWithAnimals(email)?.first()?.animals?.map { it.numeroIdentificacao }

        adapter = AdapterAnimais(documentIds!!, email, nomePropriedade, this, mUserViewModel)
        binding.recyclerViewSelecao.adapter = adapter
    }

    override fun onBackPressed() {
        startActivity(Intent(this, InformacoesPropriedade::class.java))
    }
}