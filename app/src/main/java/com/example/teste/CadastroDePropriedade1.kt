package com.example.teste

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teste.databinding.ActivityCadastroDePropriedade1Binding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.firestore.FirebaseFirestore

class CadastroDePropriedade1 : AppCompatActivity() {
    private var binding: ActivityCadastroDePropriedade1Binding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_de_propriedade1)
        binding = ActivityCadastroDePropriedade1Binding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}