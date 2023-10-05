package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teste.databinding.ActivityCadastroDePropriedade1Binding
import com.example.teste.databinding.ActivityCadastroDePropriedade2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroDePropriedade2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCadastroDePropriedade2Binding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_de_propriedade2)
        binding = ActivityCadastroDePropriedade2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        binding?.btVoltar?.setOnClickListener {
            val voltarTela = Intent (this,CadastroDePropriedade1::class.java)
            startActivity(voltarTela)
        }
    }
}