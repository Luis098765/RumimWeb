package com.example.teste

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.teste.databinding.ActivityNovoRegistroBinding
import com.example.teste.databinding.ActivityPerfilAnimalBinding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar

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

        val intent = intent
        val documentId = intent.getStringExtra("documentId").toString()

        binding?.btSalvar?.setOnClickListener {
            criarRegistro()

            val navegarTelaAnimal = Intent(this, PerfilAnimal::class.java)
            navegarTelaAnimal.putExtra("documentId", documentId)
            startActivity(navegarTelaAnimal)
        }
    }

    private fun preencherCampos () {
        val user = auth.currentUser
        val email = user?.email.toString()
        lateinit var nomePropriedade: String
        val intent = intent
        val documentId = intent.getStringExtra("documentId").toString()
        var pesoDesmame: String? = null

        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).addSnapshotListener { documento, error ->
                    if (documento?.exists() == true) {
                        val imageUrl: String = documento.data?.get("Url da imagem do animal").toString()
                        if (!imageUrl.isNullOrBlank()) {
                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                            val localFile = File.createTempFile("localFile", ".png")

                            storageRef.getFile(localFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding?.imageViewAnimal?.setImageBitmap(bitmap)
                            }
                        }

                        binding?.textViewSexoData?.text = "${documento.getString("Sexo")} - ${documento.getString("Data de nascimento")}"
                        binding?.textViewNumero?.text = documentId
                        pesoDesmame = documento.getString("Peso ao desmame")

                        preencherSpinners(pesoDesmame)
                    }
                }
            }
        }
    }

    private fun preencherSpinners (pesoDesmame: String?) {
        val spinnerTipoRegistro = findViewById<Spinner>(R.id.spinnerTipoRegistro)
        spinnerTipoRegistro.prompt = ""

        val opcoesSpinnerComDesmame = arrayOf("Pesagem ao desmame", "Pesagem", "Vacina", "Alterar status", "Observação")
        val opcoesSpinnerSemDesmame = arrayOf("Pesagem", "Vacina", "Alterar status", "Observação")

        val opcoesSpinnerTipoRegistro = if (pesoDesmame != null) {
            opcoesSpinnerSemDesmame
        } else {
            opcoesSpinnerComDesmame
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesSpinnerTipoRegistro)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoRegistro.adapter = spinnerAdapter

        val opcaoSpinner = binding?.spinnerTipoRegistro?.selectedItem.toString()

        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        spinnerStatus.prompt = ""

        val opcoesSpinnerStatus = arrayOf("Ativo", "Inativo", "Vendido", "Abatido", "Morto")

        if (opcaoSpinner == "Alterar status") {
            val spinnerAdapterStatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesSpinnerStatus)
            spinnerAdapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = spinnerAdapterStatus
        }
    }

    private fun criarRegistro () {
        val user = auth.currentUser
        val email = user?.email.toString()
        lateinit var nomePropriedade: String
        val intent = intent
        val documentId = intent.getStringExtra("documentId").toString()

        val opcaoSpinner = binding?.spinnerTipoRegistro?.selectedItem.toString()

        var data = binding?.editData?.text.toString()
        val descricao = binding?.editDescricao?.text.toString()
        var valor: String = binding?.editValor?.text.toString()

        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                val docRef = db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId)

                docRef.addSnapshotListener { documento, error ->
                    if (documento?.exists() == true) {
                        if (opcaoSpinner == "Pesagem ao desmame") {
                            if (valor != null && data != null) {
                                val peso = "$valor Kg"

                                val registroPesoDesmame =
                                    if (descricao != null) {
                                        hashMapOf(
                                            "Data do desmame" to data,
                                            "Peso ao desmame" to peso,
                                            "Descrição" to descricao
                                        )
                                    } else {
                                        hashMapOf(
                                            "Data do desmame" to data,
                                            "Peso ao desmame" to peso
                                        )
                                    }

                                docRef.update("Peso ao desmame", peso, "Data do desmame", data)

                                val nomeRegistro: String = "Pesagem ao desmame - ${data.replace("/", "-")}"
                                docRef.collection("Registros").document(nomeRegistro).set(registroPesoDesmame)
                            } else {
                                Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Valor, no mínimo", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if (opcaoSpinner == "Pesagem") {
                            if (data != null && valor != null) {
                                val peso = "$valor Kg"

                                docRef.update("Peso atual", peso)

                                val registroPeso =
                                    if (descricao != null) {
                                        hashMapOf(
                                            "Data da pesagem" to data,
                                            "Peso atual" to peso,
                                            "Descrição" to descricao
                                        )
                                    } else {
                                        hashMapOf(
                                            "Data da pesagem" to data,
                                            "Peso atual" to peso
                                        )
                                    }

                                val nomeRegistro: String = "Pesagem - ${data.replace("/", "-")}"
                                docRef.collection("Registros").document(nomeRegistro)
                                    .set(registroPeso)
                            } else {
                                Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Valor, no mínimo", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if (opcaoSpinner == "Vacina") {
                            if (data != null && descricao != null) {
                                val registroVacina = hashMapOf(
                                    "Data da vacina" to data,
                                    "Descrição" to descricao
                                )

                                val nomeRegistro: String = "Vacina - ${data.replace("/", "-")}"
                                docRef.collection("Registros").document(nomeRegistro).set(registroVacina)
                            } else {
                                Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Rótulo, no mínimo", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if (opcaoSpinner == "Alterar status") {

                        }
                    }
                }
            }
        }
    }
}