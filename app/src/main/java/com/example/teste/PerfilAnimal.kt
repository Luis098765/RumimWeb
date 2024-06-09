package com.example.teste

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.classesDoBanco.UserViewModel
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
    private lateinit var binding: ActivityPerfilAnimalBinding
    lateinit var email: String
    lateinit var mUserViewModel: UserViewModel
    lateinit var documentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_animal)
        binding = ActivityPerfilAnimalBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        email = intent.getStringExtra("email").toString()
        documentId = intent.getStringExtra("documentId").toString()
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding?.btNovoRegistro?.setOnClickListener {
            val navegarTelaNovoRegistro = Intent(this, NovoRegistro::class.java)
            navegarTelaNovoRegistro.putExtra("documentId", documentId)
            navegarTelaNovoRegistro.putExtra("email", email)
            startActivity(navegarTelaNovoRegistro)
        }

        binding?.btVoltar?.setOnClickListener {
            startActivity(Intent(this, Rebanho::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        preencherInformacoesAnimal()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencherInformacoesAnimal() {
        val animal = mUserViewModel.getAnimalAndImage(documentId)?.first()?.animal

        val pesoDesmame = animal?.pesoDesmame
        val dataDesmame = animal?.dataDesmame
        val status = animal?.status

        val pesoAtual = mUserViewModel.getPesoAtualFromAnimal(documentId).toString()

        binding?.textViewNumero?.text = animal?.numeroIdentificacao
        binding?.textViewCategoria?.text = animal?.categoria
        binding?.textViewRaca?.text = animal?.raca
        binding?.textViewSexo?.text = animal?.sexo
        binding?.textViewPesoNascimento?.text = animal?.pesoNascimento
        binding?.textViewDataNascimento?.text = animal?.nascimento
        binding?.textViewPesoAtual?.text = pesoAtual
        if (status != null) { binding?.textViewStatusAnimal?.text = "Status do animal: $status" }
        if (pesoDesmame != null) { binding?.textViewPesoDesmame?.text = pesoDesmame }
        if (dataDesmame != null) { binding?.textViewDataDesmame?.text = dataDesmame }

        val imageByteArray = mUserViewModel.getAnimalAndImage(documentId)?.first()?.image?.image
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)

        binding?.imageViewAnimal?.setImageBitmap(bitmap)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this, Rebanho::class.java))
    }
}