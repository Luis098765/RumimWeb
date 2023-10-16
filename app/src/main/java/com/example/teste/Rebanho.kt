package com.example.teste

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintDocumentAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Rebanho : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRebanhoBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rebanho)
        binding = ActivityRebanhoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        initRecyclerView()
    }

    private fun initRecyclerView(){
        binding.recyclerViewSelecao?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerViewSelecao?.setHasFixedSize(true)
        binding?.recyclerViewSelecao?.adapter = AdapterAnimais(getList())
    }

    private fun getList() = listOf(
        "Luís Fernando"
    )
}