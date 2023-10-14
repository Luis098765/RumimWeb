package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teste.databinding.ActivityPrincipalBinding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Principal : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityPrincipalBinding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        val email = auth.currentUser?.email.toString()

        val docRef = db.collection("Usuarios").document(email)

        binding?.btPropriedade?.setOnClickListener {
            val navegarInformacoesPropriedade = Intent(this,InformacoesPropriedade::class.java)
            startActivity(navegarInformacoesPropriedade)
        }
    }
}