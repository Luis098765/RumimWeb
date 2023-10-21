package com.example.teste

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.teste.databinding.ActivityNovoRegistroBinding
import com.example.teste.databinding.ActivityPerfilAnimalBinding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NovoRegistro : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityNovoRegistroBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_registro)
        binding = ActivityNovoRegistroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        preencherCampos()
    }

    private fun preencherCampos () {
        val user = auth.currentUser
        val email = user?.email.toString()
        lateinit var nomePropriedade: String
        val intent = intent
        val documentId = intent.getStringExtra("documentId").toString()

        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).addSnapshotListener { documento, error ->
                    if (documento?.exists() == true) {
                        binding?.textViewSexoData?.text = "${documento.getString("Sexo")} - ${documento.getString("Data de nascimento")}"
                        binding?.textViewNumero?.text = documentId
                    }
                }
            }
        }

        val spinner = findViewById<Spinner>(R.id.spinnerTipoRegistro)
        spinner.prompt = ""
        val opcoesSpinner = arrayOf("Pesagem ao desmame", "Pesagem", "Vacina", "Alterar Status", "Observação")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesSpinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
    }


}