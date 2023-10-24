package com.example.teste

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teste.databinding.ActivityPerfilAnimalBinding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PerfilAnimal : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityPerfilAnimalBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_animal)
        binding = ActivityPerfilAnimalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        binding?.btNovoRegistro?.setOnClickListener {
            val documentId = intent.getStringExtra("documentId").toString()

            val navegarTelaNovoRegistro = Intent(this, NovoRegistro::class.java)
            navegarTelaNovoRegistro.putExtra("documentId", documentId)
            startActivity(navegarTelaNovoRegistro)
        }
    }

    override fun onResume() {
        super.onResume()
        preencherInformacoesAnimal()
    }

    private fun preencherInformacoesAnimal() {
        val user = auth.currentUser
        val email = user?.email.toString()
        lateinit var nomePropriedade: String
        val intent = intent
        val documentId = intent.getStringExtra("documentId").toString()
        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).addSnapshotListener {  documento, error ->
                    if (documento?.exists() == true) {
                        val pesoAtual = documento.data?.get("Peso atual")
                        val pesoDesmame = documento.data?.get("Peso ao desmame")
                        val dataDesmame = documento.data?.get("Data do desmame")
                        val status = documento.data?.get("Status do animal")
                        val imageUrl: String = documento.data?.get("Url da imagem do animal").toString()

                        binding?.textViewNumero?.text = documento.getString("Número de identificação")
                        binding?.textViewCategoria?.text = documento.getString("Categoria")
                        binding?.textViewRaca?.text = documento.getString("Raça")
                        binding?.textViewSexo?.text = documento.getString("Sexo")
                        binding?.textViewPesoNascimento?.text = documento.getString("Peso ao nascimento")
                        binding?.textViewDataNascimento?.text = documento.getString("Data de nascimento")
                        if (status != null) { binding?.textViewStatusAnimal?.text = "Status do animal: ${documento.getString("Status do animal")}" }
                        if (pesoAtual != null) { binding?.textViewPesoAtual?.text = documento.getString("Peso atual") }
                        if (pesoDesmame != null) { binding?.textViewPesoDesmame?.text = documento.getString("Peso ao desmame") }
                        if (dataDesmame != null) { binding?.textViewDataDesmame?.text = documento.getString("Data do desmame") }

                        if (!imageUrl.isNullOrBlank()) {
                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                            val localFile = File.createTempFile("localFile", ".png")

                            storageRef.getFile(localFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding?.imageViewAnimal?.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }
}