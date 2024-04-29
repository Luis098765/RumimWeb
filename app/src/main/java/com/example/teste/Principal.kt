package com.example.teste

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.Animal
import com.example.teste.data.AnimalViewModel
import com.example.teste.databinding.ActivityPrincipalBinding
import com.example.teste.databinding.ActivityTelaDeCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Principal : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityPrincipalBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private var nomePropriedade: String? = null
    private var local: String? = null
    private var qtdAtivos: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        val email = auth.currentUser?.email.toString()

        val docRef = db.collection("Usuarios").document(email).collection("Propriedades")

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

        val mAnimalViewModel = ViewModelProvider(this)[AnimalViewModel::class.java]

        var dadosNoBanco: List<Animal>? = null

        mAnimalViewModel.readAllData.observe(this, Observer { animal ->
            dadosNoBanco = animal
        })

        dadosNoBanco?.forEach {
            Log.d("Animal", it.toString())
        }

        binding?.btSincronizar?.visibility = if (dadosNoBanco != null) {
            View.GONE
        } else {
            View.VISIBLE
        }

        binding?.btSincronizar?.setOnClickListener {
            if (isNetworkAvailable()) {
                Toast.makeText(this@Principal, "Sincronizando animais, aguarde...", Toast.LENGTH_SHORT).show()

                val animais = dadosNoBanco

                var cont = 0

                CoroutineScope(Dispatchers.Main).launch {
                    animais?.forEach { it ->
                        Log.d("Animal", it.toString())

                        var imageUrl = "null"

                        if (it.image != null) {
                            imageUrl = uploadImage(
                                it.numeroIdentificacao,
                                email,
                                nomePropriedade!!,
                                it.image
                            )
                        }

                        if (imageUrl != "null") {
                            val animalMap = hashMapOf(
                                "Número de identificação" to it.numeroIdentificacao,
                                "Data de nascimento" to it.nascimento,
                                "Raça" to it.raca,
                                "Sexo" to it.sexo,
                                "Categoria" to it.categoria,
                                "Peso ao nascimento" to it.pesoNascimento,
                                "Status do animal" to "Ativo",
                                "Url da imagem do animal" to imageUrl
                            )

                            db.collection("Usuarios").document(email).collection("Propriedades")
                                .document(nomePropriedade!!).collection("Animais")
                                .document(it.numeroIdentificacao).set(animalMap)
                                .addOnSuccessListener {
                                    cont++

                                    if (cont < animais.size) {
                                        Toast.makeText(
                                            this@Principal,
                                            "Animais sincronizados: $cont/${animais.size}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (cont == animais.size) {
                                        Toast.makeText(
                                            this@Principal,
                                            "Sincronização concluída!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            val animalMap = hashMapOf(
                                "Número de identificação" to it.numeroIdentificacao,
                                "Data de nascimento" to it.nascimento,
                                "Raça" to it.raca,
                                "Sexo" to it.sexo,
                                "Categoria" to it.categoria,
                                "Peso ao nascimento" to it.pesoNascimento,
                                "Status do animal" to "Ativo"
                            )

                            db.collection("Usuarios").document(email).collection("Propriedades")
                                .document(nomePropriedade!!).collection("Animais")
                                .document(it.numeroIdentificacao).set(animalMap)
                                .addOnSuccessListener {
                                    cont++

                                    if (cont < animais.size) {
                                        Toast.makeText(
                                            this@Principal,
                                            "Animais sincronizados: $cont/${animais.size}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (cont == animais.size) {
                                        Toast.makeText(
                                            this@Principal,
                                            "Sincronização concluída!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private suspend fun uploadImage(numeroIdentificacao: String, email: String, nomePropriedade: String, image: ByteArray): String {
        return suspendCoroutine { continuation ->
            if (image != null) {
//            Log.d("imageUri - $numeroIdentificacao", imageUri.toString())

                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Imagens").child(email)
                        .child("Propriedades").child(nomePropriedade).child("Animais")
                        .child(numeroIdentificacao)

                Log.d("email", email)
                Log.d("nomePropriedade", nomePropriedade)
                Log.d("numeroIdentificacao", numeroIdentificacao)


                storageReference.putBytes(image).addOnSuccessListener {
                    Log.d("Ponto", "1")

                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Ponto", "2")
                        val imageUrl = uri.toString()

                        continuation.resume(imageUrl)
                    }.addOnFailureListener {
                        Log.e("Ponto", "3")
                        continuation.resume("null")
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Ponto", "5")
                    continuation.resume("null")
                }
            }
        }
    }
}