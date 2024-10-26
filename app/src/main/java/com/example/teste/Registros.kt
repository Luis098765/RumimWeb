package com.example.teste

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teste.data.classesAuxiliares.AdapterAnimais
import com.example.teste.data.classesAuxiliares.AdapterRegistros
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityNovoRegistroBinding
import com.example.teste.databinding.ActivityRegistrosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Registros : AppCompatActivity(), AdapterRegistros.OnItemClickListener {
    private lateinit var binding: ActivityRegistrosBinding
    lateinit var email: String
    lateinit var mUserViewModel: UserViewModel
    lateinit var documentId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registros)
        binding = ActivityRegistrosBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        email = intent.getStringExtra("email").toString()
        documentId = intent.getStringExtra("documentId").toString()
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            initRecyclerView()
        }
    }

    override fun onItemClick(documentId: String) {
//        val navegarRegistro = Intent(this, Registro::class.java)
//        navegarRegistro.putExtra("documentId", documentId)
//        navegarRegistro.putExtra("email", email)
//        startActivity(navegarRegistro)
    }

    private suspend fun initRecyclerView(){
        binding?.recyclerViewSelecao?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerViewSelecao?.setHasFixedSize(true)

        val registersIds = mUserViewModel.getAnimalWithRegisters(documentId)?.first()?.registers?.map { it.nome }
        val registerDates = mUserViewModel.getAnimalWithRegisters(documentId)?.first()?.registers?.map { it.data }

        val adapter = AdapterRegistros(registersIds, registerDates, email, this, mUserViewModel)
        binding.recyclerViewSelecao.adapter = adapter
    }
}