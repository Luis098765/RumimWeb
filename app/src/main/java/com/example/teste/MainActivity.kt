package com.example.teste

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.teste.databinding.ActivityMainBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityMainBinding? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val sharedPref = Principal.getSharedPreferences(this)

        //Log.d("SharedPreferences", sharedPref.toString())
        //Log.d("email", sharedPref.getString("email", null).toString())

        if (sharedPref.getString("email", null) != null) {
            val navegarSegundaTela = Intent(this,Principal::class.java)
            startActivity(navegarSegundaTela)
        }

        auth = Firebase.auth

        binding?.btentrar?.setOnClickListener {
            if (isNetworkAvailable()) {
                val email: String = binding?.edtEmail?.text.toString()
                val password: String = binding?.edtSenha?.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    signIn(email, password)
                } else {
                    Toast.makeText(this@MainActivity, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "Sem internet!", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.btcadastro?.setOnClickListener {
            if (isNetworkAvailable()) {
                val navegarSegundaTela = Intent(this,TelaDeCadastro::class.java)
                startActivity(navegarSegundaTela)
            } else {
                Toast.makeText(this@MainActivity, "Sem internet!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                //Log.d(TAG, "signInWithEmailAndPassword: Success")

                val navegarTelaPrincipal = Intent(this,Principal::class.java)
                startActivity(navegarTelaPrincipal)
            } else {
                //Log.w(TAG, "signInWithEmailAndPassword: Failure")
                Toast.makeText(baseContext, "Falha na autenticação", Toast.LENGTH_SHORT).show()
                if (task.exception is FirebaseAuthInvalidUserException) {
                    Toast.makeText(this@MainActivity, "E-mail inválido!", Toast.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@MainActivity, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseNetworkException) {
                    Toast.makeText(this@MainActivity, "Sem internet!", Toast.LENGTH_SHORT).show()
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

    companion object {
        private var TAG = "EmailAndPassword"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}