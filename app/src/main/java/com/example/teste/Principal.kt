package com.example.teste

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.AnimalViewModel
import com.example.teste.databinding.ActivityPrincipalBinding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.lang.Exception

class Principal : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityPrincipalBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private var nomePropriedade: String? = null
    private var local: String? = null
    private var qtdAtivos: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        val email = auth.currentUser?.email.toString()

        val docRef = db.collection("Usuarios").document(email).collection("Propriedades")

        val mAnimalViewModel = ViewModelProvider(this).get(AnimalViewModel::class.java)

        Log.d("dados no banco", mAnimalViewModel.readAllData.value.toString())

        binding?.btSincronizar?.visibility = if (mAnimalViewModel.readAllData.value.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        docRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val propriedade = querySnapshot.documents[0].id

                docRef.document(propriedade).addSnapshotListener { documento, error ->
                    if (documento?.exists() == true) {
                        nomePropriedade = documento.getString("Nome da propriedade")
                        local = documento.getString("Localização da propriedade")

                        docRef.document(propriedade).collection("Animais").get().addOnSuccessListener { querySnapshot ->
                            val numeroAnimaisAtivos = querySnapshot.size()

                            qtdAtivos = numeroAnimaisAtivos.toString()
                        }
                    }
                }
            }
        }

        binding?.btPropriedade?.setOnClickListener {
            docRef.get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val navegaraCadastroPropriedade1 = Intent(this,CadastroDePropriedade1::class.java)
                    startActivity(navegaraCadastroPropriedade1)
                } else {
                    val navegarInformacoesPropriedade = Intent(this,InformacoesPropriedade::class.java)
                    startActivity(navegarInformacoesPropriedade)
                }
            }.addOnFailureListener {
                when (it) {
                    is IOException -> {
                        val navegarInformacoesPropriedade = Intent(this, InformacoesPropriedade::class.java)
                        navegarInformacoesPropriedade.putExtra("Nome da propriedade", nomePropriedade)
                        navegarInformacoesPropriedade.putExtra("Localização", local)
                        navegarInformacoesPropriedade.putExtra("Quantidade", qtdAtivos)
                        startActivity(navegarInformacoesPropriedade)
                    }
                }
            }
        }
    }
}