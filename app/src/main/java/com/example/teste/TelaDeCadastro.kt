
package com.example.teste

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import com.example.teste.databinding.ActivityMainBinding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class TelaDeCadastro : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityTelaDeCadastroBinding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_de_cadastro)
        binding = ActivityTelaDeCadastroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.btCriarConta?.setOnClickListener{
            val nome: String = binding?.editNome?.text.toString()
            val sobrenome: String = binding?.editSobrenome?.text.toString()
            val data: String = binding?.editData?.text.toString()
            val email: String = binding?.editEmail?.text.toString()
            val password: String = binding?.editSenha?.text.toString()
            val confirm_senha: String = binding?.editRepitaSenha?.text.toString()
            val ocupacaoID = binding?.radioGroup?.checkedRadioButtonId
            val ocupacao = if (ocupacaoID == R.id.Produtor) { "Produtor" } else { "Técnico" }

            val usuariosMap = hashMapOf(
                "Nome" to nome,
                "Sobrenome" to sobrenome,
                "Data de nascimento" to data,
                "Email" to email,
                "Ocupação" to ocupacao
            )

            if (email.isNotEmpty() && password.isNotEmpty() && confirm_senha.isNotEmpty()) {
                if (password == confirm_senha) {
                    createUser(email, password)
                } else {
                    Toast.makeText(this@TelaDeCadastro, "As senhas devem ser iguais!", Toast.LENGTH_SHORT).show()
                    binding?.editRepitaSenha?.setText("")
                }
            } else {
                Toast.makeText(this@TelaDeCadastro, "Por favor, preencha os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUser(email: String, password: String) {
        val nome: String = binding?.editNome?.text.toString()
        val sobrenome: String = binding?.editSobrenome?.text.toString()
        val data: String = binding?.editData?.text.toString()
        val email: String = binding?.editEmail?.text.toString()
        val password: String = binding?.editSenha?.text.toString()
        val confirm_senha: String = binding?.editRepitaSenha?.text.toString()
        val ocupacaoID = binding?.radioGroup?.checkedRadioButtonId
        val ocupacao = if (ocupacaoID == R.id.Produtor) { "Produtor" } else { "Técnico" }

        val usuariosMap = hashMapOf(
            "Nome" to nome,
            "Sobrenome" to sobrenome,
            "Data de nascimento" to data,
            "Email" to email,
            "Ocupação" to ocupacao
        )

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "CreateUserWithEmailAndPassword:Sucess")
                val user = auth.currentUser
                db.collection("Usuarios").document(email).set(usuariosMap).addOnCompleteListener {
                    Toast.makeText(this@TelaDeCadastro, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w(TAG, "CreateUserWithEmailAndPassword:Failure", task.exception)
                Toast.makeText(baseContext, "Falha na criação da conta", Toast.LENGTH_SHORT).show()
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(this@TelaDeCadastro, "Usuário existente, use outro e-mail!", Toast.LENGTH_SHORT).show()
                }
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