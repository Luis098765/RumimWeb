package com.example.teste

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.teste.data.classesDeDados.Register
import com.example.teste.data.classesDoBanco.UserViewModel
import com.example.teste.databinding.ActivityNovoRegistroBinding
import com.example.teste.databinding.ActivityPerfilAnimalBinding
import com.example.teste.databinding.ActivityRebanhoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class NovoRegistro : AppCompatActivity() {
    private lateinit var binding: ActivityNovoRegistroBinding
    lateinit var email: String
    lateinit var mUserViewModel: UserViewModel
    lateinit var documentId: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_registro)
        binding = ActivityNovoRegistroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        email = intent.getStringExtra("email").toString()
        documentId = intent.getStringExtra("documentId").toString()
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            preencherCampos()
        }

        binding?.btSalvar?.setOnClickListener {
            val navegarTelaAnimal = Intent(this, PerfilAnimal::class.java)

            CoroutineScope(Dispatchers.IO).launch {
                criarRegistro()

                navegarTelaAnimal.putExtra("documentId", documentId)
                startActivity(navegarTelaAnimal)
            }

            finish()
        }

        binding?.btDataAtual?.setOnClickListener {
            binding?.editData?.setText(SimpleDateFormat("dd/MM/yyyy").format(Date()))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, PerfilAnimal::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun preencherCampos () {
        val imageByteArray = mUserViewModel.getAnimalAndImage(documentId)?.first()?.image?.image
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)
        binding?.imageViewAnimal?.setImageBitmap(bitmap)

        val animal = mUserViewModel.getAnimalWithRegisters(documentId)?.first()?.animal

        binding?.textViewSexoData?.text = "${animal?.sexo} - ${animal?.nascimento}"
        binding?.textViewNumero?.text = documentId

        val pesoDesmame = mUserViewModel.getAnimalWithRegisters(documentId)?.first()?.registers?.find { it.nome.contains("Pesagem ao desmame") }?.valor ?: "null"

        preencherSpinnerTipoRegistro(pesoDesmame)
        preencherSpinnerStatus()
    }

    private fun preencherSpinnerTipoRegistro (pesoDesmame: String?) {
        val spinnerTipoRegistro = findViewById<Spinner>(R.id.spinnerTipoRegistro)
        spinnerTipoRegistro.prompt = ""

        val opcoesSpinnerComDesmame = arrayOf("Pesagem ao desmame", "Pesagem", "Vacina", "Alterar status", "Observação")
        val opcoesSpinnerSemDesmame = arrayOf("Pesagem", "Vacina", "Alterar status", "Observação")

        val opcoesSpinnerTipoRegistro = if (pesoDesmame != null && pesoDesmame != "null") {
            opcoesSpinnerSemDesmame
        } else {
            opcoesSpinnerComDesmame
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesSpinnerTipoRegistro)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoRegistro.adapter = spinnerAdapter
    }

    private fun preencherSpinnerStatus () {
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        spinnerStatus.prompt = ""

        val opcoesSpinnerStatus = arrayOf("Ativo", "Inativo", "Vendido", "Abatido", "Morto")

        val spinnerAdapterStatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesSpinnerStatus)
        spinnerAdapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = spinnerAdapterStatus
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun criarRegistro () {
        val opcaoSpinner = binding?.spinnerTipoRegistro?.selectedItem.toString()

        var data = binding?.editData?.text.toString()
        val descricao = binding?.editDescricao?.text.toString()
        var valor = binding?.editValor?.text.toString()

        when(opcaoSpinner) {
            "Pesagem ao desmame" -> {
                if (valor != "null" && data != "null") {
                    mUserViewModel.insertRegister(Register(0, opcaoSpinner, data, "$valor Kg", descricao, documentId))
                } else {
                    Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Valor, no mínimo", Toast.LENGTH_SHORT).show()
                }
            }

            "Pesagem" -> {
                if (valor != "null" && data != "null") {
                    mUserViewModel.insertRegister(Register(0, "Pesagem - $data", data, "$valor Kg", descricao, documentId))
                } else {
                    Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Valor, no mínimo", Toast.LENGTH_SHORT).show()
                }
            }

            "Vacina" -> {
                if (data != null && descricao != null) {
                    mUserViewModel.insertRegister(Register(0, opcaoSpinner, data, valor, descricao, documentId))
                } else {
                    Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Descrição, no mínimo", Toast.LENGTH_SHORT).show()
                }
            }

            "Alterar status" -> {
                if (data != null) {
                    val opcaoSpinnerStatus = binding?.spinnerStatus?.selectedItem.toString()

                    mUserViewModel.insertRegister(Register(0, "Alteração de status", data, opcaoSpinnerStatus, descricao, documentId))
                } else {
                    Toast.makeText(this@NovoRegistro, "Preencha o campo: Data, no mínimo", Toast.LENGTH_SHORT).show()
                }
            }

            "Observação" -> {
                if (data != null && descricao != null) {
                    mUserViewModel.insertRegister(Register(0, opcaoSpinner, data, valor, descricao, documentId))
                } else {
                    Toast.makeText(this@NovoRegistro, "Preencha os campos: Data e Descrição, no mínimo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}