package com.example.teste

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.example.teste.data.classesDeDados.Image
import com.example.teste.data.classesDeDados.User
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
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

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/teste-ruminweb.appspot.com/o/Imagens%2F66682.png?alt=media&token=c8ba32de-ea76-4d63-8caf-03c42971961e")

        CoroutineScope(Dispatchers.IO).launch {
            val imageByteArray = storageRef.getBytes(Long.MAX_VALUE).await()

            mUserViewModel.insertImage(Image("imagemPadrao", imageByteArray))
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

        CoroutineScope(Dispatchers.IO).launch {
            if (isNetworkAvailable()) {
                val querySnapshot = docRef.get().await()

                if (!querySnapshot.isEmpty) {
                    val propriedade = querySnapshot.documents[0].id
                    val documento = docRef.document(propriedade).get().await()

                    if (documento?.exists() == true) {
                        nomePropriedade = documento.getString("Nome da propriedade")
                        local = documento.getString("Localização da propriedade")

                        if (mUserViewModel.getAllUsers() != null) {
                            if (mUserViewModel.getAllUsers()!!.none {it.email == email}) {
                                mUserViewModel.insertUser(User(email!!, nomePropriedade!!, local!!, false))
                            }
                        } else {
                            mUserViewModel.insertUser(User(email!!, nomePropriedade!!, local!!, false))
                        }

                        val animaisSnapshot = docRef.document(nomePropriedade!!).collection("Animais").get().await()

                        val numeroAnimaisAtivos = animaisSnapshot.size()

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
            } else {
                nomePropriedade = sharedPref.getString("nomePropriedade", null)
                local = sharedPref.getString("localização", null)
                qtdAtivos = sharedPref.getString("qtdAtivos", null)

                if (mUserViewModel.getAllUsers() != null) {
                    if (mUserViewModel.getAllUsers()!!.none { it.email == email }) {
                        mUserViewModel.insertUser(User(email!!, nomePropriedade!!, local!!, false))
                    }
                } else {
                    mUserViewModel.insertUser(User(email!!, nomePropriedade!!, local!! , false))
                }
            }

            Log.d("Email", email ?: "null")

            Log.d("User", mUserViewModel.getUserWithAnimals(email!!)?.first()?.user.toString())

            sincronizarBancos(mUserViewModel.getUserWithAnimals(email!!)?.first()?.user?.email.toString(), nomePropriedade!!)
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
            Log.d("Users", mUserViewModel.getUserWithAnimals(email!!)?.first()?.animals.toString())
        }

        binding?.btPropriedade?.setOnClickListener {
            val propriedade = mUserViewModel.getUserWithAnimals(email!!)?.first()?.user?.nomePropriedade.toString()

            if (propriedade == "null") {
                val navegarCadastroPropriedade1 = Intent(this,CadastroDePropriedade1::class.java)
                navegarCadastroPropriedade1.putExtra("email", email)
                navegarCadastroPropriedade1.putExtra("nomePropriedade", propriedade)
                startActivity(navegarCadastroPropriedade1)
            } else {
                val navegarInformacoesPropriedade = Intent(this,InformacoesPropriedade::class.java)
                navegarInformacoesPropriedade.putExtra("email", email)
                navegarInformacoesPropriedade.putExtra("nomePropriedade", propriedade)
                startActivity(navegarInformacoesPropriedade)
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