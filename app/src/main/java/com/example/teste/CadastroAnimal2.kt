package com.example.teste

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.classesAuxiliares.ImagemAnimal
import com.example.teste.data.classesDeDados.Animal
import com.example.teste.data.classesDeDados.Image
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityCadastroAnimal2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class CadastroAnimal2 : AppCompatActivity() {
    private var binding: ActivityCadastroAnimal2Binding? = null
    lateinit var email: String
    lateinit var nomePropriedade: String
    lateinit var mUserViewModel: UserViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_animal2)
        binding = ActivityCadastroAnimal2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        email = intent.getStringExtra("email").toString()
        nomePropriedade = intent.getStringExtra("nomePropriedade").toString()
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val tipo = intent.getStringExtra("tipo").toString()
        val sexo = intent.getStringExtra("sexo").toString()

        val spinner = findViewById<Spinner>(R.id.spinnerCategoria)
        spinner.prompt = ""
        if (tipo == "Ovino") {
            if (sexo == "Fêmea") {
                val opcoesOvinoFemea = arrayOf("Cordeira", "Borrega", "Ovelha")
                val ovinoFemeaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesOvinoFemea)
                ovinoFemeaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = ovinoFemeaAdapter
            } else {
                val opcoesOvinoMacho = arrayOf("Cordeiro", "Borrego", "Carneiro")
                val ovinoMachoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesOvinoMacho)
                ovinoMachoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = ovinoMachoAdapter
            }
        } else {
            if (sexo == "Fêmea") {
                val opcoesCaprinoFemea = arrayOf("Cabrita em aleitamento", "Cabrita desmamada", "Cabrita em engorda", "Cabra")
                val caprinoFemeaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesCaprinoFemea)
                caprinoFemeaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = caprinoFemeaAdapter
            } else {
                val opcoesCaprinoMacho = arrayOf("Cabrito em aleitamento", "Cabrito desmamado", "Cabrito em engorda", "Bode")
                val caprinoMachoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesCaprinoMacho)
                caprinoMachoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = caprinoMachoAdapter
            }
        }

        binding?.btVoltar?.setOnClickListener {
            val voltarCadastroAnimal1 = Intent(this, CadastroAnimal1::class.java)
            startActivity(voltarCadastroAnimal1)
        }

        binding?.btSalvar?.setOnClickListener {
            val voltarTelaPropriedade = Intent(this, InformacoesPropriedade::class.java)
            voltarTelaPropriedade.putExtra("email", email)

            CoroutineScope(Dispatchers.IO).launch {
                createAnimal()

                startActivity(voltarTelaPropriedade)
            }

            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun createAnimal () {
        val numeroAnimal = intent.getStringExtra("numero animal").toString()
        val nascimentoAnimal = intent.getStringExtra("nascimento").toString()
        val raca = intent.getStringExtra("raça").toString()
        val sexo = intent.getStringExtra("sexo").toString()
        val categoria = binding?.spinnerCategoria?.selectedItem.toString()
        val pesoNascimento = binding?.editPesoNascimento?.text.toString() + " Kg"

        val imagemAnimal = ImagemAnimal.getInstance(numeroAnimal, null)

        val imagemByteArray = if (imagemAnimal.getImage() != null) {
            imagemAnimal.getImage()
        } else {
            mUserViewModel.getNoImage()
        }

        imagemAnimal.delete()

        val bitmap = BitmapFactory.decodeByteArray(imagemByteArray, 0, imagemByteArray!!.size)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageByteArray = outputStream.toByteArray()

        mUserViewModel.insertAnimal(Animal(
            numeroAnimal,
            nascimentoAnimal,
            raca,
            sexo,
            categoria,
            pesoNascimento,
            pesoDesmame = null,
            dataDesmame = null,
            email
        ))

        mUserViewModel.insertImage(Image(numeroAnimal, imageByteArray!!))
    }
}