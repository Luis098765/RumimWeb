package com.example.teste

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.classesAuxiliares.AnimaisOffline
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesAuxiliares.SincronizarBancos
import com.example.teste.data.classesDeDados.User
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Principal : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityPrincipalBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private var nomePropriedade: String? = null
    private var local: String? = null
    private var qtdAtivos: String? = null
    private var email: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        val mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        email = if (sharedPref.getString("email", null) != null) {
            sharedPref.getString("email", null)
        } else {
            auth.currentUser?.email.toString()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ), 1)
        }

        val docRef = db.collection("Usuarios").document(email!!).collection("Propriedades")

        if (isNetworkAvailable()) {
            docRef.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val propriedade = querySnapshot.documents[0].id

                    docRef.document(propriedade).addSnapshotListener { documento, error ->
                        if (documento?.exists() == true) {
                            nomePropriedade = documento.getString("Nome da propriedade")
                            local = documento.getString("Localização da propriedade")

                            if (mUserViewModel.getAllUsers() != null) {
                                if (mUserViewModel.getAllUsers()!!.none { it.email == email }) {
                                    mUserViewModel.insertUser(User(email!!, nomePropriedade!!, false))
                                }
                            } else {
                                mUserViewModel.insertUser(User(email!!, nomePropriedade!!, false))
                            }

                            sincronizarBancos(email!!, nomePropriedade!!)

                            docRef.document(propriedade).collection("Animais").get().addOnSuccessListener { querySnapshot ->
                                val numeroAnimaisAtivos = querySnapshot.size()

                                qtdAtivos = numeroAnimaisAtivos.toString()

                                if (sharedPref.getString("email", null) == null) {
                                    with(sharedPref.edit()) {
                                        putString("email", email)
                                        putString("nomePropriedade", nomePropriedade)
                                        putString("localizacao", local)
                                        putString("qtdAtivos", qtdAtivos)
                                        apply()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            nomePropriedade = sharedPref.getString("nomePropriedade", null)
            local = sharedPref.getString("localização", null)
            qtdAtivos = sharedPref.getString("qtdAtivos", null)

            if (mUserViewModel.getAllUsers() != null) {
                if (mUserViewModel.getAllUsers()!!.none { it.email == email }) {
                    mUserViewModel.insertUser(User(email!!, nomePropriedade!!, false))
                }
            } else {
                mUserViewModel.insertUser(User(email!!, nomePropriedade!!, false))
            }

            sincronizarBancos(email!!, nomePropriedade!!)
        }

        binding?.btSair?.setOnClickListener {
            with(sharedPref.edit()) {
                clear()
                apply()
            }

            Toast.makeText(this, "Usuário deslogado", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding?.btSincronizar?.setOnClickListener {
            Log.d("Users", mUserViewModel.getAllUsers().toString())
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun sincronizarBancos(email: String, nomePropriedade: String) {
        Log.d("Ponto", "1")
        if (isNetworkAvailable()) {
            Log.d("Ponto", "2")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("Ponto", "3")
                startService(Intent(this, SincronizarBancos::class.java).apply {
                    SincronizarBancos.email = email
                    SincronizarBancos.nomePropriedade = nomePropriedade
                })
                Log.d("Ponto", "4")
            }
        }
    }

    companion object {
        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("Principal", Context.MODE_PRIVATE)
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