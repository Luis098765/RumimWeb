package com.example.teste

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.teste.databinding.ActivityPerfilAnimalBinding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

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

        binding?.btVoltar?.setOnClickListener {
            startActivity(Intent(this, Rebanho::class.java))
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
        val documentId = intent.getStringExtra("documentId").toString().replace(" ", "")
        Log.d("documentId",documentId)
        db.collection("Usuarios").document(email).collection("Propriedades").get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                nomePropriedade = querySnapshot.documents[0].id

                db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).addSnapshotListener {  documento, error ->
                    if (documento?.exists() == true) {
                        db.collection("Usuarios").document(email).collection("Propriedades").document(nomePropriedade).collection("Animais").document(documentId).collection("Registros").orderBy("Data da pesagem", Query.Direction.ASCENDING,).get(Source.SERVER).addOnSuccessListener { registros ->
                            var peso: String? = null

                            if (registros != null && !registros.isEmpty) {
                                for (registro in registros) {
                                    Log.d("Registro '${registro.id}'", "${registro.data}")
                                    if (registro.id.contains("Pesagem")) {
                                        peso = registro.getString("Peso atual")
                                    }
                                }
                            } else {
                                Log.e("Erro", "Lista vazia")
                            }

                            Log.d("registro", "fim")
                            Log.d("variavel peso", peso?:"")

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
                            binding?.textViewPesoAtual?.text = peso
                            if (status != null) { binding?.textViewStatusAnimal?.text = "Status do animal: ${documento.getString("Status do animal")}" }
                            if (pesoDesmame != null) { binding?.textViewPesoDesmame?.text = documento.getString("Peso ao desmame") }
                            if (dataDesmame != null) { binding?.textViewDataDesmame?.text = documento.getString("Data do desmame") }

                            if (imageUrl != "null") {
                                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                val localFile = File.createTempFile("localFile", ".png")

                                storageRef.getFile(localFile).addOnSuccessListener {
                                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                    binding?.imageViewAnimal?.setImageBitmap(bitmap)
                                }
                            } else {
                                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/teste-ruminweb.appspot.com/o/Imagens%2F66682.png?alt=media&token=c8ba32de-ea76-4d63-8caf-03c42971961e")
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

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this, Rebanho::class.java))
    }
}