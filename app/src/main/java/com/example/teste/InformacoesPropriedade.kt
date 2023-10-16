package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teste.databinding.ActivityCadastroDePropriedade2Binding
import com.example.teste.databinding.ActivityInformacoesPropriedadeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InformacoesPropriedade : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityInformacoesPropriedadeBinding? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacoes_propriedade)
        binding = ActivityInformacoesPropriedadeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email.toString()

        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).addSnapshotListener { documento, error ->
                    if (documento?.exists() == true) {
                        binding?.textViewNome?.text = documento.getString("Nome da propriedade")
                        binding?.textViewLocal?.text = documento.getString("Localização da propriedade")

                        db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").get().addOnSuccessListener { querySnapshot ->
                            val numeroAnimaisAtivos = querySnapshot.size()

                            binding?.textViewQtdAtivos?.text = numeroAnimaisAtivos.toString()
                        }
                    }
                }
            }
        }

        binding?.btAdicionar?.setOnClickListener {
            db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val nomePropriedade = querySnapshot.documents[0].id

                    val navegarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
                    navegarCadastroAnimal1.putExtra("nome propriedade", nomePropriedade)
                    startActivity(navegarCadastroAnimal1)
                }
            }
        }

        binding?.btPesquisar?.setOnClickListener {
            val navegarTelaRebanho = Intent(this, Rebanho::class.java)
            startActivity(navegarTelaRebanho)
        }
    }
}