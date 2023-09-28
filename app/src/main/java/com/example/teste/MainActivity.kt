package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.teste.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.btentrar?.setOnClickListener {
            val email: String = binding?.edtEmail?.text.toString()
            val password: String = binding?.edtSenha?.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this@MainActivity, "Por favor, preencha os campos!", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.btcadastro?.setOnClickListener {
            val navegarSegundaTela = Intent(this,TelaDeCadastro::class.java)
            startActivity(navegarSegundaTela)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Log.d(TAG, "signInWithEmailAndPassword: Success")
                val user = auth.currentUser
                Toast.makeText(this@MainActivity, "Autenticação concluída", Toast.LENGTH_SHORT).show()
                binding?.edtEmail?.setText("")
                binding?.edtSenha?.setText("")
            } else {
                Log.w(TAG, "signInWithEmailAndPassword: Failure")
                Toast.makeText(baseContext, "Falha na autenticação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private var TAG = "EmailAndPassword"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}